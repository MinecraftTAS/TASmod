package com.minecrafttas.tasmod.savestates;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.Maps;
import com.minecrafttas.common.server.Client.Side;
import com.minecrafttas.common.server.exception.PacketNotImplementedException;
import com.minecrafttas.common.server.exception.WrongSideException;
import com.minecrafttas.common.server.interfaces.PacketID;
import com.minecrafttas.common.server.interfaces.ServerPacketHandler;
import com.minecrafttas.tasmod.TASmod;
import com.minecrafttas.tasmod.events.EventServer.EventCompleteLoadstate;
import com.minecrafttas.tasmod.events.EventServer.EventLoadstate;
import com.minecrafttas.tasmod.events.EventServer.EventSavestate;
import com.minecrafttas.tasmod.mixin.savestates.AccessorAnvilChunkLoader;
import com.minecrafttas.tasmod.mixin.savestates.AccessorChunkLoader;
import com.minecrafttas.tasmod.mixin.savestates.MixinChunkProviderServer;
import com.minecrafttas.tasmod.networking.TASmodBufferBuilder;
import com.minecrafttas.tasmod.networking.TASmodPackets;
import com.minecrafttas.tasmod.savestates.exceptions.LoadstateException;
import com.minecrafttas.tasmod.savestates.exceptions.SavestateDeleteException;
import com.minecrafttas.tasmod.savestates.exceptions.SavestateException;
import com.minecrafttas.tasmod.savestates.files.SavestateDataFile;
import com.minecrafttas.tasmod.savestates.files.SavestateDataFile.DataValues;
import com.minecrafttas.tasmod.savestates.files.SavestateTrackerFile;
import com.minecrafttas.tasmod.util.Ducks.ChunkProviderDuck;
import com.minecrafttas.tasmod.util.LoggerMarkers;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.NextTickListEntry;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.AnvilChunkLoader;
import net.minecraft.world.gen.ChunkProviderServer;
import net.minecraft.world.storage.SaveHandler;
import net.minecraft.world.storage.WorldInfo;

/**
 * Creates and loads savestates on both client and server without closing the
 * world <br>
 * The old version that you may find in TASTools was heavily inspired by bspkrs'
 * <a href=
 * "https://www.curseforge.com/minecraft/mc-mods/worldstatecheckpoints">WorldStateCheckpoints</a>,
 * but this new version is completely self written.
 * 
 * @author Scribble
 *
 */
public class SavestateHandlerServer implements EventCompleteLoadstate, ServerPacketHandler {

	private MinecraftServer server;
	private File savestateDirectory;

	public SavestateState state = SavestateState.NONE;

	private final List<Integer> indexList = new ArrayList<>();

	private int latestIndex = 0;
	private int currentIndex;
	
	private final Logger logger;
	public static boolean wasLoading;
	
	/**
	 * Creates a savestate handler on the specified server
	 * @param logger 
	 * 
	 * @param The server that should store the savestates
	 */
	public SavestateHandlerServer(MinecraftServer server, Logger logger) {
		this.server = server;
		this.logger = logger;
		createSavestateDirectory();
		refresh();
		loadCurrentIndexFromFile();
	}

	/**
	 * Creates a copy of the world that is currently being played and saves it in
	 * .minecraft/saves/savestates/worldname-Savestate[{@linkplain #currentIndex}+1]
	 * <p>
	 * Side: Server
	 * 
	 * @throws SavestateException
	 * @throws IOException
	 */
	public void saveState() throws SavestateException, IOException {
		saveState(-1, true);
	}
	
	public void saveState(int savestateIndex, boolean tickrate0) throws SavestateException, IOException {
		saveState(savestateIndex, tickrate0, true);
	}

	/**
	 * Creates a copy of the world that is currently being played and saves it in
	 * .minecraft/saves/savestates/worldname-Savestate[savestateIndex]
	 * <p>
	 * Side: Server
	 * 
	 * @param savestateIndex The index where the mod will save the savestate.
	 *                       index<0 if it should save it in the next index from
	 *                       the currentindex
	 * @param tickrate0 When true: Set's the game to tickrate 0 after creating a savestate
	 * @param changeIndex When true: Changes the index to the savestateIndex
	 * @throws SavestateException
	 * @throws IOException
	 */
	public void saveState(int savestateIndex, boolean tickrate0, boolean changeIndex) throws SavestateException, IOException {
		if(logger.isTraceEnabled()) {
			logger.trace(LoggerMarkers.Savestate, "SAVING a savestate with index {}, tickrate0 is {} and changeIndex is {}", savestateIndex, tickrate0, changeIndex);
		} else {
			logger.debug(LoggerMarkers.Savestate, "Creating new savestate");
		}
		
		if (state == SavestateState.SAVING) {
			throw new SavestateException("A savestating operation is already being carried out");
		}
		if (state == SavestateState.LOADING) {
			throw new SavestateException("A loadstate operation is being carried out");
		}
		
		try {
			// Open GuiSavestateScreen
			TASmod.server.sendToAll(new TASmodBufferBuilder(TASmodPackets.SAVESTATE_SCREEN).writeBoolean(true));
		} catch (Exception e) {
			TASmod.LOGGER.error("Unable to send packet to all clients:", e);
		}
		
		// Lock savestating and loadstating
		state = SavestateState.SAVING;
		
		// Create a directory just in case
		createSavestateDirectory();

		// Enable tickrate 0
		TASmod.tickratechanger.pauseGame(true);

		// Update the server variable
		server = TASmod.getServerInstance();

		// Get the motion from the client
		PlayerHandler.requestMotionFromClient();

		// Save the world!
		server.getPlayerList().saveAllPlayerData();
		server.saveAllWorlds(false);

		// Refreshing the index list
		refresh();

		// Setting the current index depending on the savestateIndex.
		int indexToSave=savestateIndex;
		if (savestateIndex < 0) {
			indexToSave=currentIndex + 1; // If the savestateIndex <= 0, create a savestate at currentIndex+1
		}
		
		// Update current index
		if(changeIndex) {
			setCurrentIndex(indexToSave);
		} else {
			logger.warn(LoggerMarkers.Savestate, "Keeping the savestate index at {}", currentIndex);
		}

		// Get the current and target directory for copying
		String worldname = server.getFolderName();
		File currentfolder = new File(savestateDirectory, ".." + File.separator + worldname);
		File targetfolder = getSavestateFile(indexToSave);
		
		EventSavestate.fireSavestateEvent(indexToSave, targetfolder, currentfolder);

		if (targetfolder.exists()) {
			logger.warn(LoggerMarkers.Savestate, "WARNING! Overwriting the savestate with the index {}", indexToSave);
			FileUtils.deleteDirectory(targetfolder);
		}

		/*
		 * Prevents creating an InputSavestate when saving at index 0 (Index 0 is the
		 * savestate when starting a recording)
		 */
		if (savestateIndex != 0) {
			/*
			 * Send the name of the world to all players. This will make a savestate of the
			 * recording on the client with that name
			 */
			try {
				// savestate inputs client
				TASmod.server.sendToAll(new TASmodBufferBuilder(TASmodPackets.SAVESTATE_SAVE).writeString(getSavestateName(indexToSave)));
			} catch (Exception e) {
				TASmod.LOGGER.error("Unable to send packet to all clients:", e);
			}
		}

		// Wait for the chunkloader to save the game
		for (WorldServer world : server.worlds) {
			AnvilChunkLoader chunkloader = (AnvilChunkLoader) ((AccessorChunkLoader)world.getChunkProvider()).getChunkLoader();
			
			while (((AccessorAnvilChunkLoader)chunkloader).getChunksToSave().size() > 0) {
			}
		}

		saveSavestateDataFile(false);
		
		// Copy the directory
		FileUtils.copyDirectory(currentfolder, targetfolder);

		// Incrementing info file
		SavestateTrackerFile tracker = new SavestateTrackerFile(new File(savestateDirectory, worldname + "-info.txt"));
		tracker.increaseSavestates();
		tracker.saveFile();

		// Send a notification that the savestate has been loaded
		server.getPlayerList().sendMessage(new TextComponentString(TextFormatting.GREEN + "Savestate " + indexToSave + " saved"));

		try {
			// close GuiSavestateScreen
			TASmod.server.sendToAll(new TASmodBufferBuilder(TASmodPackets.SAVESTATE_SCREEN).writeBoolean(false));
		} catch (Exception e) {
			TASmod.LOGGER.error("Unable to send packet to all clients:", e);
		}

		if (!tickrate0) {
			TASmod.tickratechanger.pauseGame(false);
		}

		// Unlock savestating
		state = SavestateState.NONE;
	}

	/**
	 * Loads the latest savestate at {@linkplain #currentIndex}
	 * .minecraft/saves/savestates/worldname-Savestate[{@linkplain #currentIndex}]
	 * <p>
	 * Side: Server
	 * 
	 * @throws LoadstateException
	 * @throws IOException
	 */
	public void loadState() throws LoadstateException, IOException {
		loadState(-1, true);
	}
	
	/**
	 * 
	 * @param savestateIndex
	 * @param tickrate0
	 * 
	 * @throws LoadstateException
	 * @throws IOException
	 */
	public void loadState(int savestateIndex, boolean tickrate0) throws LoadstateException, IOException {
		loadState(savestateIndex, tickrate0, true);
	}

	/**
	 * Loads the latest savestate it can find in
	 * .minecraft/saves/savestates/worldname-Savestate
	 * <p>
	 * Side: Server
	 * 
	 * @param savestateIndex The index where the mod will load the savestate.
	 *                       index<0 if it should load the currentindex
	 * @param tickrate0 When true: Set's the game to tickrate 0 after creating a savestate
	 * @param changeIndex When true: Changes the index to the savestateIndex
	 * @throws LoadstateException
	 * @throws IOException
	 */
	public void loadState(int savestateIndex, boolean tickrate0, boolean changeIndex) throws LoadstateException, IOException {
		if(logger.isTraceEnabled()) {
			logger.trace(LoggerMarkers.Savestate, "LOADING a savestate with index {}, tickrate0 is {} and changeIndex is {}", savestateIndex, tickrate0, changeIndex);
		} else {
			logger.debug(LoggerMarkers.Savestate, "Loading a savestate");
		}
		
		if (state == SavestateState.SAVING) {
			throw new LoadstateException("A savestating operation is already being carried out");
		}
		if (state == SavestateState.LOADING) {
			throw new LoadstateException("A loadstate operation is being carried out");
		}
		// Lock savestating and loadstating
		state = SavestateState.LOADING;
		
		// Create a directory just in case
		createSavestateDirectory();

		// Enable tickrate 0
		TASmod.tickratechanger.pauseGame(true);

		// Update the server instance
		server = TASmod.getServerInstance();

		refresh();

		int indexToLoad = savestateIndex < 0 ? currentIndex : savestateIndex;

		if (getSavestateFile(indexToLoad).exists()) {
			// Updating current index
			if(changeIndex) {
				setCurrentIndex(indexToLoad);
			} else {
				logger.warn(LoggerMarkers.Savestate, "Keeping the savestate index at {}", currentIndex);
			}
		} else {
			throw new LoadstateException("Savestate " + indexToLoad + " doesn't exist");
		}

		// Get the current and target directory for copying
		String worldname = server.getFolderName();
		File currentfolder = new File(savestateDirectory, ".." + File.separator + worldname);
		File targetfolder = getSavestateFile(indexToLoad);

		EventLoadstate.fireLoadstateEvent(indexToLoad, targetfolder, currentfolder);
		
		/*
		 * Prevents loading an InputSavestate when loading index 0 (Index 0 is the
		 * savestate when starting a recording. Not doing this will load an empty
		 * InputSavestate)
		 */
		if (savestateIndex != 0) {
			try {
				// loadstate inputs client
				TASmod.server.sendToAll(new TASmodBufferBuilder(TASmodPackets.SAVESTATE_LOAD).writeString(getSavestateName(savestateIndex)));
			} catch (Exception e) {
				TASmod.LOGGER.error("Unable to send packet to all clients:", e);
			}
		}

		// Disabeling level saving for all worlds in case the auto save kicks in during
		// world unload
		for (WorldServer world : server.worlds) {
			world.disableLevelSaving = true;
		}

		
		try {
			// unload chunks on client
			TASmod.server.sendToAll(new TASmodBufferBuilder(TASmodPackets.SAVESTATE_UNLOAD_CHUNKS));
		} catch (Exception e) {
			TASmod.LOGGER.error("Unable to send packet to all clients:", e);
		}

		// Unload chunks on the server
		ChunkHandler.disconnectPlayersFromChunkMap(server);
		ChunkHandler.unloadAllServerChunks(server);
		ChunkHandler.flushSaveHandler(server);

		// Delete and copy directories
		FileUtils.deleteDirectory(currentfolder);
		FileUtils.copyDirectory(targetfolder, currentfolder);
		
		// Loads savestate data from the file like name and ktrng seed if ktrng is loaded
		loadSavestateDataFile();

		// Update the player and the client
		PlayerHandler.loadAndSendMotionToPlayer(server);
		// Update the session.lock file so minecraft behaves and saves the world
		ChunkHandler.updateSessionLock(server);
		// Load the chunks and send them to the client
		ChunkHandler.addPlayersToChunkMap(server);

		// Enable level saving again
		for (WorldServer world : server.worlds) {
			world.disableLevelSaving = false;
		}

		// Incrementing info file
		SavestateTrackerFile tracker = new SavestateTrackerFile(new File(savestateDirectory, worldname + "-info.txt"));
		tracker.increaseRerecords();
		tracker.saveFile();

		// Send a notification that the savestate has been loaded
		server.getPlayerList().sendMessage(new TextComponentString(TextFormatting.GREEN + "Savestate " + indexToLoad + " loaded"));

		// Add players to the chunk
		server.getPlayerList().getPlayers().forEach(player->{
			ChunkHandler.addPlayerToServerChunk(player);
		});
		
		WorldServer[] worlds = server.worlds;

		for (WorldServer world : worlds) {
			world.tick();
		}

		if (!tickrate0) {
			TASmod.tickratechanger.pauseGame(false);
		}

		// Unlock loadstating
		state = SavestateState.WASLOADING;
	}

	/**
	 * Creates the savestate directory in case the user deletes it between
	 * savestates
	 */
	private void createSavestateDirectory() {
		logger.trace(LoggerMarkers.Savestate, "Creating savestate directory");
		if (!server.isDedicatedServer()) {
			savestateDirectory = new File(server.getDataDirectory() + File.separator + "saves" + File.separator + "savestates" + File.separator);
		} else {
			savestateDirectory = new File(server.getDataDirectory() + File.separator + "savestates" + File.separator);
		}
		if (!savestateDirectory.exists()) {
			savestateDirectory.mkdir();
		}
	}

	/**
	 * Refreshes the current savestate list and loads all indizes into {@link #indexList}
	 */
	private void refresh() {
		logger.trace(LoggerMarkers.Savestate, "Refreshing savestate list");
		indexList.clear();
		File[] files = savestateDirectory.listFiles(new FileFilter() {

			@Override
			public boolean accept(File pathname) {
				return pathname.getName().startsWith(server.getFolderName() + "-Savestate");
			}

		});
		int index = 0;
		for (File file : files) {
			try {
				Pattern patt = Pattern.compile("\\d+$");
				Matcher matcher = patt.matcher(file.getName());
				if (matcher.find()) {
					index = Integer.parseInt(matcher.group(0));
				} else {
					logger.warn(String.format("Could not process the savestate %s", file.getName()));
					continue;
				}
			} catch (NumberFormatException e) {
				logger.warn(String.format("Could not process the savestate %s", e.getMessage()));
				continue;
			}
			indexList.add(index);
		}
		Collections.sort(indexList);
		if (!indexList.isEmpty()) {
			latestIndex = indexList.get(indexList.size() - 1);
		} else {
			latestIndex = 0;
		}
	}

	/**
	 * @param index The index of the savestate file that we want to get
	 * @return The file of the savestate from the specified index
	 */
	private File getSavestateFile(int index) {
		return new File(savestateDirectory, getSavestateName(index));
	}

	/**
	 * @param index The index of the savestate file that we want to get
	 * @return The savestate name without any paths
	 */
	private String getSavestateName(int index) {
		return server.getFolderName() + "-Savestate" + index;
	}

	/**
	 * Deletes the specified savestate
	 * 
	 * @param index The index of the savestate that should be deleted
	 * @throws SavestateDeleteException
	 */
	public void deleteSavestate(int index) throws SavestateDeleteException {
		logger.warn(LoggerMarkers.Savestate, "Deleting savestate {}", index);
		if (state == SavestateState.SAVING) {
			throw new SavestateDeleteException("A savestating operation is already being carried out");
		}
		if (state == SavestateState.LOADING) {
			throw new SavestateDeleteException("A loadstate operation is being carried out");
		}
		if (index < 0) {
			throw new SavestateDeleteException("Cannot delete the negative indexes");
		}
		if(index == 0) {
			throw new SavestateDeleteException("Cannot delete protected savestate 0");
		}
		File toDelete = getSavestateFile(index);
		if (toDelete.exists()) {
			try {
				FileUtils.deleteDirectory(toDelete);
			} catch (IOException e) {
				e.printStackTrace();
				throw new SavestateDeleteException("Something went wrong while trying to delete the savestate " + index);
			}
		} else {
			throw new SavestateDeleteException(TextFormatting.YELLOW + "Savestate " + index + " doesn't exist, so it can't be deleted");
		}
		refresh();
		if (!indexList.contains(currentIndex)) {
			setCurrentIndex(latestIndex);
		}
		// Send a notification that the savestate has been deleted
		server.getPlayerList().sendMessage(new TextComponentString(TextFormatting.GREEN + "Savestate " + index + " deleted"));
	}

	/**
	 * Deletes savestates in a range from "from" to "to"
	 * 
	 * @param from
	 * @param to   (inclusive)
	 * @throws SavestateDeleteException
	 */
	public void deleteSavestate(int from, int to) throws SavestateDeleteException {
		logger.warn(LoggerMarkers.Savestate, "Deleting multiple savestates from {} to {}", from, to);
		if (state == SavestateState.SAVING) {
			throw new SavestateDeleteException("A savestating operation is already being carried out");
		}
		if (state == SavestateState.LOADING) {
			throw new SavestateDeleteException("A loadstate operation is being carried out");
		}
		if (from >= to) {
			throw new SavestateDeleteException("Can't delete amounts that are negative or 0");
		}
		for (int i = from; i <= to; i++) {
//			System.out.println("Would've deleted savestate: "+i);
			try {
				deleteSavestate(i);
			} catch (SavestateDeleteException e) {
				server.getPlayerList().sendMessage(new TextComponentString(TextFormatting.RED + e.getMessage()));
				continue;
			}
		}
	}

	/**
	 * @return A list of index numbers as string in the form of: <code>"0, 1, 2, 3"</code>
	 */
	public String getIndexesAsString() {
		refresh();
		String out = "";
		for (int i : indexList) {
			out = out.concat(" " + i + (i == indexList.size() - 1 ? "" : ","));
		}
		return out;
	}

	/**
	 * Saves the current index to the current world-folder (not the savestate
	 * folder)
	 * 
	 * @param legacy If the data file should only store the index, since it comes from a legacy file format
	 */
	private void saveSavestateDataFile(boolean legacy) {
		logger.trace(LoggerMarkers.Savestate, "Saving savestate data file");
		File tasmodDir = new File(savestateDirectory, "../" + server.getFolderName() + "/tasmod/");
		if (!tasmodDir.exists()) {
			tasmodDir.mkdir();
		}
		File savestateDat = new File(tasmodDir, "savestateData.txt");
		
		if(savestateDat.exists()) {
			savestateDat.delete();
		}
		
		SavestateDataFile file = new SavestateDataFile();
		
		file.set(DataValues.INDEX, Integer.toString(currentIndex));
		
		if(!legacy) {
			if(TASmod.ktrngHandler.isLoaded()) {
				file.set(DataValues.SEED, Long.toString(TASmod.ktrngHandler.getGlobalSeedServer()));
			}
		}
		
		file.save(savestateDat);
	}

	/**
	 * Loads information from savestateData.txt
	 * <p>
	 * This loads everything except the index, since that is loaded when the world is loaded
	 */
	private void loadSavestateDataFile() {
		logger.trace(LoggerMarkers.Savestate, "Loading savestate data file");
		File tasmodDir = new File(savestateDirectory, "../" + server.getFolderName() + "/tasmod/");
		File savestateDat = new File(tasmodDir, "savestateData.txt");
		
		if(!savestateDat.exists()) {
			return;
		}
		
		SavestateDataFile datafile = new SavestateDataFile();
		
		datafile.load(savestateDat);
		
		if(TASmod.ktrngHandler.isLoaded()) {
			String seedString = datafile.get(DataValues.SEED);
			if(seedString != null) {
				TASmod.ktrngHandler.sendGlobalSeedToServer(Long.parseLong(seedString));
			} else {
				logger.warn("KTRNG seed not loaded because it was not found in savestateData.txt!");
			}
		}
	}
	
	/**
	 * Loads the current index to the current world-folder (not the savestate
	 * folder)
	 * <p>
	 * This ensures that the server knows the current index when loading the world
	 */
	public void loadCurrentIndexFromFile() {
		logger.trace(LoggerMarkers.Savestate, "Loading current index from file");
		int index = -1;
		File tasmodDir = new File(savestateDirectory, "../" + server.getFolderName() + "/tasmod/");
		if (!tasmodDir.exists()) {
			tasmodDir.mkdir();
		}
		
		File savestateDat = new File(tasmodDir, "savestate.data");
		if(savestateDat.exists()) {
			index = legacyIndexFile(savestateDat);
			setCurrentIndex(index);
			saveSavestateDataFile(true);
			savestateDat.delete();
			return;
		}
		
		savestateDat = new File(tasmodDir, "savestateData.txt");
		if(savestateDat.exists()) {
			SavestateDataFile file = new SavestateDataFile();
			file.load(savestateDat);
			
			index = Integer.parseInt(file.get(DataValues.INDEX));
			
			setCurrentIndex(index);
		}
	}
	
	private void setCurrentIndex(int index) {
		if (index < 0) {
			currentIndex = latestIndex;
		} else {
			currentIndex = index;
		}
		logger.debug(LoggerMarkers.Savestate, "Setting the savestate index to {}", currentIndex);
	}

	public int getCurrentIndex() {
		return currentIndex;
	}

	@Override
	public void onLoadstateComplete() {
		TASmod.LOGGER.trace(LoggerMarkers.Event, "Running loadstate complete event");
		PlayerList playerList = TASmod.getServerInstance().getPlayerList();
		for (EntityPlayerMP player : playerList.getPlayers()) {
			NBTTagCompound nbttagcompound = playerList.readPlayerDataFromFile(player);
			PlayerHandler.reattachEntityToPlayer(nbttagcompound, player.getServerWorld(), player);
		}
		// Updating redstone component timers to the new world time (#136)
		ChunkHandler.updateWorldServerTickListEntries();
	}

	@Environment(EnvType.CLIENT)
	public static void playerLoadSavestateEventClient() {
		SavestateHandlerClient.addPlayerToClientChunk(Minecraft.getMinecraft().player);
	}
	
	private int legacyIndexFile(File savestateDat) {
		int index = -1;
		List<String> lines = new ArrayList<String>();
		try {
			lines = FileUtils.readLines(savestateDat, StandardCharsets.UTF_8);
		} catch (IOException e) {
			logger.warn("No savestate.data file found in current world folder, ignoring it");
		}
		if (!lines.isEmpty()) {
			for (String line : lines) {
				if (line.startsWith("currentIndex=")) {
					try {
						index = Integer.parseInt(line.split("=")[1]);
					} catch (NumberFormatException e) {
						e.printStackTrace();
					}
				}
			}
		}
		return index;
	}
	
	public static enum SavestateState {
		SAVING,
		LOADING,
		WASLOADING,
		NONE
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
	public void onServerPacket(PacketID id, ByteBuffer buf, UUID clientID) throws PacketNotImplementedException, WrongSideException, Exception {
		// TODO Permissions
		TASmodPackets packet = (TASmodPackets) id;
		
		EntityPlayerMP player = TASmod.getServerInstance().getPlayerList().getPlayerByUUID(clientID);
		Integer index = null;
		
		
		switch (packet) {
		case SAVESTATE_SAVE:
			index = TASmodBufferBuilder.readInt(buf);
//			if (player!=null && !player.canUseCommand(2, "savestate")) {
//				player.sendMessage(new TextComponentString(TextFormatting.RED+"You don't have permission to do that"));
//				return;
//			}
			try {
				TASmod.savestateHandler.saveState(index, true);
			} catch (SavestateException e) {
				if(player!=null)
					player.sendMessage(new TextComponentString(TextFormatting.RED+"Failed to create a savestate: "+ e.getMessage()));

				TASmod.LOGGER.error(LoggerMarkers.Savestate, "Failed to create a savestate: "+ e.getMessage());
			} catch (Exception e) {
				if(player!=null)
					player.sendMessage(new TextComponentString(TextFormatting.RED+"Failed to create a savestate: "+ e.getCause().toString()));
				
				TASmod.LOGGER.error(e);
			} finally {
				TASmod.savestateHandler.state=SavestateState.NONE;
			}
			break;
			
		case SAVESTATE_LOAD:
			break;
		case SAVESTATE_PLAYER:
			double x = TASmodBufferBuilder.readDouble(buf);
			double y = TASmodBufferBuilder.readDouble(buf);
			double z = TASmodBufferBuilder.readDouble(buf);
			float rx = TASmodBufferBuilder.readFloat(buf);
			float ry = TASmodBufferBuilder.readFloat(buf);
			float rz = TASmodBufferBuilder.readFloat(buf);
			boolean sprinting = TASmodBufferBuilder.readBoolean(buf);
			float jumpMovementVector = TASmodBufferBuilder.readFloat(buf);
			PlayerHandler.getMotion().put(player, new PlayerHandler.Saver(x, y, z, rx, ry, rz, sprinting, jumpMovementVector));
			break;
		case SAVESTATE_SCREEN:
		case SAVESTATE_UNLOAD_CHUNKS:
			throw new WrongSideException(id, Side.SERVER);
		default:
			throw new PacketNotImplementedException(packet, this.getClass());
		}
	}

	/**
	 * Contains player related classes
	 */
	public static class PlayerHandler {

		public static class Saver {
			private double clientX;
			private double clientY;
			private double clientZ;
			private float clientrX;
			private float clientrY;
			private float clientrZ;
			private boolean sprinting;
			private float jumpMovementVector;
		
			public Saver(double x, double y, double z, float rx, float ry, float rz, boolean sprinting, float jumpMovementVector) {
				clientX = x;
				clientY = y;
				clientZ = z;
				clientrX = rx;
				clientrY = ry;
				clientrZ = rz;
				this.sprinting = sprinting;
				this.jumpMovementVector = jumpMovementVector;
			}
		
			public double getClientX() {
				return clientX;
			}
		
			public double getClientY() {
				return clientY;
			}
		
			public double getClientZ() {
				return clientZ;
			}
		
			public float getClientrX() {
				return clientrX;
			}
		
			public float getClientrY() {
				return clientrY;
			}
		
			public float getClientrZ() {
				return clientrZ;
			}
		
			public boolean isSprinting() {
				return sprinting;
			}
		
			public float getJumpMovementVector() {
				return jumpMovementVector;
			}
		}

		/**
		 * Tries to reattach the player to an entity, if the player was riding it it while savestating.
		 * 
		 * Side: Server
		 * @param nbttagcompound where the ridden entity is saved
		 * @param worldserver that needs to spawn the entity
		 * @param playerIn that needs to ride the entity
		 */
		public static void reattachEntityToPlayer(NBTTagCompound nbttagcompound, World worldserver, Entity playerIn) {
			if (nbttagcompound != null && nbttagcompound.hasKey("RootVehicle", 10))
		    {
		        NBTTagCompound nbttagcompound1 = nbttagcompound.getCompoundTag("RootVehicle");
		        Entity entity1 = AnvilChunkLoader.readWorldEntity(nbttagcompound1.getCompoundTag("Entity"), worldserver, true);
		        
		        
		        if(entity1==null) {
		        	for (Entity entity : worldserver.loadedEntityList) {
		        		if(entity.getUniqueID().equals(nbttagcompound1.getUniqueId("Attach"))) entity1=entity;
		        	}
		        }
		        
		        if (entity1 != null)
		        {
		            UUID uuid = nbttagcompound1.getUniqueId("Attach");
		
		            if (entity1.getUniqueID().equals(uuid))
		            {
		                playerIn.startRiding(entity1, true);
		            }
		            else
		            {
		                for (Entity entity : entity1.getRecursivePassengers())
		                {
		                    if (entity.getUniqueID().equals(uuid))
		                    {
		                        playerIn.startRiding(entity, true);
		                        break;
		                    }
		                }
		            }
		
		            if (!playerIn.isRiding())
		            {
		                TASmod.LOGGER.warn("Couldn't reattach entity to player");
		                worldserver.removeEntityDangerously(entity1);
		
		                for (Entity entity2 : entity1.getRecursivePassengers())
		                {
		                    worldserver.removeEntityDangerously(entity2);
		                }
		            }
		        }
		    }
			else {
				if(playerIn.isRiding()) {
					playerIn.dismountRidingEntity();
				}
			}
		}

		/**
		 * Loads all worlds and players from the disk. Also sends the playerdata to the client in {@linkplain SavestatePlayerLoadingPacketHandler}
		 * 
		 * Side: Server
		 */
		public static void loadAndSendMotionToPlayer(MinecraftServer server) {
			
			List<EntityPlayerMP> players=server.getPlayerList().getPlayers();
			PlayerList list=server.getPlayerList();
			
			WorldServer[] worlds=server.worlds;
			for (WorldServer world : worlds) {
				WorldInfo info=world.getSaveHandler().loadWorldInfo();
				world.worldInfo = info;
			}
			for(EntityPlayerMP player : players) {
				
				int dimensionPrev=player.dimension;
				
				NBTTagCompound nbttagcompound = server.getPlayerList().readPlayerDataFromFile(player);
				
				int dimensionNow=0;
				if (nbttagcompound.hasKey("Dimension"))
		        {
		            dimensionNow = nbttagcompound.getInteger("Dimension");
		        }
				
				if(dimensionNow!=dimensionPrev) {
					list.changePlayerDimension(player, dimensionNow);
				}else {
					player.getServerWorld().unloadedEntityList.remove(player);
				}
				
				player.readFromNBT(nbttagcompound);
				
				try {
					TASmod.server.sendTo(player.getUniqueID(), new TASmodBufferBuilder(TASmodPackets.SAVESTATE_PLAYER).writeNBTTagCompound(nbttagcompound));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		public static void requestMotionFromClient() {
			TASmod.LOGGER.trace(LoggerMarkers.Savestate, "Request motion from client");
			PlayerHandler.motion.clear();
			try {
				// request client motion
				TASmod.server.sendToAll(new TASmodBufferBuilder(TASmodPackets.SAVESTATE_PLAYER));
			} catch (Exception e) {
				TASmod.LOGGER.error("Unable to send packet to all clients:", e);
			}
		
			// TODO: is this still necessary?
			int i = 1;
			while (PlayerHandler.motion.size() != TASmod.getServerInstance().getPlayerList().getCurrentPlayerCount()) {
				i++;
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				if(i % 30 == 1) {
					TASmod.LOGGER.debug(LoggerMarkers.Savestate, "Resending motion packet");
					try {
						// request client motion
						TASmod.server.sendToAll(new TASmodBufferBuilder(TASmodPackets.SAVESTATE_PLAYER));
					} catch (Exception e) {
						TASmod.LOGGER.error("Unable to send packet to all clients:", e);
					}
				}
				if (i == 1000) {
					TASmod.LOGGER.warn(LoggerMarkers.Savestate, "Client motion timed out!");
					break;
				}
		
			}
		}

		public static Map<EntityPlayerMP, PlayerHandler.Saver> getMotion() {
			return PlayerHandler.motion;
		}

		public static Map<EntityPlayerMP, PlayerHandler.Saver> motion = Maps.<EntityPlayerMP, PlayerHandler.Saver>newHashMap();
		
	}
	
	/**
	 * Contains static chunk actions, which can be triggered indiviadually for testing
	 */
	public static class ChunkHandler{

		/**
		 * Updates ticklist entries to the current world time, allowing them to not be stuck in a pressed state #136
		 */
		public static void updateWorldServerTickListEntries() {
			TASmod.LOGGER.trace(LoggerMarkers.Savestate, "Update server tick list entries");
			MinecraftServer server=TASmod.getServerInstance();
			for (WorldServer world : server.worlds) {
		        for (NextTickListEntry nextticklistentry : world.pendingTickListEntriesHashSet) {
		        	nextticklistentry.setScheduledTime(world.getTotalWorldTime());
		        }
			}
		}

		/**
		 * Just like {@link SavestateHandlerClient#addPlayerToClientChunk(EntityPlayer)}, adds the player to the chunk on the server.
		 * This prevents the player from being able to place block inside of him
		 * 
		 * Side: Server
		 */
		public static void addPlayerToServerChunk(EntityPlayerMP player) {
			TASmod.LOGGER.trace(LoggerMarkers.Savestate, "Add player {} to server chunk", player.getName());
			int i = MathHelper.floor(player.posX / 16.0D);
			int j = MathHelper.floor(player.posZ / 16.0D);
			WorldServer world = player.getServerWorld();
			Chunk chunk = world.getChunkFromChunkCoords(i, j);
			for (int k = 0; k < chunk.getEntityLists().length; k++) {
				if (chunk.getEntityLists()[k].contains(player)) {
					return;
				}
			}
			chunk.addEntity(player);
		}

		/**
		 * The session lock is minecrafts failsafe system when it comes to saving. It prevents writing to the world folder from 2 different locations <br>
		 * <br>
		 * That works by storing system time to a session.lock file, when the server started. The integrated server also saves the time when it started in a variable. <br>
		 * <br>
		 * Those two times are then compared every time minecraft tries to save and fails if the times are different.<br>
		 * <br>
		 * Since we never close the integrated server, but copy an "old" session.lock file with the savestate, the session.lock will always mismatch.<br>
		 * Thus we need to update the session lock once the loadstating is completed<br>
		 * <br>
		 * TLDR:<br>
		 * Updates the session lock to allow for vanilla saving again<br>
		 * <br>
		 * Side: Server
		 */
		public static void updateSessionLock(MinecraftServer server) {
			TASmod.LOGGER.trace(LoggerMarkers.Savestate, "Update the session lock");
			WorldServer[] worlds=server.worlds;
			for(WorldServer world : worlds) {
				((SaveHandler) world.getSaveHandler()).setSessionLock();
			}
		}

		/**
		 * Tells the save handler to save all changes to disk and remove all references to the region files, making them editable on disc<br>
		 * <br>
		 * Side: Server
		 */
		public static void flushSaveHandler(MinecraftServer server) {
			TASmod.LOGGER.trace(LoggerMarkers.Savestate, "Flush the save handler");
			//Vanilla
			WorldServer[] worlds=server.worlds;
			for(WorldServer world : worlds) {
				world.getSaveHandler().flush();
			}
		}

		/**
		 * The player chunk map keeps track of which chunks need to be sent to the client. <br>
		 * This adds the player to the chunk map so the server knows it can send the information to the client<br>
		 * <br>
		 * Side: Server
		 * @see disconnectPlayersFromChunkMap
		 */
		public static void addPlayersToChunkMap(MinecraftServer server) {
			List<EntityPlayerMP> players=server.getPlayerList().getPlayers();
			//Vanilla
			WorldServer[] worlds=server.worlds;
			for (EntityPlayerMP player : players) {
				TASmod.LOGGER.trace(LoggerMarkers.Savestate, "Add player {} to the chunk map", player.getName());
				switch (player.dimension) {
				case -1:
					worlds[1].getPlayerChunkMap().addPlayer(player);
					worlds[1].getChunkProvider().provideChunk((int)player.posX >> 4, (int)player.posZ >> 4);
					break;
				case 0:
					worlds[0].getPlayerChunkMap().addPlayer(player);
					worlds[0].getChunkProvider().provideChunk((int)player.posX >> 4, (int)player.posZ >> 4);
					break;
				case 1:
					worlds[2].getPlayerChunkMap().addPlayer(player);
					worlds[2].getChunkProvider().provideChunk((int)player.posX >> 4, (int)player.posZ >> 4);
					break;
				}
			}
		}

		/**
		 * The player chunk map keeps track of which chunks need to be sent to the client. <br>
		 * Removing the player stops the server from sending chunks to the client.<br>
		 * <br>
		 * Side: Server
		 * @see SavestatesChunkControl#addPlayersToChunkMap()
		 */
		public static void disconnectPlayersFromChunkMap(MinecraftServer server) {
			List<EntityPlayerMP> players=server.getPlayerList().getPlayers();
			//Vanilla
			WorldServer[] worlds=server.worlds;
			for (WorldServer world : worlds) {
				for (EntityPlayerMP player : players) {
					TASmod.LOGGER.trace(LoggerMarkers.Savestate, "Disconnect player {} from the chunk map", player.getName());
					world.getPlayerChunkMap().removePlayer(player);
				}
			}
		}

		/**
		 * Unloads all chunks on the server<br>
		 * <br>
		 * Side: Server
		 * @see MixinChunkProviderServer#unloadAllChunks()
		 */
		public static void unloadAllServerChunks(MinecraftServer server) {
			TASmod.LOGGER.trace(LoggerMarkers.Savestate, "Unloading all server chunks");
			//Vanilla
			WorldServer[] worlds=server.worlds;
			
			for (WorldServer world:worlds) {
				ChunkProviderServer chunkProvider=world.getChunkProvider();
				
				((ChunkProviderDuck)chunkProvider).unloadAllChunks();
			}
			
		}
	}
}
