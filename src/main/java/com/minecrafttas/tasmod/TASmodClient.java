package com.minecrafttas.tasmod;

import static com.minecrafttas.tasmod.TASmod.LOGGER;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.input.Keyboard;

import com.minecrafttas.common.Configuration;
import com.minecrafttas.common.Configuration.ConfigOptions;
import com.minecrafttas.common.KeybindManager;
import com.minecrafttas.common.KeybindManager.Keybind;
import com.minecrafttas.common.events.EventClient.EventClientInit;
import com.minecrafttas.common.events.EventClient.EventOpenGui;
import com.minecrafttas.common.events.EventClient.EventPlayerJoinedClientSide;
import com.minecrafttas.common.events.EventClient.EventPlayerLeaveClientSide;
import com.minecrafttas.common.events.EventListenerRegistry;
import com.minecrafttas.common.server.Client;
import com.minecrafttas.common.server.PacketHandlerRegistry;
import com.minecrafttas.common.server.Server;
import com.minecrafttas.tasmod.externalGui.InputContainerView;
import com.minecrafttas.tasmod.gui.InfoHud;
import com.minecrafttas.tasmod.handlers.InterpolationHandler;
import com.minecrafttas.tasmod.handlers.LoadingScreenHandler;
import com.minecrafttas.tasmod.networking.TASmodBufferBuilder;
import com.minecrafttas.tasmod.networking.TASmodPackets;
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
import net.fabricmc.loader.impl.FabricLoaderImpl;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.settings.KeyBinding;

public class TASmodClient implements ClientModInitializer, EventClientInit, EventPlayerJoinedClientSide, EventPlayerLeaveClientSide{


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
	
	public static InterpolationHandler interpolation = new InterpolationHandler();
	
	public static SavestateHandlerClient savestateHandlerClient = new SavestateHandlerClient();
	
	public static Client client;
	
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
		config = new Configuration("TASmod configuration", new File(mc.mcDataDir, "config/tasmod.cfg"));
		
		// Execute /restartandplay. Load the file to start from the config. If it exists load the playback file on start.
		String fileOnStart = config.get(ConfigOptions.FileToOpen);
		if (fileOnStart.isEmpty()) {
			fileOnStart = null;
		} else {
			config.reset(ConfigOptions.FileToOpen);
		}
		virtual=new VirtualInput(fileOnStart);
		
		// Initialize InfoHud
		hud = new InfoHud();
		// Initialize shield downloader
		shieldDownloader = new ShieldDownloader();
		// Initialize loading screen handler
		loadingScreenHandler = new LoadingScreenHandler();
		// Initialize Ticksync
		ticksyncClient = new TickSyncClient();
		// Initialize keybind manager
		keybindManager = new KeybindManager() {
			
			protected boolean isKeyDown(KeyBinding i) {
				return VirtualKeybindings.isKeyDownExceptTextfield(i);
			};
			
		};
		
		// Register event listeners
		EventListenerRegistry.register(this);
		EventListenerRegistry.register(virtual);
		EventListenerRegistry.register(hud);
		EventListenerRegistry.register(shieldDownloader);
		EventListenerRegistry.register(loadingScreenHandler);
		EventListenerRegistry.register(ticksyncClient);
		EventListenerRegistry.register(keybindManager);
		EventListenerRegistry.register(interpolation);
		EventListenerRegistry.register((EventOpenGui)(gui -> {
			if(gui instanceof GuiMainMenu) {
				openMainMenuScheduler.runAllTasks();
			}
			return gui;
		}));
		
		// Register packet handlers
		LOGGER.info(LoggerMarkers.Networking, "Registering network handlers on client");
		PacketHandlerRegistry.register(virtual.getContainer());	//TODO Move container/playbackcontroller out of virtual package
		PacketHandlerRegistry.register(ticksyncClient);
		PacketHandlerRegistry.register(tickratechanger);
		PacketHandlerRegistry.register(savestateHandlerClient);
		
		// Starting local server instance
		try {
			TASmod.server = new Server(TASmod.networkingport-1, TASmodPackets.values());
		} catch (Exception e) {
			LOGGER.error("Unable to launch TASmod server: {}", e.getMessage());
		}
		
		// Connecting to local server instance
		try {
			// connect to server and authenticate
			client = new Client("localhost", TASmod.networkingport-1, TASmodPackets.values(), mc.getSession().getUsername());
		} catch (Exception e) {
			LOGGER.error("Unable to connect TASmod client: {}", e.getMessage());
		}
	}

	@Override
	public void onClientInit(Minecraft mc) {
		// initialize keybindings
		List<KeyBinding> blockedKeybindings = new ArrayList<>();
		blockedKeybindings.add(keybindManager.registerKeybind(new Keybind("Tickrate 0 Key", "TASmod", Keyboard.KEY_F8, () -> TASmodClient.tickratechanger.togglePause())));
		blockedKeybindings.add(keybindManager.registerKeybind(new Keybind("Advance Tick", "TASmod", Keyboard.KEY_F9, () -> TASmodClient.tickratechanger.advanceTick())));
		blockedKeybindings.add(keybindManager.registerKeybind(new Keybind("Recording/Playback Stop", "TASmod", Keyboard.KEY_F10, () -> TASmodClient.virtual.getContainer().setTASState(TASstate.NONE))));
		blockedKeybindings.add(keybindManager.registerKeybind(new Keybind("Create Savestate", "TASmod", Keyboard.KEY_J, () -> {
			try {
				TASmodClient.client.send(new TASmodBufferBuilder(TASmodPackets.SAVESTATE_SAVE).writeInt(-1));
			} catch (Exception e) {
				e.printStackTrace();
			}
		})));
		blockedKeybindings.add(keybindManager.registerKeybind(new Keybind("Load Latest Savestate", "TASmod", Keyboard.KEY_K, () -> {
			try {
				TASmodClient.client.send(new TASmodBufferBuilder(TASmodPackets.SAVESTATE_LOAD).writeInt(-1));
			} catch (Exception e) {
				e.printStackTrace();
			}
		})));
		blockedKeybindings.add(keybindManager.registerKeybind(new Keybind("Open InfoGui Editor", "TASmod", Keyboard.KEY_F6, () -> Minecraft.getMinecraft().displayGuiScreen(TASmodClient.hud))));
		blockedKeybindings.add(keybindManager.registerKeybind(new Keybind("Buffer View", "TASmod", Keyboard.KEY_NUMPAD0, () -> InputContainerView.startBufferView())));
		blockedKeybindings.add(keybindManager.registerKeybind(new Keybind("Various Testing", "TASmod", Keyboard.KEY_F12, () -> {
			TASmod.tickSchedulerServer.add(() -> {
				try {
					Thread.sleep(1000L);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			});
		})));
		blockedKeybindings.forEach(VirtualKeybindings::registerBlockedKeyBinding);
		
		createTASDir();
		createSavestatesDir();
	}

	boolean waszero;
	
	boolean isLoading;

	@Override
	public void onPlayerJoinedClientSide(EntityPlayerSP player) {
		// FIXME: ask how this works
		
		/* == Scribble ==
		 * The playback state (Playing, Recording, Paused, None) of the client may be different from the server state, 
		 * since we allow the fact that the player can start a playback in the main menu.
		 * 
		 *  So when joining the world, the player sends their current state over to the server. If another player is already on the server,
		 *  then the server sends back the current server state, so everyone has the same playback state.
		 *  
		 *  Will be obsolete once we have a networking system that starts in the main menu. Then we can sync the state from there
		*/
		// TASmodClient.packetClient.sendToServer(new InitialSyncStatePacket(TASmodClient.virtual.getContainer().getState()));
		Minecraft mc = Minecraft.getMinecraft();
		String full = mc.getCurrentServerData().serverIP;
		String ip = full.split(":")[0];
		
		String connectedIP = null;
		try {
			connectedIP = client.getRemote();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		System.out.println(full);
		System.out.println(connectedIP);
		
		if(!(ip+TASmod.networkingport).equals(full)) {
			try {
				client.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				// connect to server and authenticate
				client = new Client(ip, TASmod.networkingport, TASmodPackets.values(), mc.getSession().getUsername());
			} catch (Exception e) {
				LOGGER.error("Unable to connect TASmod client: {}", e.getMessage());
			}
		}
	}

	@Override
	public void onPlayerLeaveClientSide(EntityPlayerSP player) {
		
	}
	
}
