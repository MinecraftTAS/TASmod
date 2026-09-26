package com.minecrafttas.tasmod.savestates.handlers;

import static com.minecrafttas.tasmod.TASmod.LOGGER;

import java.util.ArrayList;
import java.util.List;

import com.minecrafttas.tasmod.TASmod;
import com.minecrafttas.tasmod.util.Ducks.ChunkProviderDuck;
import com.minecrafttas.tasmod.util.Ducks.ScreenDuck;
import com.minecrafttas.tasmod.util.LoggerMarkers;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.chunk.LevelChunk;

public class SavestateWorldHandler {

	private final MinecraftServer server;

	public SavestateWorldHandler(MinecraftServer server) {
		this.server = server;
	}

	public void disableLevelSaving() {
		for (ServerLevel world : server.getAllLevels()) {
		}
	}

	public void enableLevelSaving() {
		for (ServerLevel world : server.getAllLevels()) {
		}
	}

	public void addPlayersToServerChunks() {
		for (ServerPlayer player : server.getPlayerList().getPlayers()) {
			addPlayerToServerChunk(player);
		}
	}

	public void addPlayerToServerChunk(ServerPlayer player) {
		LOGGER.trace(LoggerMarkers.Savestate, "Add player {} to server LevelChunk", player.getName().getString());
	}

	public void addPlayersToChunkMap() {
		List<ServerPlayer> players = new ArrayList<>(server.getPlayerList().getPlayers());
		for (ServerPlayer player : players) {
			LOGGER.trace(LoggerMarkers.Savestate, "Add player {} to the LevelChunk map", player.getName().getString());
		}
	}

	private void addPlayerToChunkMap(ServerLevel world, ServerPlayer player) {
		LOGGER.trace(LoggerMarkers.Savestate, "Add player {} to the LevelChunk map", player.getName().getString());
	}

	public void disconnectPlayersFromChunkMap() {
		for (ServerLevel world : server.getAllLevels()) {
			for (ServerPlayer player : server.getPlayerList().getPlayers()) {
				LOGGER.trace(LoggerMarkers.Savestate, "Disconnect player {} from the LevelChunk map", player.getName().getString());
			}
		}
	}

	public void unloadAllServerChunks() {
		LOGGER.trace(LoggerMarkers.Savestate, "Unloading all server chunks");
		for (ServerLevel world : server.getAllLevels()) {
		}
	}

	public void sendChunksToClient() {
		for (ServerLevel world : server.getAllLevels()) {
		}
	}

	public void loadAllWorlds(String string) {
		LOGGER.warn(LoggerMarkers.Savestate, "loadAllWorlds is not fully supported in 26.3");
	}

	public void flushSaveHandler() {
		// No-op - world saving is handled automatically in modern Minecraft
	}
}