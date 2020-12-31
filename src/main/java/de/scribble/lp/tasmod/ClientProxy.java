package de.scribble.lp.tasmod;

<<<<<<< Updated upstream
import org.lwjgl.input.Keyboard;

import de.scribble.lp.tasmod.savestates.SavestateHandlerClient;
=======
import de.pfannekuchen.tasmod.events.AimAssistEvents;
>>>>>>> Stashed changes
import de.scribble.lp.tasmod.tutorial.TutorialHandler;
import de.scribble.lp.tasmod.virtual.VirtualKeybindings;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import scala.swing.event.Key;

public class ClientProxy extends CommonProxy{
	
	public static Configuration config;
	
	private static TutorialHandler playbackTutorial;
	
	public static boolean isDevEnvironment;
	
	private static SavestateHandlerClient saveHandler;
	
	private static VirtualKeybindings vkeys;
	
	public static KeyBinding tickratezeroKey= new KeyBinding("Tickrate 0 Key", Keyboard.KEY_F8, "TASmod");
	
	public static KeyBinding tickAdvance= new KeyBinding("Tickadvance Key", Keyboard.KEY_F9, "TASmod");
	
	public static KeyBinding stopkey= new KeyBinding("Recording/Playback Stop", Keyboard.KEY_F10, "TASmod");
	
	
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
		
		saveHandler=new SavestateHandlerClient();
		vkeys=new VirtualKeybindings();
		
		ClientRegistry.registerKeyBinding(tickratezeroKey);
		ClientRegistry.registerKeyBinding(tickAdvance);
		ClientRegistry.registerKeyBinding(stopkey);
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
	public static VirtualKeybindings getVkeys() {
		return vkeys;
	}
}
