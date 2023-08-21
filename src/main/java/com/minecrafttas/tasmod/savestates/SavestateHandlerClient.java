package com.minecrafttas.tasmod.savestates;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.UUID;

import com.minecrafttas.common.server.exception.PacketNotImplementedException;
import com.minecrafttas.common.server.exception.WrongSideException;
import com.minecrafttas.common.server.interfaces.ClientPacketHandler;
import com.minecrafttas.common.server.interfaces.PacketID;
import com.minecrafttas.tasmod.TASmod;
import com.minecrafttas.tasmod.TASmodClient;
import com.minecrafttas.tasmod.mixin.savestates.MixinChunkProviderClient;
import com.minecrafttas.tasmod.networking.TASmodBufferBuilder;
import com.minecrafttas.tasmod.networking.TASmodPackets;
import com.minecrafttas.tasmod.playback.PlaybackController;
import com.minecrafttas.tasmod.playback.PlaybackController.TASstate;
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
public class SavestateHandlerClient implements ClientPacketHandler{

	public final static File savestateDirectory = new File(TASmodClient.tasdirectory + File.separator + "savestates");
	
	/**
	 * A bug occurs when unloading the client world. The client world has a "unloadedEntityList" which, as the name implies, stores all unloaded entities <br>
	 * <br>
	 * Strange things happen, when the client player is unloaded, which is what happens when we use {@linkplain SavestateHandlerClient#unloadAllClientChunks()}.<br>
	 * <br>
	 * This method ensures that the player is loaded by removing the player from the unloadedEntityList. <br>
	 * <br>
	 * TLDR:<br>
	 * Makes sure that the player is not removed from the loaded entity list<br>
	 * <br>
	 * Side: Client
	 */
	@Environment(EnvType.CLIENT)
	public static void keepPlayerInLoadedEntityList(net.minecraft.entity.player.EntityPlayer player) {
		TASmod.LOGGER.trace(LoggerMarkers.Savestate, "Keep player {} in loaded entity list", player.getName());
		Minecraft.getMinecraft().world.unloadedEntityList.remove(player);
	}

	/**
	 * Similar to {@linkplain keepPlayerInLoadedEntityList}, the chunks themselves have a list with loaded entities <br>
	 * <br>
	 * Even after adding the player to the world, the chunks may not load the player correctly. <br>
	 * <br>
	 * Without this, no model is shown in third person<br>
	 * This state is fixed, once the player moves into a different chunk, since the new chunk adds the player to it's list. <br>
	 * <br>
	 * 
	 * TLDR:<br>
	 * Adds the player to the chunk so the player is shown in third person <br>
	 * <br>
	 * Side: Client
	 */
	@Environment(EnvType.CLIENT)
	public static void addPlayerToClientChunk(EntityPlayer player) {
		TASmod.LOGGER.trace(LoggerMarkers.Savestate, "Add player {} to loaded entity list", player.getName());
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
		TASmod.LOGGER.debug(LoggerMarkers.Savestate, "Saving client savestate {}", nameOfSavestate);
		if (nameOfSavestate.isEmpty()) {
			TASmod.LOGGER.error(LoggerMarkers.Savestate, "No recording savestate loaded since the name of savestate is empty");
			return;
		}
	
		SavestateHandlerClient.savestateDirectory.mkdir();
	
		File targetfile = new File(SavestateHandlerClient.savestateDirectory, nameOfSavestate + ".mctas");
	
		PlaybackController container = TASmodClient.virtual.getContainer();
		if (container.isRecording()) {
			TASmodClient.serialiser.saveToFileV1(targetfile, container);	//If the container is recording, store it entirely
		} else if(container.isPlayingback()){
			TASmodClient.serialiser.saveToFileV1Until(targetfile, container, container.index()); //If the container is playing, store it until the current index
		}
	}

	/**
	 * Replaces the current recording with the recording from the savestate.
	 * Gets triggered when a savestate is loaded on the server<br>
	 * Side: Client
	 * 
	 * @param nameOfSavestate coming from the server
	 * @throws IOException
	 */
	public static void loadstate(String nameOfSavestate) throws IOException {
		TASmod.LOGGER.debug(LoggerMarkers.Savestate, "Loading client savestate {}", nameOfSavestate);
		if (nameOfSavestate.isEmpty()) {
			TASmod.LOGGER.error(LoggerMarkers.Savestate, "No recording savestate loaded since the name of savestate is empty");
			return;
		}
	
		savestateDirectory.mkdir();
	
		File targetfile = new File(savestateDirectory, nameOfSavestate + ".mctas");
	
		PlaybackController container = TASmodClient.virtual.getContainer();
		if (!container.isNothingPlaying()) { // If the file exists and the container is recording or playing, load the clientSavestate
			if (targetfile.exists()) {
				TASmodClient.virtual.loadClientSavestate(TASmodClient.serialiser.fromEntireFileV1(targetfile));
			} else {
				TASmodClient.virtual.getContainer().setTASState(TASstate.NONE, false);
				Minecraft.getMinecraft().player.sendMessage(new TextComponentString(ChatFormatting.YELLOW
						+ "Inputs could not be loaded for this savestate, since the file doesn't exist. Stopping!"));
				TASmod.LOGGER.warn(LoggerMarkers.Savestate, "Inputs could not be loaded for this savestate, since the file doesn't exist.");
			}
		}
	}

	public static void loadPlayer(NBTTagCompound compound) {
		Minecraft mc = Minecraft.getMinecraft();
		EntityPlayerSP player = mc.player;

		player.readFromNBT(compound);
		NBTTagCompound motion = compound.getCompoundTag("clientMotion");

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

		// #86
		int gamemode = compound.getInteger("playerGameType");
		GameType type = GameType.getByID(gamemode);
		Minecraft.getMinecraft().playerController.setGameType(type);

		// #?? Player rotation does not change when loading a savestate
//		CameraInterpolationEvents.rotationPitch = player.rotationPitch;
//		CameraInterpolationEvents.rotationYaw = player.rotationYaw + 180f;

		SavestateHandlerClient.keepPlayerInLoadedEntityList(player);
	}

	/**
	 * Unloads all chunks and reloads the renderer so no chunks will be visible throughout the unloading progress<br>
	 * <br>
	 * Side: Client
	 * @see MixinChunkProviderClient#unloadAllChunks()
	 */
	@Environment(EnvType.CLIENT)
	public static void unloadAllClientChunks() {
		TASmod.LOGGER.trace(LoggerMarkers.Savestate, "Unloading All Client Chunks");
		Minecraft mc = Minecraft.getMinecraft();
		
		ChunkProviderClient chunkProvider=mc.world.getChunkProvider();
		
		((ChunkProviderDuck)chunkProvider).unloadAllChunks();
		Minecraft.getMinecraft().renderGlobal.loadRenderers();
	}

	@Override
	public PacketID[] getAcceptedPacketIDs() {
		return new TASmodPackets[] {
				TASmodPackets.SAVESTATE_SAVE,
				TASmodPackets.SAVESTATE_LOAD,
				TASmodPackets.SAVESTATE_PLAYER,
				TASmodPackets.SAVESTATE_SCREEN,
				TASmodPackets.SAVESTATE_UNLOAD_CHUNKS
		};
	}

	@Override
	public void onClientPacket(PacketID id, ByteBuffer buf, UUID clientID) throws PacketNotImplementedException, WrongSideException, Exception {
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
				TASmod.LOGGER.error(e.getMessage());
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
			NBTTagCompound compound = TASmodBufferBuilder.readNBTTagCompound(buf);
			SavestateHandlerClient.loadPlayer(compound);
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
			SavestateHandlerClient.unloadAllClientChunks();
			break;

		default:
			throw new PacketNotImplementedException(packet, this.getClass());
		}
		
	}

}
