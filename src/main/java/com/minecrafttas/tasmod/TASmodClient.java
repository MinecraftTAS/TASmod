package com.minecrafttas.tasmod;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.lwjgl.input.Keyboard;

import com.minecrafttas.common.Configuration;
import com.minecrafttas.common.Configuration.ConfigOptions;
import com.minecrafttas.common.KeybindManager;
import com.minecrafttas.common.KeybindManager.Keybind;
import com.minecrafttas.common.events.EventClient.EventClientInit;
import com.minecrafttas.common.events.EventClient.EventPlayerJoinedClientSide;
import com.minecrafttas.common.events.EventClient.EventPlayerLeaveClientSide;
import com.minecrafttas.common.events.EventListener;
import com.minecrafttas.server.Client;
import com.minecrafttas.tasmod.externalGui.InputContainerView;
import com.minecrafttas.tasmod.gui.InfoHud;
import com.minecrafttas.tasmod.handlers.InterpolationHandler;
import com.minecrafttas.tasmod.handlers.LoadingScreenHandler;
import com.minecrafttas.tasmod.playback.PlaybackController.TASstate;
import com.minecrafttas.tasmod.playback.PlaybackSerialiser;
import com.minecrafttas.tasmod.playback.server.InitialSyncStatePacket;
import com.minecrafttas.tasmod.playback.server.TASstateClient;
import com.minecrafttas.tasmod.savestates.server.LoadstatePacket;
import com.minecrafttas.tasmod.savestates.server.SavestatePacket;
import com.minecrafttas.tasmod.tickratechanger.TickrateChangerClient;
import com.minecrafttas.tasmod.util.ShieldDownloader;
import com.minecrafttas.tasmod.util.TickScheduler;
import com.minecrafttas.tasmod.virtual.VirtualInput;
import com.minecrafttas.tasmod.virtual.VirtualKeybindings;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.impl.FabricLoaderImpl;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.settings.KeyBinding;

public class TASmodClient implements ClientModInitializer, EventClientInit, EventPlayerJoinedClientSide, EventPlayerLeaveClientSide{


	public static boolean isDevEnvironment;

	public static VirtualInput virtual;

	public static PlaybackSerialiser serialiser = new PlaybackSerialiser();

	public static final String tasdirectory = Minecraft.getMinecraft().mcDataDir.getAbsolutePath() + File.separator + "saves" + File.separator + "tasfiles";

	public static final String savestatedirectory = Minecraft.getMinecraft().mcDataDir.getAbsolutePath() + File.separator + "saves" + File.separator + "savestates";

	public static InfoHud hud;
	
	public static ShieldDownloader shieldDownloader;
	
	public static TickrateChangerClient tickratechanger = new TickrateChangerClient();
	
	public static TickScheduler gameLoopSchedulerClient = new TickScheduler();
	
	public static TickScheduler tickSchedulerClient = new TickScheduler();
	
	public static Configuration config;
	
	public static LoadingScreenHandler loadingScreenHandler;
	
	public static KeybindManager keybindManager;
	
	public static InterpolationHandler interpolation = new InterpolationHandler();
	
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
		EventListener.register(this);
		isDevEnvironment = FabricLoaderImpl.INSTANCE.isDevelopmentEnvironment();
		
		Minecraft mc = Minecraft.getMinecraft();
		config = new Configuration("TASmod configuration", new File(mc.mcDataDir, "config/tasmod.cfg"));
		
		String fileOnStart = config.get(ConfigOptions.FileToOpen);
		
		if (fileOnStart.isEmpty()) {
			fileOnStart = null;
		} else {
			config.reset(ConfigOptions.FileToOpen);
		}
		
		virtual=new VirtualInput(fileOnStart);
		EventListener.register(virtual);
		EventListener.register(virtual.getContainer());

		
		hud = new InfoHud();
		EventListener.register(hud);
		
		shieldDownloader = new ShieldDownloader();
		EventListener.register(shieldDownloader);
		
		loadingScreenHandler = new LoadingScreenHandler();
		EventListener.register(loadingScreenHandler);
		
		keybindManager = new KeybindManager() {
			
			protected boolean isKeyDown(KeyBinding i) {
				return VirtualKeybindings.isKeyDownExceptTextfield(i);
			};
			
		};
		EventListener.register(keybindManager);
		
		EventListener.register(interpolation);
		
		try {
			// connect to server and authenticate
			client = new Client("127.0.0.1", 5555);
			UUID uuid = mc.getSession().getProfile().getId();
			if (uuid == null) // dev environment
				uuid = UUID.randomUUID();
			client.authenticate(uuid);
		} catch (Exception e) {
			TASmod.LOGGER.error("Unable to connect TASmod client: {}", e);
		}
	}

	@Override
	public void onClientInit(Minecraft mc) {
		// initialize keybindings
		List<KeyBinding> blockedKeybindings = new ArrayList<>();
		blockedKeybindings.add(keybindManager.registerKeybind(new Keybind("Tickrate 0 Key", "TASmod", Keyboard.KEY_F8, () -> TASmodClient.tickratechanger.togglePause())));
		blockedKeybindings.add(keybindManager.registerKeybind(new Keybind("Advance Tick", "TASmod", Keyboard.KEY_F9, () -> TASmodClient.tickratechanger.advanceTick())));
		blockedKeybindings.add(keybindManager.registerKeybind(new Keybind("Recording/Playback Stop", "TASmod", Keyboard.KEY_F10, () -> TASstateClient.setOrSend(TASstate.NONE))));
		blockedKeybindings.add(keybindManager.registerKeybind(new Keybind("Create Savestate", "TASmod", Keyboard.KEY_J, () -> TASmodClient.packetClient.sendToServer(new SavestatePacket()))));
		blockedKeybindings.add(keybindManager.registerKeybind(new Keybind("Load Latest Savestate", "TASmod", Keyboard.KEY_K, () -> TASmodClient.packetClient.sendToServer(new LoadstatePacket()))));
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
		// TASmodClient.packetClient.sendToServer(new InitialSyncStatePacket(TASmodClient.virtual.getContainer().getState()));
	}

	@Override
	public void onPlayerLeaveClientSide(EntityPlayerSP player) {
		
	}
	
}
