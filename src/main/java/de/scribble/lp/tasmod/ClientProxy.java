package de.scribble.lp.tasmod;

import java.io.File;

import org.lwjgl.input.Keyboard;

import de.pfannekuchen.tasmod.events.AimAssistEvents;
import de.pfannekuchen.tasmod.events.CameraInterpolationEvents;
import de.scribble.lp.tasmod.savestates.SavestateEvents;
import de.scribble.lp.tasmod.savestates.motion.MotionEvents;
import de.scribble.lp.tasmod.tutorial.TutorialHandler;
import de.scribble.lp.tasmod.virtual.VirtualKeybindings;
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
	
	public static KeyBinding tickratezeroKey= new KeyBinding("Tickrate 0 Key", Keyboard.KEY_F8, "TASmod");
	
	public static KeyBinding tickAdvance= new KeyBinding("Advance Tick", Keyboard.KEY_F9, "TASmod");
	
	public static KeyBinding showNextLocation= new KeyBinding("Show Next Location", Keyboard.KEY_O, "TASmod");
	
	public static KeyBinding stopkey= new KeyBinding("Recording/Playback Stop", Keyboard.KEY_F10, "TASmod");
	
	public static KeyBinding savestateSaveKey = new KeyBinding("Create Savestate", Keyboard.KEY_J, "TASmod");
	
	public static KeyBinding savestateLoadKey = new KeyBinding("Load Latest Savestate", Keyboard.KEY_K, "TASmod");
	
	public static KeyBinding testingKey = new KeyBinding("Various Testing", Keyboard.KEY_F12, "TASmod");
	
	
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
		MinecraftForge.EVENT_BUS.register(new CameraInterpolationEvents());
		
		MinecraftForge.EVENT_BUS.register(new SavestateEvents());
		MinecraftForge.EVENT_BUS.register(new MotionEvents());
		
		ClientRegistry.registerKeyBinding(tickratezeroKey);
		ClientRegistry.registerKeyBinding(tickAdvance);
		ClientRegistry.registerKeyBinding(stopkey);
		ClientRegistry.registerKeyBinding(showNextLocation);
		ClientRegistry.registerKeyBinding(savestateSaveKey);
		ClientRegistry.registerKeyBinding(savestateLoadKey);
		ClientRegistry.registerKeyBinding(testingKey);
		
		VirtualKeybindings.registerBlockedKeyBinding(tickratezeroKey);
		VirtualKeybindings.registerBlockedKeyBinding(tickAdvance);
		VirtualKeybindings.registerBlockedKeyBinding(stopkey);
		VirtualKeybindings.registerBlockedKeyBinding(testingKey);
		
		new File (Minecraft.getMinecraft().mcDataDir,"saves"+File.separator+"savestates").mkdir();
		super.init(ev);
	}
	public void postInit(FMLPostInitializationEvent ev) {
		super.postInit(ev);
	}
	public static TutorialHandler getPlaybackTutorial() {
		return playbackTutorial;
	}
}
