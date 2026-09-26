package com.minecrafttas.tasmod.savestates;

import static com.minecrafttas.tasmod.TASmod.LOGGER;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.logging.log4j.Logger;

import com.minecrafttas.tasmod.mctcommon.events.EventListenerRegistry;
import com.minecrafttas.tasmod.mctcommon.networking.Client.Side;
import com.minecrafttas.tasmod.mctcommon.networking.exception.PacketNotImplementedException;
import com.minecrafttas.tasmod.mctcommon.networking.exception.WrongSideException;
import com.minecrafttas.tasmod.mctcommon.networking.interfaces.PacketID;
import com.minecrafttas.tasmod.mctcommon.networking.interfaces.ServerPacketHandler;
import com.minecrafttas.tasmod.TASmod;
import com.minecrafttas.tasmod.commands.CommandSavestate;
import com.minecrafttas.tasmod.events.EventSavestate;
import com.minecrafttas.tasmod.mixin.savestates.AccessorAnvilChunkLoader;
import com.minecrafttas.tasmod.networking.TASmodBufferBuilder;
import com.minecrafttas.tasmod.registries.TASmodPackets;
import com.minecrafttas.tasmod.savestates.SavestateIndexer.DeletionRunnable;
import com.minecrafttas.tasmod.savestates.SavestateIndexer.ErrorRunnable;
import com.minecrafttas.tasmod.savestates.SavestateIndexer.SavestatePaths;
import com.minecrafttas.tasmod.savestates.exceptions.LoadstateException;
import com.minecrafttas.tasmod.savestates.exceptions.SavestateDeleteException;
import com.minecrafttas.tasmod.savestates.exceptions.SavestateException;
import com.minecrafttas.tasmod.savestates.handlers.SavestatePlayerHandlerServer;
import com.minecrafttas.tasmod.savestates.handlers.SavestateResourcePackHandler;
import com.minecrafttas.tasmod.savestates.handlers.SavestateTempHandler;
import com.minecrafttas.tasmod.savestates.handlers.SavestateWorldHandler;
import com.minecrafttas.tasmod.util.TASComponent;
import com.minecrafttas.tasmod.util.LoggerMarkers;
import com.minecrafttas.tasmod.util.Scheduler.Task;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.ChatFormatting;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.storage.RegionFileStorage;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.storage.LevelStorageSource;

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
public class SavestateHandlerServer implements ServerPacketHandler {

	/**
	 * The server instance
	 */
	private final MinecraftServer server;

	/**
	 * The current state of the handler to prevent savestating/loadstating twice
	 */
	private SavestateState state = SavestateState.NONE;

	/**
	 * Manages enumeration and location of savestates on the file system
	 */
	private SavestateIndexer indexer;

	/**
	 * Declutter class for handling player specific steps during saving/loading
	 */
	private final SavestatePlayerHandlerServer playerHandler;
	/**
	 * Declutter class for handling world specific steps during saving/loading
	 */
	private final SavestateWorldHandler worldHandler;
	/**
	 * Class for handling events associated with temporary savestates
	 */
	private final SavestateTempHandler tempSavestateHandler;

	/**
	 * The logger instance
	 */
	private final Logger logger;

	/**
	 * Creates a savestate handler on the specified server
	 * @param server The server that should store the savestates
	 * @param logger The logger instance
	 */
	public SavestateHandlerServer(MinecraftServer server, Logger logger) {
		this.server = server;
		this.logger = logger;

		this.playerHandler = new SavestatePlayerHandlerServer(server);
		this.worldHandler = new SavestateWorldHandler(server);
		this.tempSavestateHandler = new SavestateTempHandler(this, logger);

		createIndexer(server);
	}

	/**
	 * Creates a new savestate at index {@link SavestateIndexer#getCurrentIndex()} + 1 with a default name
	 * @param cb The {@link SavestateCallback}
	 * @param flags The {@link SavestateFlags}
	 * @throws SavestateException If a savestate can't be created
	 */
	public void saveState(SavestateCallback cb, SavestateFlags... flags) throws SavestateException {
		saveState(-1, null, cb, flags);
	}

	/**
	 * Creates a new savestate at the specified index with a default name
	 * @param index The index to save the savestate to
	 * @param cb The {@link SavestateCallback}
	 * @param flags The {@link SavestateFlags}
	 * @throws SavestateException If a savestate can't be created
	 */
	public void saveState(int index, SavestateCallback cb, SavestateFlags... flags) throws SavestateException {
		saveState(index, null, cb, flags);
	}

	/**
	 * Creates a new savestate at index {@link SavestateIndexer#getCurrentIndex()} + 1 with a specified name
	 * @param name The name that the savestate should have
	 * @param cb The {@link SavestateCallback}
	 * @param flags The {@link SavestateFlags}
	 * @throws SavestateException If a savestate can't be created
	 */
	public void saveState(String name, SavestateCallback cb, SavestateFlags... flags) throws SavestateException {
		saveState(-1, name, cb, flags);
	}

	/**
	 * Creates a new savestate at a specified index and name
	 * @param index The index to save the savestate to
	 * @param name The name that the savestate should have
	 * @param cb The {@link SavestateCallback}
	 * @param flags The {@link SavestateFlags}
	 * @throws SavestateException If a savestate can't be created
	 */
	public void saveState(int index, String name, SavestateCallback cb, SavestateFlags... flags) throws SavestateException {
		if (logger.isTraceEnabled()) {
			logger.trace(LoggerMarkers.Savestate, "SAVING a savestate with index {}. Flags: ", index, Arrays.stream(flags).map(Enum::toString).collect(Collectors.joining(",")));
		} else {
			logger.debug(LoggerMarkers.Savestate, "Creating new savestate");
		}

		if (state == SavestateState.SAVING) {
			throw new SavestateException("A savestating operation is already being carried out");
		}
		if (state == SavestateState.LOADING) {
			throw new SavestateException("A loadstate operation is being carried out");
		}

		// Open GuiSavestateScreen
		try {
			TASmod.server.sendToAll(new TASmodBufferBuilder(TASmodPackets.SAVESTATE_LOADING_SCREEN).writeEnum(SavestateState.SAVING));
		} catch (Exception e) {
			logger.catching(e);
		}

		// Lock savestating and loadstating
		state = SavestateState.SAVING;

		logger.trace("Create new savestate index via indexer");
		SavestatePaths paths = indexer.createSavestate(index, name, !SavestateFlags.BLOCK_CHANGE_INDEX.isBlocked(flags));

		if (paths.getSavestate().index == 0) {
			if (!ArrayUtils.contains(flags, SavestateFlags.BLOCK_CLIENT_SAVESTATE))
				flags = ArrayUtils.add(flags, SavestateFlags.BLOCK_CLIENT_SAVESTATE);
		}

		savestateInner(paths, cb, flags);
	}

	/**
	 * Creates a temporary savestate without an index
	 * @param cb The {@link SavestateCallback}
	 */
	public void saveStateTemp(SavestateCallback cb) {
		if (state == SavestateState.SAVING) {
			throw new SavestateException("A savestating operation is already being carried out");
		}
		if (state == SavestateState.LOADING) {
			throw new SavestateException("A loadstate operation is being carried out");
		}

		// Lock savestating and loadstating
		state = SavestateState.SAVING;

		SavestatePaths paths = indexer.createTempSavestate();
		SavestateFlags[] flags = new SavestateFlags[] { SavestateFlags.BLOCK_CLIENT_SAVESTATE, SavestateFlags.BLOCK_PAUSE_TICKRATE };
		savestateInner(paths, cb, flags);
		paths.getSavestate().save();
	}

	/**
	 * Inner savestate method using a series of steps to copy the folder from {@link SavestatePaths#getSourceFolder()} to {@link SavestatePaths#getTargetFolder()}
	 * @param paths The paths to use during copying
	 * @param cb The {@link SavestateCallback}
	 * @param flags The {@link SavestateFlags}
	 */
	private void savestateInner(SavestatePaths paths, SavestateCallback cb, SavestateFlags... flags) {

		// Enable tickrate 0
		TASmod.tickratechanger.pauseGame(true);

// Save the world!
		server.getPlayerList().saveAll();
		for (ServerLevel level : server.getAllLevels()) {
			level.save(null, true, false);
		}

		Path sourceFolder = paths.getSourceFolder();
		Path targetFolder = paths.getTargetFolder();
		Integer indexToSave = paths.getSavestate().index;
		logger.debug("Source: {}, Target: {}", sourceFolder, targetFolder);
		EventListenerRegistry.fireEvent(EventSavestate.EventServerSavestate.class, server, paths);

		if (Files.exists(targetFolder)) {
			if (indexToSave != null)
				logger.warn(LoggerMarkers.Savestate, "WARNING! Overwriting the savestate with the index {}", indexToSave);
			deleteFolder(targetFolder);
		}

		/*
		 * Prevents creating an InputSavestate when saving at index 0 (Index 0 is the
		 * savestate when starting a full recording)
		 */
		if (!SavestateFlags.BLOCK_CLIENT_SAVESTATE.isBlocked(flags)) {
			/*
			 * Send the name of the world to all players. This will make a savestate of the
			 * recording on the client with that name
			 */
			Path folder = paths.getSavestate().folder;
			Path savestateDir = folder.getParent().getParent();
			Path relativeFolder = savestateDir.relativize(folder);
			try {
				// savestate inputs client
				TASmod.server.sendToAll(new TASmodBufferBuilder(TASmodPackets.SAVESTATE_SAVE).writeString(relativeFolder.toString()));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

// Wait for the chunkloader to save the game
		for (ServerLevel world : server.getAllLevels()) {
			// Modern chunk saving is automatic, just wait a bit
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}

		// Copy the directory
		copyFolder(sourceFolder, targetFolder);

		// Unpause the game
		if (SavestateFlags.BLOCK_PAUSE_TICKRATE.isBlocked(flags)) {
			TASmod.tickratechanger.pauseGame(false);
		} else {
			try {
				TASmod.server.sendToAll(new TASmodBufferBuilder(TASmodPackets.TICKRATE_0_WARN));
			} catch (Exception e) {
				logger.catching(e);
			}
		}

		// Run the savestate callback
		if (cb != null)
			cb.invoke(paths);

		// Unlock savestating
		state = SavestateState.NONE;
	}

	/**
	 * Loads a savestate at the {@link SavestateIndexer#getCurrentIndex()}
	 * @param cb The {@link SavestateCallback}
	 * @param flags The {@link SavestateFlags}
	 * @throws LoadstateException If a savestate can't be loaded
	 */
	public void loadState(SavestateCallback cb, SavestateFlags... flags) throws LoadstateException {
		loadState(-1, cb, flags);
	}

	/**
	 * Loads a savestate at the specified index
	 * @param index The index to load from
	 * @param cb The {@link SavestateCallback}
	 * @param flags The {@link SavestateFlags}
	 * @throws LoadstateException If a savestate can't be loaded
	 */
	public void loadState(int index, SavestateCallback cb, SavestateFlags... flags) throws LoadstateException {
		if (logger.isTraceEnabled()) {
			logger.trace(LoggerMarkers.Savestate, "LOADING a savestate with index {}, ", index, Arrays.stream(flags).map(Enum::toString).collect(Collectors.joining(",")));
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

		// Open GuiSavestateScreen
		try {
			TASmod.server.sendToAll(new TASmodBufferBuilder(TASmodPackets.SAVESTATE_LOADING_SCREEN).writeEnum(SavestateState.LOADING));
		} catch (Exception e) {
			logger.catching(e);
		}

		// Get the current and target directory for copying
		logger.trace(LoggerMarkers.Savestate, "Load savestate index via indexer");
		SavestatePaths paths = indexer.loadSavestate(index, !SavestateFlags.BLOCK_CHANGE_INDEX.isBlocked(flags));
		logger.debug(LoggerMarkers.Savestate, "Source: {}, Target: {}", paths.getSourceFolder(), paths.getTargetFolder());

		if (paths.getSavestate().index == 0) {
			if (!ArrayUtils.contains(flags, SavestateFlags.BLOCK_CLIENT_SAVESTATE))
				flags = ArrayUtils.add(flags, SavestateFlags.BLOCK_CLIENT_SAVESTATE);
		}

		loadStateInner(paths, cb, flags);
	}

	/**
	 * Loads a temporary savestate (created in {@link #saveStateTemp(SavestateCallback)})
	 * @param cb The {@link SavestateCallback}
	 */
	public void loadStateTemp(SavestateCallback cb) {
		if (state == SavestateState.SAVING) {
			throw new LoadstateException("A savestating operation is already being carried out");
		}
		if (state == SavestateState.LOADING) {
			throw new LoadstateException("A loadstate operation is being carried out");
		}

		// Lock savestating and loadstating
		state = SavestateState.LOADING;

		SavestatePaths paths = indexer.loadTempSavestate();
		if (paths == null)
			return;

		// Add blocking flags
		SavestateFlags[] flags = new SavestateFlags[] { SavestateFlags.BLOCK_CLIENT_SAVESTATE, SavestateFlags.BLOCK_PAUSE_TICKRATE };
		loadStateInner(paths, cb, flags);
		/**
		 * After copying the temporary savestate (which is missing an index in savestate.dat)
		 * the index would get deleted in the main world folder.
		 * 
		 * Since that index is used to for loading the current index on start, we need to retrieve it by simply saving and overwriting the savestate
		 */
		paths.getSavestate().save();
	}

	/**
	 * Inner loadstate method using a series of steps to copy the folder from {@link SavestatePaths#getSourceFolder()} to {@link SavestatePaths#getTargetFolder()}
	 * and hotswapping the chunks while the {@link #server} is still running
	 * 
	 * @param paths The paths to use during copying
	 * @param cb The {@link SavestateCallback}
	 * @param flags The {@link SavestateFlags}
	 */
	private void loadStateInner(SavestatePaths paths, SavestateCallback cb, SavestateFlags... flags) {
// Enable tickrate 0
		TASmod.tickratechanger.pauseGame(true);

		String worldname = server.getWorldData().getLevelName();
		Path sourcefolder = paths.getSourceFolder();
		Path targetfolder = paths.getTargetFolder();

		EventListenerRegistry.fireEvent(EventSavestate.EventServerLoadstate.class, server, paths);

		/*
		 * Prevents loading an InputSavestate when loading index 0 (Index 0 is the
		 * savestate when starting a recording. Not doing this will load an empty
		 * InputSavestate)
		 */
		if (!SavestateFlags.BLOCK_CLIENT_SAVESTATE.isBlocked(flags)) {
			Path folder = paths.getSavestate().folder;
			Path savestateDir = folder.getParent().getParent();
			Path relativeFolder = savestateDir.relativize(folder);
			try {
				// loadstate inputs client
				TASmod.server.sendToAll(new TASmodBufferBuilder(TASmodPackets.SAVESTATE_LOAD).writeString(relativeFolder.toString()));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		// Disabeling level saving for all worlds in case the auto save kicks in during
		// world unload
		worldHandler.disableLevelSaving();

		// Unload chunks on client
		try {
			TASmod.server.sendToAll(new TASmodBufferBuilder(TASmodPackets.SAVESTATE_UNLOAD_CHUNKS));
		} catch (Exception e) {
			logger.catching(e);
		}

		// Unload chunks on the server
		worldHandler.disconnectPlayersFromChunkMap();
		worldHandler.unloadAllServerChunks();
		worldHandler.flushSaveHandler();

		// Delete and copy directories
		deleteFolder(targetfolder);
		copyFolder(sourcefolder, targetfolder);

		playerHandler.clearScoreboard();

		// Load the world from disk
		worldHandler.loadAllWorlds(worldname);

		// Update the player and the client
		playerHandler.loadAndSendMotionToPlayer();

		// Load the chunks and send them to the client
		worldHandler.addPlayersToChunkMap();

		// Reenable level saving
		worldHandler.enableLevelSaving();

		// Refresh server resourcepacks on the client
		SavestateResourcePackHandler.refreshServerResourcepack(server);

		// Add players to the chunk
		worldHandler.addPlayersToServerChunks();

		worldHandler.sendChunksToClient();

		if (SavestateFlags.BLOCK_PAUSE_TICKRATE.isBlocked(flags)) {
			TASmod.tickratechanger.pauseGame(false);
		} else {
			try {
				TASmod.server.sendToAll(new TASmodBufferBuilder(TASmodPackets.TICKRATE_0_WARN));
			} catch (Exception e) {
				logger.catching(e);
			}
		}

		if (cb != null)
			cb.invoke(paths);

		// Unlock savestating
		state = SavestateState.NONE;

		/*
		 *  TODO Savestates can be reloaded without a tick passing...
		 *  And since this scheduler is not cleared, it would execute the same task multiple times in the next tick
		 *  Rn it's not a problem, but this should be looked at...
		 */
		TASmod.tickSchedulerServer.add(() -> {
			EventListenerRegistry.fireEvent(EventSavestate.EventServerCompleteLoadstate.class);
			onLoadstateComplete();
		});
	}

	/**
	 * Create and set the {@link #indexer} based on the server
	 * @param server The server to retrieve the current directory from
	 */
private void createIndexer(MinecraftServer server) {
		logger.trace(LoggerMarkers.Savestate, "Creating savestate indexer");

		Path dataDirectory = server.getServerDirectory(); // The basic minecraft data directory
		Path savesDirectory = dataDirectory; // The location of minecraft saves. On the a dedicated server it's the same as the data directory
		if (!server.isDedicatedServer()) {
			savesDirectory = dataDirectory.resolve("saves"); // The location of minecraft saves. On the integrated server it's .minecraft/saves
		}

Path savestateBaseDirectory = savesDirectory.resolve("savestates"); // The base savestatedir: .minecraft/saves/savestates
		String worldname = server.getWorldData().getLevelName();

		logger.debug("Created savestate handler with saves: {}, savestates: {}, worldname: {}", savesDirectory, savestateBaseDirectory, worldname);
		this.indexer = new SavestateIndexer(logger, savesDirectory, savestateBaseDirectory, worldname);
	}

	/**
	 * Deletes the savestate at a specified index
	 * @param index The index to delete from
	 * @param cb The {@link SavestateCallback}
	 * @throws SavestateDeleteException If a savestate can't be deleted
	 */
	public void deleteSavestate(int index, SavestateCallback cb) throws SavestateDeleteException {
		logger.warn(LoggerMarkers.Savestate, "Deleting savestate {}", index);
		SavestatePaths paths = this.indexer.deleteSavestate(index);

		if (cb != null)
			cb.invoke(paths);
	}

	/**
	 * Deletes multiple savestates
	 * @param from The lower index to delete from
	 * @param to The upper index to delete to
	 * @param cb The {@link SavestateCallback}
	 * @param err The {@link ErrorRunnable} If an error occurs while deleting one savestate
	 * @throws SavestateDeleteException If something other than that fails
	 */
	public void deleteSavestate(int from, int to, SavestateCallback cb, ErrorRunnable err) throws SavestateDeleteException {
		logger.warn(LoggerMarkers.Savestate, "Deleting multiple savestates from {} to {}", from, to);
		if (state == SavestateState.SAVING) {
			err.run(new SavestateDeleteException("msg.tasmod.savestate.save.error"));
			return;
		}
		if (state == SavestateState.LOADING) {
			err.run(new SavestateDeleteException("msg.tasmod.savestate.load.error"));
			return;
		}

		DeletionRunnable onDelete = (paths) -> {
			SavestateIndexer.deleteFolder(paths.getTargetFolder());
			if (cb != null)
				cb.invoke(paths);
		};

		indexer.deleteMultipleSavestates(from, to, onDelete, err);
	}

	/**
	 * Renames a savestate at the specified index
	 * @param index The index to rename
	 * @param name The new name of that savestate
	 * @throws SavestateException If something goes wrong
	 */
	public void rename(int index, String name) throws SavestateException {
		rename(index, name, null);
	}

	/**
	 * Renames the {@link SavestateIndexer#getCurrentSavestate()}
	 * @param name The new name for the currentSavestate
	 * @throws SavestateException If something goes wrong
	 */
	public void renameCurrent(String name) throws SavestateException {
		indexer.renameCurrent(name);
	}

	/**
	 * Renames a savestate at the specified index
	 * @param index The index to rename
	 * @param name The new name of that savestate
	 * @param cb The {@link SavestateCallback}
	 * @throws SavestateException If something goes wrong
	 */
	public void rename(int index, String name, SavestateCallback cb) throws SavestateException {
		SavestatePaths paths = indexer.renameSavestate(index, name);

		if (cb != null)
			cb.invoke(paths);
	}

	/**
	 * Reloads the {@link #indexer}
	 */
	public void reload() {
		indexer.reload();
	}

public void onLoadstateComplete() { // TODO Make Event
		logger.trace(LoggerMarkers.Savestate, "Running loadstate complete event");
		PlayerList playerList = server.getPlayerList();
		for (ServerPlayer player : playerList.getPlayers()) {
			CompoundTag nbttagcompound = new CompoundTag(); // TODO: getPersistentData() not available in newer mappings
			playerHandler.reattachEntityToPlayer(nbttagcompound, player.level(), player);
		}
	}

	@Override
	public PacketID[] getAcceptedPacketIDs() {
		return new TASmodPackets[] {
				//@formatter:off
				TASmodPackets.SAVESTATE_SAVE,
				TASmodPackets.SAVESTATE_LOAD
				//@formatter:on
		};
	}

	@Override
	public void onServerPacket(PacketID id, ByteBuffer buf, String username) throws PacketNotImplementedException, WrongSideException, Exception {
		// TODO Permissions
		TASmodPackets packet = (TASmodPackets) id;

		ServerPlayer player = server.getPlayerList().getPlayerByName(username);

		switch (packet) {
			case SAVESTATE_SAVE:
				int index = TASmodBufferBuilder.readInt(buf);

				SavestateCallback cb = (paths) -> {
					/* 
					 * Opens the savestate rename screen only for the player who initiated the savestate.
					 * Once the player is done renaming the savestate, the screens are cleared for all players.
					 */
					try {
						TASmod.server.sendTo(player, new TASmodBufferBuilder(TASmodPackets.SAVESTATE_RENAME_SCREEN).writeInt(paths.getSavestate().index));
					} catch (Exception e) {
						LOGGER.catching(e);
					}
				};

Task savestateTask = () -> {
					try {
						saveState(index, cb);
					} catch (SavestateException e) {
						server.getPlayerList().broadcastSystemMessage(Component.translatable(e.getMessage()).withStyle(ChatFormatting.RED), false);

						try {
							TASmod.server.sendToAll(new TASmodBufferBuilder(TASmodPackets.TICKRATE_0_WARN));
							TASmod.server.sendToAll(new TASmodBufferBuilder(TASmodPackets.CLEAR_SCREEN));
						} catch (Exception e1) {
							logger.catching(e);
						}

						LOGGER.error("Failed to create a savestate");
						LOGGER.catching(e);
					} catch (Exception e) {
						Throwable cause = e.getCause();
						if (cause == null) {
							cause = e;
						}
						server.getPlayerList().broadcastSystemMessage(Component.translatable("msg.tasmod.savestate.failure", e.getMessage()).withStyle(ChatFormatting.RED), false);

						try {
							TASmod.server.sendToAll(new TASmodBufferBuilder(TASmodPackets.TICKRATE_0_WARN));
							TASmod.server.sendToAll(new TASmodBufferBuilder(TASmodPackets.CLEAR_SCREEN));
						} catch (Exception e1) {
							logger.catching(e);
						}

						LOGGER.error("Failed to create a savestate");
						LOGGER.catching(e);
					} finally {
						resetState();
					}
				};

				/*
				 * If the savestate is triggered via a keybind from the client,
				 * savestates have to be saved at the start of a tick,
				 * otherwise everything would desync 
				 */
				if (TASmod.tickratechanger.ticksPerSecond != 0)
					TASmod.tickSchedulerServer.add(savestateTask);
				else
					TASmod.gameLoopSchedulerServer.add(savestateTask);
				break;

			case SAVESTATE_LOAD:
				int indexing = TASmodBufferBuilder.readInt(buf);

SavestateCallback cb2 = CommandSavestate.createChatMessageCallback(player.createCommandSourceStack(), "msg.tasmod.savestate.load.end");

				Task loadstateTask = () -> {

					try {
						loadState(indexing, cb2);
					} catch (LoadstateException e) {
						server.getPlayerList().broadcastSystemMessage(Component.translatable(e.getMessage()).withStyle(ChatFormatting.RED), false);

						try {
							TASmod.server.sendToAll(new TASmodBufferBuilder(TASmodPackets.TICKRATE_0_WARN));
							TASmod.server.sendToAll(new TASmodBufferBuilder(TASmodPackets.CLEAR_SCREEN));
						} catch (Exception e1) {
							logger.catching(e);
						}

						LOGGER.error("Failed to create a savestate: " + e.getMessage());
					} catch (Exception e) {
						Throwable cause = e.getCause();
						if (cause == null) {
							cause = e;
						}
						server.getPlayerList().broadcastSystemMessage(Component.translatable("msg.tasmod.savestate.failure", e.getMessage()).withStyle(ChatFormatting.RED), false);

						try {
							TASmod.server.sendToAll(new TASmodBufferBuilder(TASmodPackets.TICKRATE_0_WARN));
							TASmod.server.sendToAll(new TASmodBufferBuilder(TASmodPackets.CLEAR_SCREEN));
						} catch (Exception e1) {
							logger.catching(e);
						}

						LOGGER.throwing(e);
					} finally {
						resetState();
					}
				};
				TASmod.gameLoopSchedulerServer.add(loadstateTask);
				break;

			default:
				throw new PacketNotImplementedException(packet, this.getClass(), Side.SERVER);
		}
	}

	/**
	 * Copies a folder recursively
	 * @param src Source path to copy from
	 * @param dest Source path to copy to
	 */
	public static void copyFolder(Path src, Path dest) {
		try {
			Files.walk(src).forEach(s -> {
				try {
					Path d = dest.resolve(src.relativize(s));
					if (Files.isDirectory(s)) {
						if (!Files.exists(d))
							Files.createDirectory(d);
						return;
					}
					Files.copy(s, d, StandardCopyOption.REPLACE_EXISTING);
				} catch (Exception e) {
					e.printStackTrace();
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Deletes a folder recursively
	 * @param toDelete The folder to delete
	 */
	public static void deleteFolder(Path toDelete) {
		try {
			Files.walk(toDelete).forEach(s -> {
				if (toDelete.equals(s))
					return;
				if (Files.isDirectory(s)) {
					deleteFolder(s);
				} else {
					try {
						Files.delete(s);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			});
			Files.delete(toDelete);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Callback method that runs at the end of a Savestate/Loadstate
	 * 
	 * Takes a {@link SavestatePaths} argument
	 * 
	 * @author Scribble
	 */
	@FunctionalInterface
	public interface SavestateCallback {
		public void invoke(SavestatePaths path);
	}

	/**
	 * The state of the savestate
	 * 
	 * @author Scribble
	 */
	public enum SavestateState {
		/**
		 * This savestatehandler is currently creating a savestate
		 */
		SAVING,
		/**
		 * The savestatehandler is currently loading a savestate
		 */
		LOADING,
		/**
		 * The savestatehandler is idle. It can accept commands to create/load a savestate
		 */
		NONE
	}

	/**
	 * Acts as flags for savestates and loadstates
	 * 
	 * Add these to the parameters to block certain savestate behaviour
	 * 
	 * @author Scribble
	 */
	public static enum SavestateFlags {
		/**
		 * Stops updating the current index when savestating/loadstating
		 */
		BLOCK_CHANGE_INDEX,
		/**
		 * Stops the creation/loading of a client savestate
		 */
		BLOCK_CLIENT_SAVESTATE,
		/**
		 * Stops setting the tickrate to 0 after a savestate/loadstate
		 */
		BLOCK_PAUSE_TICKRATE;

		/**
		 * Check if the flag is in the flaglist and therefore blocked
		 * @param flagList The flag list to check
		 * @return True if this enum is in the flagList
		 */
		public boolean isBlocked(SavestateFlags[] flagList) {
			return Arrays.stream(flagList).anyMatch(this::equals);
		}
	}

	/**
	 * @return A list of savestates with savestates with a default range
	 */
	public List<SavestateIndexer.Savestate> getSavestateInfo() {
		return getSavestateInfo(-1, 10);
	}

	/**
	 * @param center The center of the range 
	 * @param amount How many savestates minus and plus the center are displayed
	 * @return A list of savestates with a specified center and amount
	 */
	public List<SavestateIndexer.Savestate> getSavestateInfo(int center, int amount) {
		return indexer.getSavestateList(center, amount);
	}

	/**
	 * @return How many savestates are used
	 */
	public int size() {
		return indexer.size();
	}

	/**
	 * @return {@link SavestateIndexer#getCurrentIndex()}
	 */
	public int getCurrentIndex() {
		return indexer.getCurrentIndex();
	}

	/**
	 * @return The current savestate directory 
	 */
	public Path getCurrentSavestateDir() {
		return indexer.getCurrentSavestateDir();
	}

	/**
	 * Resets the {@link #state} to {@link SavestateState#NONE}
	 */
	public void resetState() {
		state = SavestateState.NONE;
	}

	/**
	 * @return The current {@link #state}
	 */
	public SavestateState getState() {
		return state;
	}

	/**
	 * @return The {@link #playerHandler}
	 */
	public SavestatePlayerHandlerServer getPlayerHandler() {
		return playerHandler;
	}

	/**
	 * @return The {@link #tempSavestateHandler}
	 */
	public SavestateTempHandler getSavestateTemporaryHandler() {
		return tempSavestateHandler;
	}
}

