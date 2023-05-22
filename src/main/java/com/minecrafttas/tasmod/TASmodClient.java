package com.minecrafttas.tasmod;

import java.io.File;
import java.io.IOException;

import com.minecrafttas.common.Configuration;
import com.minecrafttas.common.Configuration.ConfigOptions;
import com.minecrafttas.common.KeybindRegistry;
import com.minecrafttas.common.events.EventListener;
import com.minecrafttas.common.events.client.EventClientInit;
import com.minecrafttas.common.events.client.player.EventPlayerJoinedClientSide;
import com.minecrafttas.common.events.client.player.EventPlayerLeaveClientSide;
import com.minecrafttas.tasmod.gui.InfoHud;
import com.minecrafttas.tasmod.handlers.InterpolationHandler;
import com.minecrafttas.tasmod.handlers.KeybindingHandler;
import com.minecrafttas.tasmod.handlers.LoadingScreenHandler;
import com.minecrafttas.tasmod.networking.TASmodNetworkClient;
import com.minecrafttas.tasmod.playback.PlaybackSerialiser;
import com.minecrafttas.tasmod.playback.server.InitialSyncStatePacket;
import com.minecrafttas.tasmod.tickratechanger.TickrateChangerClient;
import com.minecrafttas.tasmod.util.ShieldDownloader;
import com.minecrafttas.tasmod.util.TickScheduler;
import com.minecrafttas.tasmod.virtual.VirtualInput;
import com.minecrafttas.tasmod.virtual.VirtualKeybindings;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.impl.FabricLoaderImpl;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;

public class TASmodClient implements ClientModInitializer, EventClientInit, EventPlayerJoinedClientSide, EventPlayerLeaveClientSide{


	public static boolean isDevEnvironment;

	public static VirtualInput virtual;

	public static PlaybackSerialiser serialiser = new PlaybackSerialiser();

	public static final String tasdirectory = Minecraft.getMinecraft().mcDataDir.getAbsolutePath() + File.separator + "saves" + File.separator + "tasfiles";

	public static final String savestatedirectory = Minecraft.getMinecraft().mcDataDir.getAbsolutePath() + File.separator + "saves" + File.separator + "savestates";

	public static InfoHud hud;
	
	public static ShieldDownloader shieldDownloader;
	
	public static TASmodNetworkClient packetClient;
	
	public static TickrateChangerClient tickratechanger = new TickrateChangerClient();
	
	public static TickScheduler gameLoopSchedulerClient = new TickScheduler();
	
	public static TickScheduler tickSchedulerClient = new TickScheduler();
	
	public static Configuration config;
	
	public static LoadingScreenHandler loadingScreenHandler;
	
	public static KeybindingHandler keybindingHandler;
	
	public static InterpolationHandler interpolation = new InterpolationHandler();
	
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
		shieldDownloader = new ShieldDownloader();
		EventListener.register(shieldDownloader);
		
		loadingScreenHandler = new LoadingScreenHandler();
		EventListener.register(loadingScreenHandler);
		
		keybindingHandler = new KeybindingHandler();
		EventListener.register(keybindingHandler);
		
		EventListener.register(interpolation);
	}

	@Override
	public void onClientInit(Minecraft mc) {
		
		KeybindRegistry.registerKeyBinding(keybindingHandler.tickratezeroKey);
		KeybindRegistry.registerKeyBinding(keybindingHandler.tickAdvance);
		KeybindRegistry.registerKeyBinding(keybindingHandler.stopkey);
		KeybindRegistry.registerKeyBinding(keybindingHandler.savestateSaveKey);
		KeybindRegistry.registerKeyBinding(keybindingHandler.savestateLoadKey);
		KeybindRegistry.registerKeyBinding(keybindingHandler.testingKey);
		KeybindRegistry.registerKeyBinding(keybindingHandler.infoGuiKey);
		KeybindRegistry.registerKeyBinding(keybindingHandler.bufferViewKey);
		
		VirtualKeybindings.registerBlockedKeyBinding(keybindingHandler.tickratezeroKey);
		VirtualKeybindings.registerBlockedKeyBinding(keybindingHandler.tickAdvance);
		VirtualKeybindings.registerBlockedKeyBinding(keybindingHandler.stopkey);
		VirtualKeybindings.registerBlockedKeyBinding(keybindingHandler.savestateSaveKey);
		VirtualKeybindings.registerBlockedKeyBinding(keybindingHandler.savestateLoadKey);
		VirtualKeybindings.registerBlockedKeyBinding(keybindingHandler.testingKey);
		VirtualKeybindings.registerBlockedKeyBinding(keybindingHandler.infoGuiKey);
		VirtualKeybindings.registerBlockedKeyBinding(keybindingHandler.bufferViewKey);

		createTASDir();
		createSavestatesDir();
	}

	boolean waszero;
	
	boolean isLoading;

	@Override
	public void onPlayerJoinedClientSide(EntityPlayerSP player) {
		Minecraft mc = Minecraft.getMinecraft();
		
		if(mc.isIntegratedServerRunning())
			TASmodClient.packetClient = new TASmodNetworkClient(TASmod.logger);
		else {
			String full = mc.getCurrentServerData().serverIP;
			String[] fullsplit = full.split(":");
			if(fullsplit.length == 1) {
				TASmodClient.packetClient = new TASmodNetworkClient(TASmod.logger, full, 3111);
			} else if(fullsplit.length == 2){
				String ip = fullsplit[0];
				TASmodClient.packetClient = new TASmodNetworkClient(TASmod.logger, ip, 3111);
			} else {
				System.err.println("Something went wrong while connecting. The ip seems to be wrong");
			}
		}
		
		TASmodClient.packetClient.sendToServer(new InitialSyncStatePacket(TASmodClient.virtual.getContainer().getState()));
		
		
	}

	@Override
	public void onPlayerLeaveClientSide(EntityPlayerSP player) {
		try {
			if(TASmodClient.packetClient!=null) {
				TASmodClient.packetClient.killClient();
				TASmodClient.packetClient=null;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
