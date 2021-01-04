package de.scribble.lp.tasmod;

import java.io.File;

import org.lwjgl.input.Keyboard;

import de.pfannekuchen.tasmod.events.AimAssistEvents;
import de.scribble.lp.tasmod.savestates.SavestateEvents;
import de.scribble.lp.tasmod.savestates.SavestateHandlerClient;
import de.scribble.lp.tasmod.tutorial.TutorialHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class ClientProxy extends CommonProxy{
	
	public static Configuration config;
	
	private static TutorialHandler playbackTutorial;
	
	public static boolean isDevEnvironment;
	
	private static SavestateHandlerClient saveHandler;
	
	public static KeyBinding tickratezeroKey= new KeyBinding("Tickrate 0 Key", Keyboard.KEY_F8, "TASmod");
	
	public static KeyBinding tickAdvance= new KeyBinding("Advance Tick", Keyboard.KEY_F9, "TASmod");
	
	public static KeyBinding showNextLocation= new KeyBinding("Show Next Location", Keyboard.KEY_O, "TASmod");
	
	public static KeyBinding stopkey= new KeyBinding("Recording/Playback Stop", Keyboard.KEY_F10, "TASmod");
	
	public static KeyBinding savestateSaveKey = new KeyBinding("Create Savestate", Keyboard.KEY_J, "TASmod");
	
	public static KeyBinding savestateLoadKey = new KeyBinding("Load Latest Savestate", Keyboard.KEY_K, "TASmod");
	
	
	public void preInit(FMLPreInitializationEvent ev) {
		isDevEnvironment=(Boolean) Launch.blackboard.get("fml.deobfuscatedEnvironment");
		config = new Configuration(ev.getSuggestedConfigurationFile());
		Config.reloadClientConfig(config);
		
		super.preInit(ev);
	}
	public void init(FMLInitializationEvent ev) {
		playbackTutorial=new TutorialHandler((short)1);
		MinecraftForge.EVENT_BUS.register(new InfoGui());
		MinecraftForge.EVENT_BUS.register(playbackTutorial);
		MinecraftForge.EVENT_BUS.register(new AimAssistEvents());
		MinecraftForge.EVENT_BUS.register(new SavestateEvents());
		
		saveHandler=new SavestateHandlerClient();
		
		ClientRegistry.registerKeyBinding(tickratezeroKey);
		ClientRegistry.registerKeyBinding(tickAdvance);
		ClientRegistry.registerKeyBinding(stopkey);
		ClientRegistry.registerKeyBinding(showNextLocation);
		
		new File (Minecraft.getMinecraft().mcDataDir,"saves"+File.separator+"savestates").mkdir();
		super.init(ev);
	}
	public void postInit(FMLPostInitializationEvent ev) {
		super.postInit(ev);
	}
	public static TutorialHandler getPlaybackTutorial() {
		return playbackTutorial;
	}
	public static SavestateHandlerClient getSaveHandler() {
		return saveHandler;
	}
}
