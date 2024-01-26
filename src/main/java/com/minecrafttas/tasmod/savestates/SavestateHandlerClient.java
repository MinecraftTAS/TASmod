package com.minecrafttas.tasmod.savestates;

import static com.minecrafttas.tasmod.TASmod.LOGGER;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

import com.minecrafttas.mctcommon.server.Client.Side;
import com.minecrafttas.mctcommon.server.exception.PacketNotImplementedException;
import com.minecrafttas.mctcommon.server.exception.WrongSideException;
import com.minecrafttas.mctcommon.server.interfaces.ClientPacketHandler;
import com.minecrafttas.mctcommon.server.interfaces.PacketID;
import com.minecrafttas.tasmod.TASmodClient;
import com.minecrafttas.tasmod.mixin.savestates.MixinChunkProviderClient;
import com.minecrafttas.tasmod.networking.TASmodBufferBuilder;
import com.minecrafttas.tasmod.networking.TASmodPackets;
import com.minecrafttas.tasmod.playback.PlaybackControllerClient;
import com.minecrafttas.tasmod.playback.PlaybackControllerClient.TASstate;
import com.minecrafttas.tasmod.savestates.SavestateHandlerServer.PlayerHandler.MotionData;
import com.minecrafttas.tasmod.savestates.exceptions.SavestateException;
import com.minecrafttas.tasmod.savestates.gui.GuiSavestateSavingScreen;
import com.minecrafttas.tasmod.util.Ducks.ChunkProviderDuck;
import com.minecrafttas.tasmod.util.LoggerMarkers;
import com.mojang.realmsclient.gui.ChatFormatting;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.ChunkProviderClient;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.GameType;
import net.minecraft.world.chunk.Chunk;

/**
 * Various savestate steps and actions for the client side
 * 
 * @author Scribble
 */
public class SavestateHandlerClient implements ClientPacketHandler {

	public final static File savestateDirectory = new File(TASmodClient.tasdirectory + File.separator + "savestates");

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
	@Environment(EnvType.CLIENT)
	public static void keepPlayerInLoadedEntityList(net.minecraft.entity.player.EntityPlayer player) {
		LOGGER.trace(LoggerMarkers.Savestate, "Keep player {} in loaded entity list", player.getName());
		Minecraft.getMinecraft().world.unloadedEntityList.remove(player);
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
	 * 
	 * TLDR:<br>
	 * Adds the player to the chunk so the player is shown in third person <br>
	 * <br>
	 * Side: Client
	 */
	@Environment(EnvType.CLIENT)
	public static void addPlayerToClientChunk(EntityPlayer player) {
		LOGGER.trace(LoggerMarkers.Savestate, "Add player {} to loaded entity list", player.getName());
		int i = MathHelper.floor(player.posX / 16.0D);
		int j = MathHelper.floor(player.posZ / 16.0D);
		Chunk chunk = Minecraft.getMinecraft().world.getChunkFromChunkCoords(i, j);
		for (int k = 0; k < chunk.getEntityLists().length; k++) {
			if (chunk.getEntityLists()[k].contains(player)) {
				return;
			}
		}
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

		SavestateHandlerClient.savestateDirectory.mkdir();

		File targetfile = new File(SavestateHandlerClient.savestateDirectory, nameOfSavestate + ".mctas");

		PlaybackControllerClient container = TASmodClient.controller;
		if (container.isRecording()) {
			TASmodClient.serialiser.saveToFileV1(targetfile, container); // If the container is recording, store it entirely
		} else if (container.isPlayingback()) {
			TASmodClient.serialiser.saveToFileV1Until(targetfile, container, container.index()); // If the container is playing, store it until the current index
		}
	}

	/**
	 * Replaces the current recording with the recording from the savestate. Gets
	 * triggered when a savestate is loaded on the server<br>
	 * Side: Client
	 * 
	 * @param nameOfSavestate coming from the server
	 * @throws IOException
	 */
	public static void loadstate(String nameOfSavestate) throws IOException {
		LOGGER.debug(LoggerMarkers.Savestate, "Loading client savestate {}", nameOfSavestate);
		if (nameOfSavestate.isEmpty()) {
			LOGGER.error(LoggerMarkers.Savestate, "No recording savestate loaded since the name of savestate is empty");
			return;
		}

		savestateDirectory.mkdir();

		File targetfile = new File(savestateDirectory, nameOfSavestate + ".mctas");

		PlaybackControllerClient container = TASmodClient.controller;
		if (!container.isNothingPlaying()) { // If the file exists and the container is recording or playing, load the
												// clientSavestate
			if (targetfile.exists()) {
//				TASmodClient.virtual.loadClientSavestate(TASmodClient.serialiser.fromEntireFileV1(targetfile)); TODO Move to PlaybackController
			} else {
				TASmodClient.controller.setTASStateClient(TASstate.NONE, false);
				Minecraft.getMinecraft().player.sendMessage(new TextComponentString(ChatFormatting.YELLOW + "Inputs could not be loaded for this savestate,"));
				Minecraft.getMinecraft().player.sendMessage(new TextComponentString(ChatFormatting.YELLOW + "since the file doesn't exist. Stopping!"));
				LOGGER.warn(LoggerMarkers.Savestate, "Inputs could not be loaded for this savestate, since the file doesn't exist.");
			}
		}
	}

	public static void loadPlayer(NBTTagCompound compound) {
		LOGGER.trace(LoggerMarkers.Savestate, "Loading client player from NBT");
		Minecraft mc = Minecraft.getMinecraft();
		EntityPlayerSP player = mc.player;

		player.readFromNBT(compound);
		NBTTagCompound motion = compound.getCompoundTag("clientMotion");
		
		if(motion.hasNoTags()) {
			LOGGER.warn(LoggerMarkers.Savestate, "Could not load the motion from the savestate. Savestate seems to be created manually or by a different mod");
		} else {
			LOGGER.trace(LoggerMarkers.Savestate, "Loading client motion from NBT");
			double x = motion.getDouble("x");
			double y = motion.getDouble("y");
			double z = motion.getDouble("z");
			player.motionX = x;
			player.motionY = y;
			player.motionZ = z;
			
			float rx = motion.getFloat("RelativeX");
			float ry = motion.getFloat("RelativeY");
			float rz = motion.getFloat("RelativeZ");
			player.moveForward = rx;
			player.moveVertical = ry;
			player.moveStrafing = rz;
			
			boolean sprinting = motion.getBoolean("Sprinting");
			float jumpVector = motion.getFloat("JumpFactor");
			player.setSprinting(sprinting);
			player.jumpMovementFactor = jumpVector;
		}

		LOGGER.trace(LoggerMarkers.Savestate, "Setting client gamemode");
		// #86
		int gamemode = compound.getInteger("playerGameType");
		GameType type = GameType.getByID(gamemode);
		mc.playerController.setGameType(type);

		// #?? Player rotation does not change when loading a savestate
//		CameraInterpolationEvents.rotationPitch = player.rotationPitch;
//		CameraInterpolationEvents.rotationYaw = player.rotationYaw + 180f;

		SavestateHandlerClient.keepPlayerInLoadedEntityList(player);
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
		Minecraft mc = Minecraft.getMinecraft();

		ChunkProviderClient chunkProvider = mc.world.getChunkProvider();

		((ChunkProviderDuck) chunkProvider).unloadAllChunks();
		Minecraft.getMinecraft().renderGlobal.loadRenderers();
	}

	@Override
	public PacketID[] getAcceptedPacketIDs() {
		return new TASmodPackets[] { 
				TASmodPackets.SAVESTATE_SAVE, 
				TASmodPackets.SAVESTATE_LOAD, 
				TASmodPackets.SAVESTATE_PLAYER,
				TASmodPackets.SAVESTATE_REQUEST_MOTION,
				TASmodPackets.SAVESTATE_SCREEN,
				TASmodPackets.SAVESTATE_UNLOAD_CHUNKS
				};
	}

	@Override
	public void onClientPacket(PacketID id, ByteBuffer buf, String username) throws PacketNotImplementedException, WrongSideException, Exception {
		TASmodPackets packet = (TASmodPackets) id;
		String name = null;
		Minecraft mc = Minecraft.getMinecraft();

		switch (packet) {
			case SAVESTATE_SAVE:
				// Create client savestate
				name = TASmodBufferBuilder.readString(buf);
				try {
					SavestateHandlerClient.savestate(name);
				} catch (SavestateException e) {
					LOGGER.error(e.getMessage());
				} catch (IOException e) {
					e.printStackTrace();
				}
				break;
			case SAVESTATE_LOAD:
				// Load client savestate
				name = TASmodBufferBuilder.readString(buf);
				try {
					SavestateHandlerClient.loadstate(name);
				} catch (IOException e) {
					e.printStackTrace();
				}
				break;
			case SAVESTATE_PLAYER:
				NBTTagCompound compound;
				try {
					compound = TASmodBufferBuilder.readNBTTagCompound(buf);
				} catch (IOException e) {
					e.printStackTrace();
					return;
				}
				/*
				 * Fair warning: Do NOT read the buffer inside an addScheduledTask. Read it
				 * before that. The buffer will have the wrong limit, when the task is executed.
				 * This is probably due to the buffers being reused.
				 */
				Minecraft.getMinecraft().addScheduledTask(() -> {
					SavestateHandlerClient.loadPlayer(compound);
				});
				break;

			case SAVESTATE_REQUEST_MOTION:
				EntityPlayerSP player = Minecraft.getMinecraft().player;
				if (player != null) {
					if (!(Minecraft.getMinecraft().currentScreen instanceof GuiSavestateSavingScreen)) {
						Minecraft.getMinecraft().displayGuiScreen(new GuiSavestateSavingScreen());
					}
					TASmodClient.client.send(
							new TASmodBufferBuilder(TASmodPackets.SAVESTATE_REQUEST_MOTION)
							.writeMotionData(
									new MotionData(
											player.motionX,
											player.motionY,
											player.motionZ,
											player.moveForward,
											player.moveVertical,
											player.moveStrafing,
											player.isSprinting(),
											player.jumpMovementFactor)
									)
							);
				}
				break;
			case SAVESTATE_SCREEN:
				// Open/Close Savestate screen
				boolean open = TASmodBufferBuilder.readBoolean(buf);
				if (open) {
					mc.displayGuiScreen(new GuiSavestateSavingScreen());
				} else {
					mc.displayGuiScreen(null);
				}
				break;

			case SAVESTATE_UNLOAD_CHUNKS:
				Minecraft.getMinecraft().addScheduledTask(() -> {
					SavestateHandlerClient.unloadAllClientChunks();
				});
				break;

			default:
				throw new PacketNotImplementedException(packet, this.getClass(), Side.CLIENT);
		}

	}

}
