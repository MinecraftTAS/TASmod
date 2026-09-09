package com.minecrafttas.tasmod;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.minecrafttas.mctcommon.CommandRegistry;
import com.minecrafttas.mctcommon.Configuration;
import com.minecrafttas.mctcommon.ConfigurationRegistry;
import com.minecrafttas.mctcommon.events.EventListenerRegistry;
import com.minecrafttas.mctcommon.events.EventServer.EventServerInit;
import com.minecrafttas.mctcommon.events.EventServer.EventServerStart;
import com.minecrafttas.mctcommon.events.EventServer.EventServerStop;
import com.minecrafttas.mctcommon.networking.PacketHandlerRegistry;
import com.minecrafttas.mctcommon.networking.Server;
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
import com.minecrafttas.tasmod.config.TASmodServerConfig;
import com.minecrafttas.tasmod.handlers.PlayUntilHandler;
import com.minecrafttas.tasmod.ktrng.GlobalRNG;
import com.minecrafttas.tasmod.ktrng.builtin.MathRNG;
import com.minecrafttas.tasmod.ktrng.builtin.SquidRNG;
import com.minecrafttas.tasmod.ktrng.builtin.WorldSeedRNG;
import com.minecrafttas.tasmod.ktrng.events.KillTheRNGMonitor;
import com.minecrafttas.tasmod.ktrng.handlers.UUIDHandler;
import com.minecrafttas.tasmod.playback.PlaybackControllerServer;
import com.minecrafttas.tasmod.playback.metadata.builtin.StartpositionMetadataExtension;
import com.minecrafttas.tasmod.registries.TASmodAPIRegistry;
import com.minecrafttas.tasmod.registries.TASmodPackets;
import com.minecrafttas.tasmod.savestates.SavestateHandlerServer;
import com.minecrafttas.tasmod.savestates.handlers.SavestateGuiHandlerServer;
import com.minecrafttas.tasmod.savestates.handlers.SavestateResourcePackHandler;
import com.minecrafttas.tasmod.savestates.storage.builtin.ClientMotionStorage;
import com.minecrafttas.tasmod.savestates.storage.builtin.EntityAiTaskStorage;
import com.minecrafttas.tasmod.savestates.storage.builtin.EntityStorage;
import com.minecrafttas.tasmod.savestates.storage.builtin.KTRNGSeedStorage;
import com.minecrafttas.tasmod.savestates.storage.builtin.entity.BatSpawnPositionSubStorage;
import com.minecrafttas.tasmod.savestates.storage.builtin.entity.CreeperDetonateSubStorage;
import com.minecrafttas.tasmod.savestates.storage.builtin.entity.LivingTickTimersSubStorage;
import com.minecrafttas.tasmod.savestates.storage.builtin.entity.SquidRotationSubStorage;
import com.minecrafttas.tasmod.tickratechanger.TickrateChangerServer;
import com.minecrafttas.tasmod.ticksync.TickSyncServer;
import com.minecrafttas.tasmod.util.LoggerMarkers;
import com.minecrafttas.tasmod.util.Scheduler;
import com.minecrafttas.tasmod.util.TabCompletionUtils;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.impl.FabricLoaderImpl;
import net.minecraft.server.MinecraftServer;

/**
 * ModContainer for TASmod
 *
 * @author Scribble
 */
public class TASmod implements ModInitializer, EventServerStart, EventServerInit, EventServerStop {

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

	public static final boolean isDevEnvironment = FabricLoaderImpl.INSTANCE.isDevelopmentEnvironment();

	public static final StartpositionMetadataExtension startPositionMetadataExtension = new StartpositionMetadataExtension();

	public static final TabCompletionUtils tabCompletionUtils = new TabCompletionUtils();

	public static final CommandFileCommand commandFileCommand = new CommandFileCommand();

	public static final PlayUntilHandler playUntil = new PlayUntilHandler();

	public static ClientMotionStorage motionStorage = new ClientMotionStorage();

	public static GlobalRNG globalRandomness;

	public static KTRNGSeedStorage seedStorage = new KTRNGSeedStorage();
	public static EntityAiTaskStorage entityAiTaskStorage = new EntityAiTaskStorage();

	public static MathRNG mathRandomness = new MathRNG(0);

	public static WorldSeedRNG worldSeedRandomness = new WorldSeedRNG(0);

	public static SquidRNG squidRNG = new SquidRNG(0L);

	public static KillTheRNGMonitor debugRand = new KillTheRNGMonitor();

	public static UUIDHandler uuidHandler = new UUIDHandler();

	public static Configuration config;

	@Override
	public void onInitialize() {

		LOGGER.info("Initializing TASmod");

		String modVersion = FabricLoader.getInstance().getModContainer("tasmod").get().getMetadata().getVersion().getFriendlyString();

		if (!"${mod_version}".equals(modVersion)) {
			version = modVersion;
		}

		// Start ticksync
		ticksyncServer = new TickSyncServer();

		// Initilize KillTheRNG
		LOGGER.info("Testing connection with KillTheRNG");
		//		ktrngHandler = new KillTheRNGHandler(FabricLoaderImpl.INSTANCE.isModLoaded("killtherng"));

		// Initialize TickrateChanger
		tickratechanger = new TickrateChangerServer(LOGGER);

		// Register event listeners
		EventListenerRegistry.register(this);
		EventListenerRegistry.register(ticksyncServer);
		EventListenerRegistry.register(tickratechanger);
		//		EventListenerRegistry.register(ktrngHandler);
		EventListenerRegistry.register(debugRand);

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
		EventListenerRegistry.register(uuidHandler);
		EventListenerRegistry.register(TASmodAPIRegistry.SAVESTATE_STORAGE);

		registerSavestateStorage();
	}

	@Override
	public void onServerStart(MinecraftServer server) {
		if (globalRandomness == null) {
			globalRandomness = new GlobalRNG();
			EventListenerRegistry.register(globalRandomness);
		}
		mathRandomness = new MathRNG(0);
		squidRNG.setSeed(globalRandomness.getCurrentSeed() + 1000L);
	}

	@Override
	public void onServerInit(MinecraftServer server) {
		LOGGER.info("Initializing server");
		serverInstance = server;
		loadConfig(server);

		// Command handling

		CommandRegistry.registerServerCommand(new CommandTickrate(), server);
		CommandRegistry.registerServerCommand(new CommandRecord(), server);
		CommandRegistry.registerServerCommand(new CommandPlay(), server);
		CommandRegistry.registerServerCommand(new CommandSaveTAS(), server);
		CommandRegistry.registerServerCommand(new CommandLoadTAS(), server);
		CommandRegistry.registerServerCommand(new CommandClearInputs(), server);
		CommandRegistry.registerServerCommand(new CommandSavestate(), server);
		CommandRegistry.registerServerCommand(new CommandFullRecord(), server);
		CommandRegistry.registerServerCommand(new CommandFullPlay(), server);
		CommandRegistry.registerServerCommand(new CommandRestartAndPlay(), server);
		CommandRegistry.registerServerCommand(new CommandPlayUntil(), server);
		CommandRegistry.registerServerCommand(commandFileCommand, server);

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
		TASmodAPIRegistry.SAVESTATE_STORAGE.register(seedStorage);
		//@formatter:off
		TASmodAPIRegistry.SAVESTATE_STORAGE.register(
				new EntityStorage(
							new BatSpawnPositionSubStorage(),
							new CreeperDetonateSubStorage(),
							new SquidRotationSubStorage(),
							new LivingTickTimersSubStorage()
						)
				);
		//@formatter:on
		TASmodAPIRegistry.SAVESTATE_STORAGE.register(entityAiTaskStorage);
	}

	public static MinecraftServer getServerInstance() {
		return serverInstance;
	}

	private void loadConfig(MinecraftServer server) {
		Path configDir = server.getDataDirectory().toPath().resolve("config");

		if (!Files.exists(configDir)) {
			try {
				Files.createDirectory(configDir);
			} catch (IOException e) {
				LOGGER.catching(e);
			}
		}

		ConfigurationRegistry SERVER_CONFIG_REGISTRY = new ConfigurationRegistry();
		SERVER_CONFIG_REGISTRY.register(TASmodServerConfig.values());
		config = new Configuration("TASmod Server Configuration", configDir.resolve("tasmod_server.cfg"), SERVER_CONFIG_REGISTRY);

		config.load();
		config.save();
	}
}
