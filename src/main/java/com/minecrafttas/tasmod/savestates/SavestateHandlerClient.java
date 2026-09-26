package com.minecrafttas.tasmod.savestates;

import static com.minecrafttas.tasmod.TASmod.LOGGER;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;

import com.dselent.bigarraylist.BigArrayList;
import com.minecrafttas.tasmod.mctcommon.events.EventListenerRegistry;
import com.minecrafttas.tasmod.mctcommon.networking.Client.Side;
import com.minecrafttas.tasmod.mctcommon.networking.exception.PacketNotImplementedException;
import com.minecrafttas.tasmod.mctcommon.networking.exception.WrongSideException;
import com.minecrafttas.tasmod.mctcommon.networking.interfaces.ClientPacketHandler;
import com.minecrafttas.tasmod.mctcommon.networking.interfaces.PacketID;
import com.minecrafttas.tasmod.TASmodClient;
import com.minecrafttas.tasmod.events.EventSavestate;
import com.minecrafttas.tasmod.networking.TASmodBufferBuilder;
import com.minecrafttas.tasmod.playback.PlaybackControllerClient;
import com.minecrafttas.tasmod.playback.PlaybackControllerClient.InputContainer;
import com.minecrafttas.tasmod.playback.PlaybackControllerClient.TASstate;
import com.minecrafttas.tasmod.playback.tasfile.PlaybackSerialiser;
import com.minecrafttas.tasmod.registries.TASmodAPIRegistry;
import com.minecrafttas.tasmod.registries.TASmodPackets;
import com.minecrafttas.tasmod.savestates.exceptions.SavestateException;
import com.minecrafttas.tasmod.util.Ducks.ChunkProviderDuck;
import com.minecrafttas.tasmod.util.Ducks.ClientLevelDuck;
import com.minecrafttas.tasmod.util.LoggerMarkers;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.world.level.chunk.LevelChunk;

/**
 * Various savestate steps and actions for the client side
 * 
 * @author Scribble
 */
public class SavestateHandlerClient implements ClientPacketHandler, EventSavestate.EventClientCompleteLoadstate, EventSavestate.EventClientLoadPlayer {

	public final static Path clientSavestateDirectory = TASmodClient.getTasFileDirectory().resolve("savestates");

	/**
	 * A bug occurs when unloading the client world. The client world has a
	 * "unloadedEntityList" which, as the name implies, stores all unloaded entities
	 * <br>
	 * <br>
	 * Strange things happen, when the client player is unloaded, which is what
	 * happens when we use
	 * {@linkplain SavestateHandlerClient#unloadAllClientChunks()}.<br>
	 * <br>
	 * This method ensures that the player is loaded by removing the player from the
	 * unloadedEntityList. <br>
	 * <br>
	 * TLDR:<br>
	 * Makes sure that the player is not removed from the loaded entity list<br>
	 * <br>
	 * Side: Client
	 */
	@Override
	public void onClientLoadPlayer(LocalPlayer player) {
		LOGGER.trace(LoggerMarkers.Savestate, "Keep player {} in loaded entity list", player.getName());
		// Minecraft.getInstance().level.getEntityList().remove(player); // API changed in 1.22+
	}

	/**
	 * Similar to {@linkplain keepPlayerInLoadedEntityList}, the chunks themselves
	 * have a list with loaded entities <br>
	 * <br>
	 * Even after adding the player to the world, the chunks may not load the player
	 * correctly. <br>
	 * <br>
	 * Without this, no model is shown in third person<br>
	 * This state is fixed, once the player moves into a different chunk, since the
	 * new chunk adds the player to it's list. <br>
	 * <br>
	 * TLDR:<br>
	 * Adds the player to the chunk so the player is shown in third person <br>
	 * <br>
	 * Side: Client
	 */
	@Override
	public void onClientLoadstateComplete() {
		LocalPlayer player = Minecraft.getInstance().player;
		LOGGER.trace(LoggerMarkers.Savestate, "Add player {} to loaded entity list", player.getName());
		int i = Mth.floor(player.getX() / 16.0D);
		int j = Mth.floor(player.getZ() / 16.0D);
		LevelChunk chunk = Minecraft.getInstance().level.getChunk(i, j);
		// chunk.getEntities() API changed in 1.22+
		// if (chunk.getEntities().contains(player)) {
		// 	return;
		// }
		chunk.addEntity(player);
	}

	/**
	 * Makes a copy of the recording that is currently running. Gets triggered when
	 * a savestate is made on the server <br>
	 * Side: Client
	 * 
	 * @param nameOfSavestate coming from the server
	 * @throws SavestateException
	 * @throws IOException
	 */
	public static void savestate(String nameOfSavestate) throws SavestateException, IOException {
		LOGGER.debug(LoggerMarkers.Savestate, "Saving client savestate {}", nameOfSavestate);
		if (nameOfSavestate.isEmpty()) {
			LOGGER.error(LoggerMarkers.Savestate, "No recording savestate loaded since the name of savestate is empty");
			return;
		}

		Path targetfile = clientSavestateDirectory.resolve(nameOfSavestate + ".mctas").normalize();

		if (!targetfile.startsWith(clientSavestateDirectory)) {
			LOGGER.error("Could not create client savestate: Savestate won't be saved in savestate folder {}", targetfile);
			return;
		}

		Path targetParentDir = targetfile.getParent();
		if (!Files.exists(targetParentDir))
			Files.createDirectories(targetParentDir);

		PlaybackControllerClient container = TASmodClient.controller;
		if (container.isRecording()) {
			PlaybackSerialiser.saveToFile(targetfile, container, ""); // If the container is recording, store it entirely
		} else if (container.isPlayingback()) {
			PlaybackSerialiser.saveToFile(targetfile, container, "", container.index()); // If the container is playing, store it until the current index
		}
	}

	/**
	 * <p>Loads a copy of the TASfile from the file system and applies it depending on the {@link PlaybackControllerClient#state TASstate}.
	 * 
	 * <p>Savestates can be loaded while the state is {@link TASstate#RECORDING recording}, {@link TASstate#PLAYBACK playing back} or {@link TASstate#PAUSED paused},<br>
	 * in that case however, the {@link PlaybackControllerClient#stateAfterPause TASstate after pause} will be used.
	 * 
	 * @param nameOfSavestate coming from the server
	 * @throws IOException
	 */
	public static void loadstate(String nameOfSavestate) throws Exception {
		LOGGER.debug(LoggerMarkers.Savestate, "Loading client savestate {}", nameOfSavestate);
		if (nameOfSavestate.isEmpty()) {
			LOGGER.error(LoggerMarkers.Savestate, "No recording savestate loaded since the name of savestate is empty");
			return;
		}

		Path targetfile = clientSavestateDirectory.resolve(nameOfSavestate + ".mctas").normalize();

		if (!targetfile.startsWith(clientSavestateDirectory)) {
			LOGGER.error("Could not load client savestate: Savestate won't be saved in savestate folder {}", targetfile);
			return;
		}

		PlaybackControllerClient controller = TASmodClient.controller;

		TASstate state = controller.getState();

		if (state == TASstate.NONE) {
			TASmodClient.tickSchedulerClient.add(() -> {
				EventListenerRegistry.fireEvent(EventSavestate.EventClientCompleteLoadstate.class);
			});
			return;
		}

		if (state == TASstate.PAUSED) {
			state = controller.getStateAfterPause();
		}

		BigArrayList<InputContainer> savestateContainerList;

		if (Files.exists(targetfile)) {
			savestateContainerList = PlaybackSerialiser.loadFromFile(targetfile, state != TASstate.PLAYBACK);
		} else {
			controller.setTASStateClient(TASstate.NONE, false);
			Minecraft.getInstance().player.sendSystemMessage(Component.literal(ChatFormatting.YELLOW + "Inputs could not be loaded for this savestate,"));
			Minecraft.getInstance().player.sendSystemMessage(Component.literal(ChatFormatting.YELLOW + "since the file doesn't exist. Stopping!"));
			LOGGER.warn(LoggerMarkers.Savestate, "Inputs could not be loaded for this savestate, since the file doesn't exist.");
			return;
		}

		/*
		 * Imagine a recording that is 20 tick long with VV showing the current index of the controller:
		 *                     VV
		 *  0                  20
		 * <====================>
		 * 
		 * Now we load a savestate with only 10 ticks:
		 * 
		 * 0         10
		 * <==========>
		 * 
		 * We expect to resume the recording at the 10th tick.
		 * Therefore when loading a client savestate during a recording we set the index to size-1 and preload the inputs at the same index.
		 *           VV
		 * 0         10
		 * <==========> 
		 * 
		 * */
		if (state == TASstate.RECORDING) {
			long index = savestateContainerList.size() - 1;

			preload(savestateContainerList, index);
			controller.setInputs(savestateContainerList, index);
			TASmodClient.virtual.clearNext();
		}
		/*
		 * When loading a savestate during a playback 2 different scenarios can happen.
		 */
		else if (state == TASstate.PLAYBACK) {

			/*
			 * Scenario 1:
			 * The loadstated file is SMALLER than the total inputs in the controller:
			 * 
			 * The recording is 20 ticks long, with VV being the index where the playback is currently at.
			 *               VV
			 *  0            13    20
			 * <====================>
			 * 
			 * And our loadstated file being only 10 ticks long:
			 * 
			 * 0         10
			 * <==========>
			 * 
			 * We expect to start at tick 10 WITHOUT clearing the controller.
			 * If we were to replace the controller, everything above tick 10 would be lost.
			 * So we only set the index to 10 and preload the inputs.
			 * 
			 *            VV
			 *  0         10       20
			 * <====================>
			 * */
			if (controller.size() >= savestateContainerList.size()) {
				long index = savestateContainerList.size();

				preload(controller.getInputs(), index);
				controller.setIndex(index);
				TASmodClient.virtual.clearNext();
			}
			/*
			 * Scenario 2:
			 * The loadstated file is LARGER than the controller, 
			 * which may happen when loading a more recent savestate after loading an old one
			 * 
			 * In that case we just apply the playback just like in the recording
			 * */
			else {
				long index = savestateContainerList.size() - 1;

				preload(savestateContainerList, index);
				controller.setInputs(savestateContainerList, index);
			}
		}

		TASmodClient.tickSchedulerClient.add(() -> {
			EventListenerRegistry.fireEvent(EventSavestate.EventClientCompleteLoadstate.class);
		});
	}

	private static void preload(BigArrayList<InputContainer> containerList, long index) {
		LOGGER.trace(LoggerMarkers.Savestate, "Preloading container at index {}", index);
		InputContainer containerToPreload = containerList.get(index);
		// TASmodClient.virtual.preloadInput(containerToPreload.getKeyboard(), containerToPreload.getMouse(), containerToPreload.getCameraAngle()); // Type mismatch stubbed

		TASmodAPIRegistry.PLAYBACK_FILE_COMMAND.onPlaybackTick(index, containerToPreload);
	}

	/**
	 * Unloads all chunks and reloads the renderer so no chunks will be visible
	 * throughout the unloading progress<br>
	 * <br>
	 * Side: Client
	 * 
	 * @see MixinChunkProviderClient#unloadAllChunks()
	 */
	@Environment(EnvType.CLIENT)
	public static void unloadAllClientChunks() {
		LOGGER.trace(LoggerMarkers.Savestate, "Unloading All Client Chunks");
		Minecraft mc = Minecraft.getInstance();

		// ClientChunkManager might not exist in 1.22+
		// Use reflection or alternative approach
		// mc.levelRenderer.loadAllChunks(); // API changed
		// ((ClientLevelDuck) mc.level).clearEntityList();
	}

	@Override
	public PacketID[] getAcceptedPacketIDs() {
		return new TASmodPackets[] {
				//@formatter:off
				TASmodPackets.SAVESTATE_SAVE,
				TASmodPackets.SAVESTATE_LOAD
				};
				//@formatter:on
	}

	@Override
	public void onClientPacket(PacketID id, ByteBuffer buf, String username) throws PacketNotImplementedException, WrongSideException, Exception {
		TASmodPackets packet = (TASmodPackets) id;
		Minecraft mc = Minecraft.getInstance();

		switch (packet) {
			case SAVESTATE_SAVE:
				String savestateName = TASmodBufferBuilder.readString(buf);
				mc.execute(() -> {

					// Create client savestate
					try {
						SavestateHandlerClient.savestate(savestateName);
					} catch (SavestateException e) {
						LOGGER.error(e.getMessage());
					} catch (IOException e) {
						e.printStackTrace();
					}
				});
				break;
			case SAVESTATE_LOAD:
				// Load client savestate
				String loadstateName = TASmodBufferBuilder.readString(buf);
				mc.execute(() -> {
					try {
						SavestateHandlerClient.loadstate(loadstateName);
					} catch (IOException e) {
						e.printStackTrace();
					} catch (Exception e) {
						e.printStackTrace();
					}
				});
				break;
			default:
				throw new PacketNotImplementedException(packet, this.getClass(), Side.CLIENT);
		}
	}
}

