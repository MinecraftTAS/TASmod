package com.minecrafttas.tasmod;

import java.io.File;

import com.minecrafttas.common.KeybindRegistry;
import com.minecrafttas.common.events.EventListener;
import com.minecrafttas.common.events.client.EventClientInit;
import com.minecrafttas.tasmod.events.KeybindingEvents;
import com.minecrafttas.tasmod.gui.InfoHud;
import com.minecrafttas.tasmod.networking.TASmodNetworkClient;
import com.minecrafttas.tasmod.playback.PlaybackSerialiser;
import com.minecrafttas.tasmod.util.ShieldDownloader;
import com.minecrafttas.tasmod.util.TickScheduler;
import com.minecrafttas.tasmod.virtual.VirtualInput;
import com.minecrafttas.tasmod.virtual.VirtualKeybindings;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.impl.FabricLoaderImpl;
import net.minecraft.client.Minecraft;

public class TASmodClient implements ClientModInitializer, EventClientInit {


	public static boolean isDevEnvironment;

	public static VirtualInput virtual;

	public static PlaybackSerialiser serialiser = new PlaybackSerialiser();

	public static final String tasdirectory = Minecraft.getMinecraft().mcDataDir.getAbsolutePath() + File.separator + "saves" + File.separator + "tasfiles";

	public static final String savestatedirectory = Minecraft.getMinecraft().mcDataDir.getAbsolutePath() + File.separator + "saves" + File.separator + "savestates";

	public static InfoHud hud;
	
	public static ShieldDownloader shieldDownloader;
	
	public static TASmodNetworkClient packetClient;
	
	public static TickScheduler gameLoopSchedulerClient = new TickScheduler();
	
	public static TickScheduler tickSchedulerClient = new TickScheduler();
	
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
		
//		if(fileOnStart.isEmpty()) {
//			fileOnStart=null;
//		}
		virtual=new VirtualInput(null);
		
		hud = new InfoHud();
		shieldDownloader = new ShieldDownloader();
		
	}

	@Override
	public void onClientInit(Minecraft mc) {
		KeybindRegistry.registerKeyBinding(KeybindingEvents.tickratezeroKey);
		KeybindRegistry.registerKeyBinding(KeybindingEvents.tickAdvance);
		KeybindRegistry.registerKeyBinding(KeybindingEvents.stopkey);
		KeybindRegistry.registerKeyBinding(KeybindingEvents.savestateSaveKey);
		KeybindRegistry.registerKeyBinding(KeybindingEvents.savestateLoadKey);
		KeybindRegistry.registerKeyBinding(KeybindingEvents.testingKey);
		KeybindRegistry.registerKeyBinding(KeybindingEvents.infoGuiKey);
		KeybindRegistry.registerKeyBinding(KeybindingEvents.bufferViewKey);
		
		VirtualKeybindings.registerBlockedKeyBinding(KeybindingEvents.tickratezeroKey);
		VirtualKeybindings.registerBlockedKeyBinding(KeybindingEvents.tickAdvance);
		VirtualKeybindings.registerBlockedKeyBinding(KeybindingEvents.stopkey);
		VirtualKeybindings.registerBlockedKeyBinding(KeybindingEvents.savestateSaveKey);
		VirtualKeybindings.registerBlockedKeyBinding(KeybindingEvents.savestateLoadKey);
		VirtualKeybindings.registerBlockedKeyBinding(KeybindingEvents.testingKey);
		VirtualKeybindings.registerBlockedKeyBinding(KeybindingEvents.infoGuiKey);
		VirtualKeybindings.registerBlockedKeyBinding(KeybindingEvents.bufferViewKey);

		createTASDir();
		createSavestatesDir();
	}
}
