package com.minecrafttas.tasmod;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.minecrafttas.tasmod.mctcommon.events.EventListenerRegistry;
import com.minecrafttas.tasmod.mctcommon.events.EventServer.EventServerInit;
import com.minecrafttas.tasmod.mctcommon.events.EventServer.EventServerStop;
import com.minecrafttas.tasmod.mctcommon.networking.PacketHandlerRegistry;
import com.minecrafttas.tasmod.mctcommon.networking.Server;
import com.minecrafttas.tasmod.commands.CommandClearInputs;
import com.minecrafttas.tasmod.commands.CommandFileCommand;
import com.minecrafttas.tasmod.commands.CommandFullPlay;
import com.minecrafttas.tasmod.commands.CommandFullRecord;
import com.minecrafttas.tasmod.commands.CommandLoadTAS;
import com.minecrafttas.tasmod.commands.CommandPlay;
import com.minecrafttas.tasmod.commands.CommandPlayUntil;
import com.minecrafttas.tasmod.commands.CommandRecord;
import com.minecrafttas.tasmod.commands.CommandRestartAndPlay;
import com.minecrafttas.tasmod.commands.CommandSaveTAS;
import com.minecrafttas.tasmod.commands.CommandSavestate;
import com.minecrafttas.tasmod.commands.CommandTickrate;
import com.minecrafttas.tasmod.handlers.PlayUntilHandler;
import com.minecrafttas.tasmod.playback.PlaybackControllerServer;
import com.minecrafttas.tasmod.playback.metadata.builtin.StartpositionMetadataExtension;
import com.minecrafttas.tasmod.registries.TASmodAPIRegistry;
import com.minecrafttas.tasmod.registries.TASmodPackets;
import com.minecrafttas.tasmod.savestates.SavestateHandlerServer;
import com.minecrafttas.tasmod.savestates.handlers.SavestateGuiHandlerServer;
import com.minecrafttas.tasmod.savestates.handlers.SavestateResourcePackHandler;
import com.minecrafttas.tasmod.savestates.storage.builtin.ClientMotionStorage;
import com.minecrafttas.tasmod.tickratechanger.TickrateChangerServer;
import com.minecrafttas.tasmod.ticksync.TickSyncServer;
import com.minecrafttas.tasmod.util.LoggerMarkers;
import com.minecrafttas.tasmod.util.Scheduler;
import com.minecrafttas.tasmod.util.TabCompletionUtils;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.loader.api.FabricLoader;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;

/**
 * ModContainer for TASmod
 *
 * @author Scribble
 */
public class TASmod implements ModInitializer, EventServerInit, EventServerStop {

	public static final Logger LOGGER = LogManager.getLogger("TASmod");

	public static String version = "dev";

	private static MinecraftServer serverInstance;

	public static PlaybackControllerServer playbackControllerServer = new PlaybackControllerServer();

	public static SavestateHandlerServer savestateHandlerServer;

	//	public static KillTheRNGHandler ktrngHandler;

	public static TickrateChangerServer tickratechanger;

	public static TickSyncServer ticksyncServer;

	public static final Scheduler tickSchedulerServer = new Scheduler();
	public static final Scheduler gameLoopSchedulerServer = new Scheduler();

	public static Server server;

	public static final int networkingport = 8999;

	public static final boolean isDevEnvironment = FabricLoader.getInstance().isDevelopmentEnvironment();

	public static final StartpositionMetadataExtension startPositionMetadataExtension = new StartpositionMetadataExtension();

	public static final TabCompletionUtils tabCompletionUtils = new TabCompletionUtils();

	public static final CommandFileCommand commandFileCommand = new CommandFileCommand();

	public static final PlayUntilHandler playUntil = new PlayUntilHandler();

	public static ClientMotionStorage motionStorage = new ClientMotionStorage();

	@Override
	public void onInitialize() {

		LOGGER.info("Initializing TASmod");

		String modVersion = FabricLoader.getInstance().getModContainer("tasmod").get().getMetadata().getVersion().getFriendlyString();

		if (!"${mod_version}".equals(modVersion)) {
			version = modVersion;
		}

		// Register commands
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			CommandTickrate.register(dispatcher);
			CommandRecord.register(dispatcher);
			CommandPlay.register(dispatcher);
			CommandSaveTAS.register(dispatcher);
			CommandLoadTAS.register(dispatcher);
			CommandClearInputs.register(dispatcher);
			CommandSavestate.register(dispatcher);
			CommandFullRecord.register(dispatcher);
			CommandFullPlay.register(dispatcher);
			CommandRestartAndPlay.register(dispatcher);
			CommandPlayUntil.register(dispatcher);
			CommandFileCommand.register(dispatcher);
		});

		// Start ticksync
		ticksyncServer = new TickSyncServer();

		// Initilize KillTheRNG
		LOGGER.info("Testing connection with KillTheRNG");
		//		ktrngHandler = new KillTheRNGHandler(FabricLoader.getInstance().isModLoaded("killtherng"));

		// Initialize TickrateChanger
		tickratechanger = new TickrateChangerServer(LOGGER);

		// Register event listeners
		EventListenerRegistry.register(this);
		EventListenerRegistry.register(ticksyncServer);
		EventListenerRegistry.register(tickratechanger);
		//		EventListenerRegistry.register(ktrngHandler);

		// Register packet handlers
		LOGGER.info(LoggerMarkers.Networking, "Registering network handlers");
		PacketHandlerRegistry.register(ticksyncServer);
		PacketHandlerRegistry.register(tickratechanger);
		//		PacketHandlerRegistry.register(ktrngHandler);
		PacketHandlerRegistry.register(playbackControllerServer);
		PacketHandlerRegistry.register(startPositionMetadataExtension);
		PacketHandlerRegistry.register(tabCompletionUtils);
		PacketHandlerRegistry.register(commandFileCommand);
		PacketHandlerRegistry.register(new SavestateGuiHandlerServer());
		PacketHandlerRegistry.register(motionStorage);
		SavestateResourcePackHandler resourcepackHandler = new SavestateResourcePackHandler();
		PacketHandlerRegistry.register(resourcepackHandler);
		EventListenerRegistry.register(resourcepackHandler);
		PacketHandlerRegistry.register(playUntil);
		EventListenerRegistry.register(playUntil);

		EventListenerRegistry.register(TASmodAPIRegistry.SAVESTATE_STORAGE);

		registerSavestateStorage();
	}

	@Override
	public void onServerInit(MinecraftServer server) {
		LOGGER.info("Initializing server");
		serverInstance = server;

		savestateHandlerServer = new SavestateHandlerServer(server, LOGGER);
		PacketHandlerRegistry.register(savestateHandlerServer);
		PacketHandlerRegistry.register(savestateHandlerServer.getPlayerHandler());
		EventListenerRegistry.register(savestateHandlerServer.getSavestateTemporaryHandler());

		if (!server.isDedicatedServer()) {
			TASmod.tickratechanger.ticksPerSecond = 0F;
			TASmod.tickratechanger.tickrateSaved = 20F;
		} else {
			// Starting custom server instance
			try {
				TASmod.server = new Server(networkingport, TASmodPackets.values());
			} catch (Exception e) {
				LOGGER.error("Unable to launch TASmod server: {}", e.getMessage());
			}
		}
	}

	@Override
	public void onServerStop(MinecraftServer mcserver) {
		serverInstance = null;

		if (mcserver.isDedicatedServer()) {
			try {
				if (server != null)
					server.close();
			} catch (IOException e) {
				LOGGER.error("Unable to close TASmod server: {}", e);
			}
		}

		if (savestateHandlerServer != null) {
			PacketHandlerRegistry.unregister(savestateHandlerServer); // Unregistering the savestatehandler, as a new instance is registered in onServerStart()
			PacketHandlerRegistry.unregister(savestateHandlerServer.getPlayerHandler());
			EventListenerRegistry.unregister(savestateHandlerServer.getSavestateTemporaryHandler());

			savestateHandlerServer = null;
		}
	}

	private void registerSavestateStorage() {
		TASmodAPIRegistry.SAVESTATE_STORAGE.register(motionStorage);
	}

	public static MinecraftServer getServerInstance() {
		return serverInstance;
	}
}

