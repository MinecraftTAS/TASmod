package com.minecrafttas.tasmod;

import static com.minecrafttas.tasmod.TASmod.LOGGER;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.concurrent.TimeoutException;

import org.apache.logging.log4j.Level;

import com.minecrafttas.tasmod.mctcommon.Configuration;
import com.minecrafttas.tasmod.mctcommon.ConfigurationRegistry;
import com.minecrafttas.tasmod.mctcommon.KeybindManager;
import com.minecrafttas.tasmod.mctcommon.LanguageManager;
import com.minecrafttas.tasmod.mctcommon.events.EventClient.EventClientInit;
import com.minecrafttas.tasmod.mctcommon.events.EventClient.EventOpenGui;
import com.minecrafttas.tasmod.mctcommon.events.EventClient.EventOptionsInit;
import com.minecrafttas.tasmod.mctcommon.events.EventClient.EventPlayerJoinedClientSide;
import com.minecrafttas.tasmod.mctcommon.events.EventListenerRegistry;
import com.minecrafttas.tasmod.mctcommon.file.AbstractDataFile;
import com.minecrafttas.tasmod.mctcommon.networking.Client;
import com.minecrafttas.tasmod.mctcommon.networking.PacketHandlerRegistry;
import com.minecrafttas.tasmod.mctcommon.networking.Server;
import com.minecrafttas.tasmod.commands.client.CommandFolder;
import com.minecrafttas.tasmod.gui.InfoHud;
import com.minecrafttas.tasmod.handlers.LoadingScreenHandler;
import com.minecrafttas.tasmod.playback.PlaybackControllerClient;
import com.minecrafttas.tasmod.playback.PlaybackControllerClient.TASstate;
import com.minecrafttas.tasmod.playback.filecommands.builtin.DesyncMonitorFileCommandExtension;
import com.minecrafttas.tasmod.playback.filecommands.builtin.LabelFileCommandExtension;
import com.minecrafttas.tasmod.playback.filecommands.builtin.OptionsFileCommandExtension;
import com.minecrafttas.tasmod.playback.metadata.builtin.CreditsMetadataExtension;
import com.minecrafttas.tasmod.playback.metadata.builtin.StartpositionMetadataExtension;
import com.minecrafttas.tasmod.playback.tasfile.flavor.builtin.AlphaFlavor;
import com.minecrafttas.tasmod.playback.tasfile.flavor.builtin.Beta1Flavor;
import com.minecrafttas.tasmod.registries.TASmodAPIRegistry;
import com.minecrafttas.tasmod.registries.TASmodConfig;
import com.minecrafttas.tasmod.registries.TASmodKeybinds;
import com.minecrafttas.tasmod.registries.TASmodPackets;
import com.minecrafttas.tasmod.savestates.SavestateHandlerClient;
import com.minecrafttas.tasmod.savestates.handlers.SavestateGuiHandlerClient;
import com.minecrafttas.tasmod.savestates.handlers.SavestatePlayerHandlerClient;
import com.minecrafttas.tasmod.tickratechanger.TickrateChangerClient;
import com.minecrafttas.tasmod.ticksync.TickSyncClient;
import com.minecrafttas.tasmod.util.LoggerMarkers;
import com.minecrafttas.tasmod.util.Scheduler;
import com.minecrafttas.tasmod.util.ShieldDownloader;
import com.minecrafttas.tasmod.virtual.VirtualInput;
import com.minecrafttas.tasmod.virtual.VirtualKeybindings;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.Options;
import net.minecraft.server.MinecraftServer;

import java.nio.file.Paths;

public class TASmodClient implements ClientModInitializer, EventClientInit, EventPlayerJoinedClientSide, EventOpenGui, EventOptionsInit {

	public static VirtualInput virtual;

	public static TickSyncClient ticksyncClient;

	public static Path getTasFileDirectory() {
		return Paths.get(".").toAbsolutePath().resolve("saves").resolve("tasfiles");
	}

	public static Path getSavestateDirectory() {
		return Paths.get(".").toAbsolutePath().resolve("saves").resolve("savestates");
	}

	// Static fields for backward compatibility with other classes
	public static final Path tasfiledirectory = getTasFileDirectory();
	public static final Path savestatedirectory = getSavestateDirectory();

	public static InfoHud hud;

	public static ShieldDownloader shieldDownloader;

	public static TickrateChangerClient tickratechanger = new TickrateChangerClient();

	public static Scheduler gameLoopSchedulerClient = new Scheduler();

	public static Scheduler tickSchedulerClient = new Scheduler();

	public static Scheduler openTitleScreenScheduler = new Scheduler();

	public static Configuration config;

	public static LoadingScreenHandler loadingScreenHandler;

	public static KeybindManager keybindManager;

	public static SavestateHandlerClient savestateHandlerClient = new SavestateHandlerClient();

	public static Client client;

	public static CreditsMetadataExtension creditsMetadataExtension = new CreditsMetadataExtension();

	public static StartpositionMetadataExtension startpositionMetadataExtension = new StartpositionMetadataExtension();
	/**
	 * The container where all inputs get stored during recording or stored and
	 * ready to be played back
	 */
	public static PlaybackControllerClient controller = new PlaybackControllerClient();

	public static void createTASfileDir() {
		try {
			AbstractDataFile.createDirectory(getTasFileDirectory());
		} catch (IOException e) {
			TASmod.LOGGER.catching(e);
		}
	}

	public static void createSavestatesDir() {
		try {
			AbstractDataFile.createDirectory(getSavestateDirectory());
		} catch (IOException e) {
			TASmod.LOGGER.catching(e);
		}
	}

	@Override
	public void onInitializeClient() {

		LanguageManager.registerMod("tasmod");

		createFolders();

		registerConfigValues();

		Minecraft mc = Minecraft.getInstance();

		loadConfig(mc);

		virtual = new VirtualInput(LOGGER);

		// Initialize InfoHud
		hud = new InfoHud();
		// Initialize shield downloader
		shieldDownloader = new ShieldDownloader();
		// Initialize loading screen handler
		loadingScreenHandler = new LoadingScreenHandler();
		// Initialize Ticksync
		ticksyncClient = new TickSyncClient();
		// Initialize keybind manager
		keybindManager = new KeybindManager(VirtualKeybindings::isKeyDownExceptTextfield);

		// Create them here so they are created after the folders have been created, since they depend on the tasfiles folder
		desyncMonitorFileCommandExtension = new DesyncMonitorFileCommandExtension();
		optionsFileCommandExtension = new OptionsFileCommandExtension();
		labelFileCommandExtension = new LabelFileCommandExtension();

		TASmodAPIRegistry.CLIENT_COMMANDS.register(new CommandFolder());

		registerEventListeners();

		registerNetworkPacketHandlers();

		// Starting local server instance
		try {
			TASmod.server = new Server(TASmodPackets.values());
		} catch (Exception e) {
			LOGGER.error("Unable to launch TASmod server: {}", e.getMessage());
		}
	}

	private void createFolders() {
		createTASfileDir();
		createSavestatesDir();
	}

	private void registerNetworkPacketHandlers() {
		// Register packet handlers
		LOGGER.info(LoggerMarkers.Networking, "Registering network handlers on client");
		PacketHandlerRegistry.register(controller);
		PacketHandlerRegistry.register(ticksyncClient);
		PacketHandlerRegistry.register(tickratechanger);
		PacketHandlerRegistry.register(savestateHandlerClient);
		PacketHandlerRegistry.register(new SavestatePlayerHandlerClient());
		PacketHandlerRegistry.register(new SavestateGuiHandlerClient());
	}

	private void registerEventListeners() {
		EventListenerRegistry.register(this);
		EventListenerRegistry.register(hud);
		EventListenerRegistry.register(shieldDownloader);
		EventListenerRegistry.register(loadingScreenHandler);
		EventListenerRegistry.register(ticksyncClient);
		EventListenerRegistry.register(keybindManager);
		EventListenerRegistry.register((EventOpenGui) (gui -> {
			if (gui instanceof TitleScreen) {
				openTitleScreenScheduler.runAllTasks();
			}
			return gui;
		}));
		EventListenerRegistry.register(controller);
		EventListenerRegistry.register(creditsMetadataExtension);
		EventListenerRegistry.register(startpositionMetadataExtension);

		EventListenerRegistry.register(desyncMonitorFileCommandExtension);

		EventListenerRegistry.register(TASmodAPIRegistry.PLAYBACK_METADATA);
		EventListenerRegistry.register(TASmodAPIRegistry.PLAYBACK_FILE_COMMAND);
		EventListenerRegistry.register(new LoggerMarkers());
		EventListenerRegistry.register(savestateHandlerClient);

		// virtual.interpolationHandler is not an EventBase, handled elsewhere
		// EventListenerRegistry.register(virtual.interpolationHandler);
		EventListenerRegistry.register(tickratechanger);
	}

	@Override
	public void onClientInit(Minecraft mc) {
		registerPlaybackMetadata(mc);
		registerSerialiserFlavors(mc);
		registerFileCommands();
	}

	boolean waszero;

	boolean isLoading;

	@Override
	public void onPlayerJoinedClientSide(LocalPlayer player) {
		Minecraft mc = Minecraft.getInstance();
		ServerData data = mc.getCurrentServer();
		MinecraftServer server = TASmod.getServerInstance();

		String ip = null;
		int port;
		boolean local;
		if (server != null) {
			ip = "localhost";
			port = TASmod.server.port;
			local = true;
		} else if (data != null) {
			ip = data.ip.split(":")[0];
			port = TASmod.networkingport;
			local = false;
		} else {
			return; // No server data available
		}

		String connectedIP = null;
		try {
			connectedIP = client.getRemote();
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (!(ip + ":" + port).equals(connectedIP)) {
			try {
				LOGGER.info("Closing client connection: {}", client.getRemote());
				client.disconnect();
			} catch (IOException e) {
				e.printStackTrace();
			}
			final String IP = ip;
			final int PORT = port;
			gameLoopSchedulerClient.add(() -> {
				try {
					// connect to server and authenticate
					client = new Client(IP, PORT, TASmodPackets.values(), mc.getUser().getName(), local);
				} catch (TimeoutException e) {
					// mc.getConnection().getNetworkManager().closeChannel(null); // API changed
				} catch (Exception e) {
					LOGGER.error("Unable to connect TASmod client: {}", e.getMessage());
					e.printStackTrace();
				}
			});
		}
	}

	@Override
	public Screen onOpenGui(Screen gui) {
		if (gui instanceof TitleScreen) {
			initializeCustomPacketHandler();
		} else if (gui != null && gui.getClass().getSimpleName().equals("ControlsScreen")) {
			TASmodClient.controller.setTASState(TASstate.NONE); // Set the TASState to nothing to avoid collisions
			if (TASmodClient.tickratechanger.ticksPerSecond == 0) {
				TASmodClient.tickratechanger.pauseClientGame(false); // Unpause the game
				waszero = true;
			}
		} else if (gui != null && !gui.getClass().getSimpleName().equals("ControlsScreen")) {
			if (waszero) {
				waszero = false;
				TASmodClient.tickratechanger.pauseClientGame(true);
			}
		}
		return gui;
	}

	private void initializeCustomPacketHandler() {
		if (client == null) {
			Minecraft mc = Minecraft.getInstance();

			String IP = "localhost";
			int PORT = TASmod.server.port;

			// Get the connection on startup from config
			String configAddress = config.get(TASmodConfig.ServerConnection);
			if (configAddress != null && !configAddress.isEmpty()) {
				String[] ipSplit = configAddress.split(":");
				IP = ipSplit[0];
				try {
					PORT = Integer.parseInt(ipSplit[1]);
				} catch (Exception e) {
					LOGGER.catching(Level.ERROR, e);
					IP = "localhost";
					PORT = TASmod.networkingport - 1;
				}
			}

			try {
				// connect to server and authenticate
				client = new Client(IP, PORT, TASmodPackets.values(), mc.getUser().getName(), true);
			} catch (Exception e) {
				LOGGER.error("Unable to connect TASmod client: {}", e);
			}
		}
	}

	@Override
	public void onOptionsInit(Options options) {
		// Initialize keybind manager
		keybindManager.registerKeybinds(options, TASmodKeybinds.class);
		Arrays.stream(TASmodKeybinds.valuesVanillaKeybind()).forEach(VirtualKeybindings::registerBlockedKeyMapping);
	}

	private void registerPlaybackMetadata(Minecraft mc) {
		TASmodAPIRegistry.PLAYBACK_METADATA.register(creditsMetadataExtension);
		TASmodAPIRegistry.PLAYBACK_METADATA.register(startpositionMetadataExtension);
	}

	public static Beta1Flavor betaFlavor = new Beta1Flavor();
	public static AlphaFlavor alphaFlavor = new AlphaFlavor();

	private void registerSerialiserFlavors(Minecraft mc) {
		TASmodAPIRegistry.SERIALISER_FLAVOR.register(betaFlavor);
		TASmodAPIRegistry.SERIALISER_FLAVOR.register(alphaFlavor);
	}

	public static DesyncMonitorFileCommandExtension desyncMonitorFileCommandExtension;
	public static OptionsFileCommandExtension optionsFileCommandExtension;
	public static LabelFileCommandExtension labelFileCommandExtension;

	private void registerFileCommands() {
		TASmodAPIRegistry.PLAYBACK_FILE_COMMAND.register(desyncMonitorFileCommandExtension);
		TASmodAPIRegistry.PLAYBACK_FILE_COMMAND.register(optionsFileCommandExtension);
		TASmodAPIRegistry.PLAYBACK_FILE_COMMAND.register(labelFileCommandExtension);

		TASmodAPIRegistry.PLAYBACK_FILE_COMMAND.setConfig(config);
	}

	private static final ConfigurationRegistry CONFIG_REGISTRY = new ConfigurationRegistry();

	private void registerConfigValues() {
		CONFIG_REGISTRY.register(TASmodConfig.values());
	}

	private void loadConfig(Minecraft mc) {
		Path configDir = Paths.get(".").toAbsolutePath().resolve("config");
		if (!Files.exists(configDir)) {
			try {
				Files.createDirectory(configDir);
			} catch (IOException e) {
				LOGGER.catching(e);
			}
		}
		config = new Configuration("TASmod configuration", configDir.resolve("tasmod.cfg"), CONFIG_REGISTRY);

		config.load();
		config.save();
	}
}

