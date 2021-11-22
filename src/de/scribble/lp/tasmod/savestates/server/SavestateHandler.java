package de.scribble.lp.tasmod.savestates.server;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;

import de.scribble.lp.tasmod.CommonProxy;
import de.scribble.lp.tasmod.TASmod;
import de.scribble.lp.tasmod.savestates.client.InputSavestatesPacket;
import de.scribble.lp.tasmod.savestates.server.chunkloading.SavestatesChunkControl;
import de.scribble.lp.tasmod.savestates.server.exceptions.LoadstateException;
import de.scribble.lp.tasmod.savestates.server.exceptions.SavestateDeleteException;
import de.scribble.lp.tasmod.savestates.server.exceptions.SavestateException;
import de.scribble.lp.tasmod.savestates.server.motion.ClientMotionServer;
import de.scribble.lp.tasmod.savestates.server.playerloading.SavestatePlayerLoading;
import de.scribble.lp.tasmod.tickratechanger.TickrateChangerServer;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.storage.AnvilChunkLoader;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Creates and loads savestates on both client and server without closing the
 * world <br>
 * The old version that you may find in TASTools was heavily inspired by bspkrs'
 * <a href=
 * "https://www.curseforge.com/minecraft/mc-mods/worldstatecheckpoints">WorldStateCheckpoints</a>,
 * but this new version is completely self written.
 * 
 * @author ScribbleLP
 *
 */
public class SavestateHandler {

	private MinecraftServer server;
	private File savestateDirectory;

	public SavestateState state = SavestateState.NONE;

	private final List<Integer> indexList = new ArrayList<>();

	private int latestIndex = 0;
	private int currentIndex;

	/**
	 * Creates a savestate handler on the specified server
	 * 
	 * @param The server that should store the savestates
	 */
	public SavestateHandler(MinecraftServer server) {
		this.server = server;
		createSavestateDirectory();
		refresh();
		loadCurrentIndexFromFile();
	}

	/**
	 * Creates a copy of the world that is currently being played and saves it in
	 * .minecraft/saves/savestates/worldname-Savestate[{@linkplain #currentIndex}+1]
	 * <br>
	 * <br>
	 * Side: Server
	 * 
	 * @throws SavestateException
	 * @throws IOException
	 */
	public void saveState() throws SavestateException, IOException {
		saveState(-1, true);
	}

	/**
	 * Creates a copy of the world that is currently being played and saves it in
	 * .minecraft/saves/savestates/worldname-Savestate[savestateIndex] <br>
	 * <br>
	 * Side: Server
	 * 
	 * @param savestateIndex The index where the mod will save the savestate.
	 *                       index<0 if it should save it in the next index from
	 *                       the currentindex
	 * @param tickrate0 When true: Set's the game to tickrate 0 after creating a savestate
	 * @throws SavestateException
	 * @throws IOException
	 */
	public void saveState(int savestateIndex, boolean tickrate0) throws SavestateException, IOException {
		if (state == SavestateState.SAVING) {
			throw new SavestateException("A savestating operation is already being carried out");
		}
		if (state == SavestateState.LOADING) {
			throw new SavestateException("A loadstate operation is being carried out");
		}
		// Lock savestating and loadstating
		state = SavestateState.SAVING;

		// Create a directory just in case
		createSavestateDirectory();

		// Enable tickrate 0
		TickrateChangerServer.pauseGame(true);

		// Update the server variable
		server = TASmod.getServerInstance();

		// Get the motion from the client
		ClientMotionServer.requestMotionFromClient();

		// Save the world!
		server.getPlayerList().saveAllPlayerData();
		server.saveAllWorlds(true);

		// Refreshing the index list
		refresh();

		// Setting the current index depending on the savestateIndex.
		if (savestateIndex < 0) {
			setCurrentIndex(currentIndex + 1); // If the savestateIndex <= 0, create a savestate at currentIndex+1
		} else {
			setCurrentIndex(savestateIndex);
		}

		// Get the current and target directory for copying
		String worldname = server.getFolderName();
		File currentfolder = new File(savestateDirectory, ".." + File.separator + worldname);
		File targetfolder = getSavestateFile(currentIndex);

		if (targetfolder.exists()) {
			TASmod.logger.warn("WARNING! Overwriting the savestate with the index {}", currentIndex);
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
			CommonProxy.NETWORK.sendToAll(new InputSavestatesPacket(true, getSavestateName(currentIndex)));
		}

		// Wait for the chunkloader to save the game
		for (WorldServer world : server.worlds) {
			AnvilChunkLoader chunkloader = (AnvilChunkLoader) world.getChunkProvider().chunkLoader;
			while (chunkloader.getPendingSaveCount() > 0) {
			}
		}

		// Copy the directory
		FileUtils.copyDirectory(currentfolder, targetfolder);

		// Incrementing info file
		SavestateTrackerFile tracker = new SavestateTrackerFile(new File(savestateDirectory, worldname + "-info.txt"));
		tracker.increaseSavestates();
		tracker.saveFile();

		saveCurrentIndexToFile();

		// Send a notification that the savestate has been loaded
		server.getPlayerList().sendMessage(new TextComponentString(TextFormatting.GREEN + "Savestate " + currentIndex + " saved"));

		// Close the GuiSavestateScreen on the client
		CommonProxy.NETWORK.sendToAll(new SavestatePacket());

		if (!tickrate0) {
			TickrateChangerServer.pauseGame(false);
		}

		// Unlock savestating
		state = SavestateState.NONE;
	}

	/**
	 * Loads the latest savestate at {@linkplain #currentIndex}
	 * .minecraft/saves/savestates/worldname-Savestate[{@linkplain #currentIndex}]
	 * 
	 * Side: Server
	 * 
	 * @throws LoadstateException
	 * @throws IOException
	 */
	public void loadState() throws LoadstateException, IOException {
		loadState(-1, true);
	}

	/**
	 * Loads the latest savestate it can find in
	 * .minecraft/saves/savestates/worldname-Savestate
	 * 
	 * Side: Server
	 * 
	 * @param savestateIndex The index where the mod will load the savestate.
	 *                       index<0 if it should load the currentindex
	 * @param tickrate0 When true: Set's the game to tickrate 0 after creating a savestate
	 * @throws LoadstateException
	 * @throws IOException
	 */
	public void loadState(int savestateIndex, boolean tickrate0) throws LoadstateException, IOException {
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
		TickrateChangerServer.pauseGame(true);

		// Update the server instance
		server = TASmod.getServerInstance();

		refresh();

		int indexToLoad = savestateIndex < 0 ? currentIndex : savestateIndex;

		if (!getSavestateFile(indexToLoad).exists()) {
			throw new LoadstateException("Savestate " + indexToLoad + " doesn't exist");
		} else {
			setCurrentIndex(indexToLoad);
		}

		// Get the current and target directory for copying
		String worldname = server.getFolderName();
		File currentfolder = new File(savestateDirectory, ".." + File.separator + worldname);
		File targetfolder = getSavestateFile(currentIndex);

		/*
		 * Prevents loading an InputSavestate when loading index 0 (Index 0 is the
		 * savestate when starting a recording. Not doing this will load an empty
		 * InputSavestate)
		 */
		if (savestateIndex != 0) {
			// Load savestate on the client
			CommonProxy.NETWORK.sendToAll(new InputSavestatesPacket(false, getSavestateName(currentIndex)));
		}

		// Disabeling level saving for all worlds in case the auto save kicks in during
		// world unload
		for (WorldServer world : server.worlds) {
			world.disableLevelSaving = true;
		}

		// Unload chunks on the client
		CommonProxy.NETWORK.sendToAll(new LoadstatePacket());

		// Unload chunks on the server
		SavestatesChunkControl.disconnectPlayersFromChunkMap();
		SavestatesChunkControl.unloadAllServerChunks();
		SavestatesChunkControl.flushSaveHandler();

		// Delete and copy directories
		FileUtils.deleteDirectory(currentfolder);
		FileUtils.copyDirectory(targetfolder, currentfolder);

		// Update the player and the client
		SavestatePlayerLoading.loadAndSendMotionToPlayer();
		// Update the session.lock file so minecraft behaves and saves the world
		SavestatesChunkControl.updateSessionLock();
		// Load the chunks and send them to the client
		SavestatesChunkControl.addPlayersToChunkMap();

		// Enable level saving again
		for (WorldServer world : server.worlds) {
			world.disableLevelSaving = false;
		}

		// Incrementing info file
		SavestateTrackerFile tracker = new SavestateTrackerFile(new File(savestateDirectory, worldname + "-info.txt"));
		tracker.increaseRerecords();
		tracker.saveFile();

		saveCurrentIndexToFile();

		// Send a notification that the savestate has been loaded
		server.getPlayerList().sendMessage(new TextComponentString(TextFormatting.GREEN + "Savestate " + currentIndex + " loaded"));

		WorldServer[] worlds = DimensionManager.getWorlds();

		for (WorldServer world : worlds) {
			world.tick();
		}

		if (!tickrate0) {
			TickrateChangerServer.pauseGame(false);
		}

		// Unlock loadstating
		state = SavestateState.WASLOADING;
	}

	/**
	 * Creates the savestate directory in case the user deletes it between
	 * savestates
	 */
	private void createSavestateDirectory() {
		if (!server.isDedicatedServer()) {
			savestateDirectory = new File(server.getDataDirectory() + File.separator + "saves" + File.separator + "savestates" + File.separator);
		} else {
			savestateDirectory = new File(server.getDataDirectory() + File.separator + "savestates" + File.separator);
		}
		if (!savestateDirectory.exists()) {
			savestateDirectory.mkdir();
		}
	}

	private void refresh() {
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
					TASmod.logger.warn(String.format("Could not process the savestate %s", file.getName()));
					continue;
				}
			} catch (NumberFormatException e) {
				TASmod.logger.warn(String.format("Could not process the savestate %s", e.getMessage()));
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
		if (state == SavestateState.SAVING) {
			throw new SavestateDeleteException("A savestating operation is already being carried out");
		}
		if (state == SavestateState.LOADING) {
			throw new SavestateDeleteException("A loadstate operation is being carried out");
		}
		if (index < 0) {
			throw new SavestateDeleteException("Cannot delete the negative indexes");
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
	 * @param to   (exclusive)
	 * @throws SavestateDeleteException
	 */
	public void deleteSavestate(int from, int to) throws SavestateDeleteException {
		if (state == SavestateState.SAVING) {
			throw new SavestateDeleteException("A savestating operation is already being carried out");
		}
		if (state == SavestateState.LOADING) {
			throw new SavestateDeleteException("A loadstate operation is being carried out");
		}
		if (from >= to) {
			throw new SavestateDeleteException("Can't delete amounts that are negative or 0");
		}
		for (int i = from; i < to; i++) {
			if (i == 0) {
				continue;
			}
			try {
				deleteSavestate(i);
			} catch (SavestateDeleteException e) {
				server.getPlayerList().sendMessage(new TextComponentString(TextFormatting.RED + e.getMessage()));
				continue;
			}
		}
	}

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
	 */
	private void saveCurrentIndexToFile() {
		File tasmodDir = new File(savestateDirectory, "../" + server.getFolderName() + "/tasmod/");
		if (!tasmodDir.exists()) {
			tasmodDir.mkdir();
		}
		File savestateDat = new File(tasmodDir, "savestate.data");
		List<String> lines = new ArrayList<String>();
		lines.add("currentIndex=" + currentIndex);
		try {
			FileUtils.writeLines(savestateDat, lines);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Loads the current index to the current world-folder (not the savestate
	 * folder)
	 */
	public void loadCurrentIndexFromFile() {
		int index = -1;
		List<String> lines = new ArrayList<String>();
		File tasmodDir = new File(savestateDirectory, "../" + server.getFolderName() + "/tasmod/");
		if (!tasmodDir.exists()) {
			tasmodDir.mkdir();
		}
		File savestateDat = new File(tasmodDir, "savestate.data");
		try {
			lines = FileUtils.readLines(savestateDat, StandardCharsets.UTF_8);
		} catch (IOException e) {
			TASmod.logger.warn("No savestate.data file found in current world folder, ignoring it");
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
		setCurrentIndex(index);
	}

	private void setCurrentIndex(int index) {
		if (index < 0) {
			currentIndex = latestIndex;
		} else {
			currentIndex = index;
		}
		TASmod.logger.info("Setting the savestate index to {}", currentIndex);
	}

	public int getCurrentIndex() {
		return currentIndex;
	}

	/**
	 * Event, that gets executed after a loadstate operation was carried out, get's
	 * called on the server side
	 */
	public static void playerLoadSavestateEventServer() {
		PlayerList playerList = TASmod.getServerInstance().getPlayerList();
		for (EntityPlayerMP player : playerList.getPlayers()) {
			NBTTagCompound nbttagcompound = playerList.getPlayerNBT(player);
			SavestatePlayerLoading.reattachEntityToPlayer(nbttagcompound, player.getServerWorld(), player);
		}
	}

	@SideOnly(Side.CLIENT)
	public static void playerLoadSavestateEventClient() {
		SavestatesChunkControl.addPlayerToChunk(Minecraft.getMinecraft().player);
	}
}