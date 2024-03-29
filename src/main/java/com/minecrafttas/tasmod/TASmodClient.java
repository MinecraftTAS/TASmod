package com.minecrafttas.tasmod;

import static com.minecrafttas.tasmod.TASmod.LOGGER;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.Level;
import org.lwjgl.input.Keyboard;

import com.minecrafttas.mctcommon.Configuration;
import com.minecrafttas.mctcommon.Configuration.ConfigOptions;
import com.minecrafttas.mctcommon.KeybindManager;
import com.minecrafttas.mctcommon.KeybindManager.Keybind;
import com.minecrafttas.mctcommon.LanguageManager;
import com.minecrafttas.mctcommon.events.EventClient.EventClientInit;
import com.minecrafttas.mctcommon.events.EventClient.EventOpenGui;
import com.minecrafttas.mctcommon.events.EventClient.EventPlayerJoinedClientSide;
import com.minecrafttas.mctcommon.events.EventListenerRegistry;
import com.minecrafttas.mctcommon.server.Client;
import com.minecrafttas.mctcommon.server.PacketHandlerRegistry;
import com.minecrafttas.mctcommon.server.Server;
import com.minecrafttas.tasmod.gui.InfoHud;
import com.minecrafttas.tasmod.handlers.LoadingScreenHandler;
import com.minecrafttas.tasmod.networking.TASmodBufferBuilder;
import com.minecrafttas.tasmod.networking.TASmodPackets;
import com.minecrafttas.tasmod.playback.PlaybackControllerClient;
import com.minecrafttas.tasmod.playback.PlaybackControllerClient.TASstate;
import com.minecrafttas.tasmod.playback.PlaybackSerialiser;
import com.minecrafttas.tasmod.savestates.SavestateHandlerClient;
import com.minecrafttas.tasmod.tickratechanger.TickrateChangerClient;
import com.minecrafttas.tasmod.ticksync.TickSyncClient;
import com.minecrafttas.tasmod.util.LoggerMarkers;
import com.minecrafttas.tasmod.util.Scheduler;
import com.minecrafttas.tasmod.util.ShieldDownloader;
import com.minecrafttas.tasmod.virtual.VirtualInput;
import com.minecrafttas.tasmod.virtual.VirtualKeybindings;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiControls;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.ChatType;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

public class TASmodClient implements ClientModInitializer, EventClientInit, EventPlayerJoinedClientSide, EventOpenGui{


	public static VirtualInput virtual;

	public static TickSyncClient ticksyncClient;
	
	public static PlaybackSerialiser serialiser = new PlaybackSerialiser();

	public static final String tasdirectory = Minecraft.getMinecraft().mcDataDir.getAbsolutePath() + File.separator + "saves" + File.separator + "tasfiles";

	public static final String savestatedirectory = Minecraft.getMinecraft().mcDataDir.getAbsolutePath() + File.separator + "saves" + File.separator + "savestates";

	public static InfoHud hud;
	
	public static ShieldDownloader shieldDownloader;
	
	public static TickrateChangerClient tickratechanger = new TickrateChangerClient();
	
	public static Scheduler gameLoopSchedulerClient = new Scheduler();
	
	public static Scheduler tickSchedulerClient = new Scheduler();
	
	public static Scheduler openMainMenuScheduler = new Scheduler();
	
	public static Configuration config;
	
	public static LoadingScreenHandler loadingScreenHandler;
	
	public static KeybindManager keybindManager;
	
	public static SavestateHandlerClient savestateHandlerClient = new SavestateHandlerClient();
	
	public static Client client;
	/**
	 * The container where all inputs get stored during recording or stored and
	 * ready to be played back
	 */
	public static PlaybackControllerClient controller = new PlaybackControllerClient();

	public static void createTASDir() {
		File tasDir=new File(tasdirectory);
		if(!tasDir.exists()) {
			tasDir.mkdir();
		}
	}
	
	public static void createSavestatesDir() {
		File savestateDir=new File(savestatedirectory);
		if(!savestateDir.exists()) {
			savestateDir.mkdir();
		}
	}

	@Override
	public void onInitializeClient() {
		
		// Load config
		Minecraft mc = Minecraft.getMinecraft();
		
		File configDir = new File(mc.mcDataDir, "config");
		if(!configDir.exists()) {
			configDir.mkdir();
		}
		config = new Configuration("TASmod configuration", new File(configDir, "tasmod.cfg"));
		
		LanguageManager.registerMod("tasmod");

		// Execute /restartandplay. Load the file to start from the config. If it exists load the playback file on start.
		String fileOnStart = config.get(ConfigOptions.FileToOpen);
		if (fileOnStart.isEmpty()) {
			fileOnStart = null;
		} else {
			config.reset(ConfigOptions.FileToOpen);
		}
		virtual=new VirtualInput(LOGGER); //TODO Move fileOnStart to PlaybackController
		
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
		
		// Register event listeners
		EventListenerRegistry.register(this);
//		EventListenerRegistry.register(virtual); TODO Remove if unnecessary
		EventListenerRegistry.register(hud);
		EventListenerRegistry.register(shieldDownloader);
		EventListenerRegistry.register(loadingScreenHandler);
		EventListenerRegistry.register(ticksyncClient);
		EventListenerRegistry.register(keybindManager);
		EventListenerRegistry.register((EventOpenGui)(gui -> {
			if(gui instanceof GuiMainMenu) {
				openMainMenuScheduler.runAllTasks();
			}
			return gui;
		}));
		
		// Register packet handlers
		LOGGER.info(LoggerMarkers.Networking, "Registering network handlers on client");
		PacketHandlerRegistry.register(controller);
		PacketHandlerRegistry.register(ticksyncClient);
		PacketHandlerRegistry.register(tickratechanger);
		PacketHandlerRegistry.register(savestateHandlerClient);
		
		// Starting local server instance
		try {
			TASmod.server = new Server(TASmod.networkingport-1, TASmodPackets.values());
		} catch (Exception e) {
			LOGGER.error("Unable to launch TASmod server: {}", e.getMessage());
		}
		
	}

	@Override
	public void onClientInit(Minecraft mc) {
		// initialize keybindings
		List<KeyBinding> blockedKeybindings = new ArrayList<>();
		blockedKeybindings.add(keybindManager.registerKeybind(new Keybind("Tickrate 0 Key", "TASmod", Keyboard.KEY_F8, () -> TASmodClient.tickratechanger.togglePause(), VirtualKeybindings::isKeyDown)));
		blockedKeybindings.add(keybindManager.registerKeybind(new Keybind("Advance Tick", "TASmod", Keyboard.KEY_F9, () -> TASmodClient.tickratechanger.advanceTick(), VirtualKeybindings::isKeyDown)));
		blockedKeybindings.add(keybindManager.registerKeybind(new Keybind("Recording/Playback Stop", "TASmod", Keyboard.KEY_F10, () -> TASmodClient.controller.setTASState(TASstate.NONE), VirtualKeybindings::isKeyDown)));
		blockedKeybindings.add(keybindManager.registerKeybind(new Keybind("Create Savestate", "TASmod", Keyboard.KEY_J, () -> {
			Minecraft.getMinecraft().ingameGUI.addChatMessage(ChatType.CHAT, new TextComponentString("Savestates might not work correctly at the moment... rewriting a lot of core features, which might break this..."));
			try {
				TASmodClient.client.send(new TASmodBufferBuilder(TASmodPackets.SAVESTATE_SAVE).writeInt(-1));
			} catch (Exception e) {
				e.printStackTrace();
			}
		})));
		blockedKeybindings.add(keybindManager.registerKeybind(new Keybind("Load Latest Savestate", "TASmod", Keyboard.KEY_K, () -> {
			Minecraft.getMinecraft().ingameGUI.addChatMessage(ChatType.CHAT, new TextComponentString(TextFormatting.RED+"Savestates might not work correctly at the moment... rewriting a lot of core features, which might break this..."));
			try {
				TASmodClient.client.send(new TASmodBufferBuilder(TASmodPackets.SAVESTATE_LOAD).writeInt(-1));
			} catch (Exception e) {
				e.printStackTrace();
			}
		})));
		blockedKeybindings.add(keybindManager.registerKeybind(new Keybind("Open InfoGui Editor", "TASmod", Keyboard.KEY_F6, () -> Minecraft.getMinecraft().displayGuiScreen(TASmodClient.hud))));
		blockedKeybindings.add(keybindManager.registerKeybind(new Keybind("Various Testing", "TASmod", Keyboard.KEY_F12, () -> {
			TASmodClient.client.disconnect();
		}, VirtualKeybindings::isKeyDown)));
		blockedKeybindings.add(keybindManager.registerKeybind(new Keybind("Various Testing2", "TASmod", Keyboard.KEY_F7, () -> {
			try {
				TASmodClient.client = new Client("localhost", TASmod.networkingport-1, TASmodPackets.values(), mc.getSession().getProfile().getName(), true);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}, VirtualKeybindings::isKeyDown)));
		blockedKeybindings.forEach(VirtualKeybindings::registerBlockedKeyBinding);
		
		createTASDir();
		createSavestatesDir();
	}

	boolean waszero;
	
	boolean isLoading;

	@Override
	public void onPlayerJoinedClientSide(EntityPlayerSP player) {
		Minecraft mc = Minecraft.getMinecraft();
		ServerData data = mc.getCurrentServerData();
		MinecraftServer server = TASmod.getServerInstance();
		
		String ip = null;
		int port;
		boolean local;
		if(server!=null) {
			ip = "localhost";
			port = TASmod.networkingport-1;
			local = true;
		} else {
			ip = data.serverIP.split(":")[0];
			port = TASmod.networkingport;
			local = false;
		}
		
		String connectedIP = null;
		try {
			connectedIP = client.getRemote();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
		if(!(ip+":"+port).equals(connectedIP)) {
			try {
				LOGGER.info("Closing client connection: {}", client.getRemote());
				client.disconnect();
			} catch (IOException e) {
				e.printStackTrace();
			}
			final String IP = ip;
			final int PORT = port;
			gameLoopSchedulerClient.add(()->{
				try {
					// connect to server and authenticate
					client = new Client(IP, PORT, TASmodPackets.values(), mc.getSession().getUsername(), local);
				} catch (Exception e) {
					LOGGER.error("Unable to connect TASmod client: {}", e.getMessage());
					e.printStackTrace();
				}
				ticksyncClient.setEnabled(true);
			});
		}
	}

	@Override
	public GuiScreen onOpenGui(GuiScreen gui) {
		if (gui instanceof GuiMainMenu) {
			if (client == null) {
				Minecraft mc = Minecraft.getMinecraft();

				String IP = "localhost";
				int PORT = TASmod.networkingport - 1;
				
				// Get the connection on startup from config
				String configAddress = config.get(ConfigOptions.ServerConnection);
				if(configAddress != null && !configAddress.isEmpty()) {
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
					client = new Client(IP, PORT, TASmodPackets.values(), mc.getSession().getUsername(), true);
				} catch (Exception e) {
					LOGGER.error("Unable to connect TASmod client: {}", e);
				}
				ticksyncClient.setEnabled(true);
			}
		} else if (gui instanceof GuiControls) {
			TASmodClient.controller.setTASState(TASstate.NONE); // Set the TASState to nothing to avoid collisions
			if (TASmodClient.tickratechanger.ticksPerSecond == 0) {
				TASmodClient.tickratechanger.pauseClientGame(false); // Unpause the game
				waszero = true;
			}
		} else if (!(gui instanceof GuiControls)) {
			if (waszero) {
				waszero = false;
				TASmodClient.tickratechanger.pauseClientGame(true);
			}
		}
		return gui;
	}
}
