package com.minecrafttas.tasmod;

import java.io.File;

import org.lwjgl.input.Keyboard;

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
import net.minecraft.client.settings.KeyBinding;

public class TASmodClient implements ClientModInitializer {


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
		isDevEnvironment = FabricLoaderImpl.INSTANCE.isDevelopmentEnvironment();
		
//		if(fileOnStart.isEmpty()) {
//			fileOnStart=null;
//		}
		virtual=new VirtualInput(null);
		
		hud = new InfoHud();
		shieldDownloader = new ShieldDownloader();

		
		//TODO Keybind registering
//		ClientRegistry.registerKeyBinding(KeybindingEvents.tickratezeroKey);
//		ClientRegistry.registerKeyBinding(KeybindingEvents.tickAdvance);
//		ClientRegistry.registerKeyBinding(KeybindingEvents.stopkey);
//		ClientRegistry.registerKeyBinding(KeybindingEvents.savestateSaveKey);
//		ClientRegistry.registerKeyBinding(KeybindingEvents.savestateLoadKey);
//		ClientRegistry.registerKeyBinding(KeybindingEvents.testingKey);
//		ClientRegistry.registerKeyBinding(KeybindingEvents.infoGuiKey);
//		ClientRegistry.registerKeyBinding(KeybindingEvents.bufferViewKey);
		
		if(TASmod.ktrngHandler.isLoaded()) {
			KeybindingEvents.ktrngKey=new KeyBinding("KTRNG SeedChange Pause", Keyboard.KEY_B, "TASmod");
//			ClientRegistry.registerKeyBinding(KeybindingEvents.ktrngKey);
			VirtualKeybindings.registerBlockedKeyBinding(KeybindingEvents.ktrngKey);
		}

		
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
