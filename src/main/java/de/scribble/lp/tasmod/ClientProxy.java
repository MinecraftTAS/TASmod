package de.scribble.lp.tasmod;

import org.lwjgl.input.Keyboard;

import de.scribble.lp.tasmod.tutorial.TutorialHandler;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class ClientProxy extends CommonProxy{
	
	public static Configuration config;
	
	static TutorialHandler playbackTutorial;
	
	public void preInit(FMLPreInitializationEvent ev) {
		config = new Configuration(ev.getSuggestedConfigurationFile());
		Config.reloadClientConfig(config);
		super.preInit(ev);
	}
	public void init(FMLInitializationEvent ev) {
		playbackTutorial=new TutorialHandler((short)1);
		MinecraftForge.EVENT_BUS.register(new InfoGui());
		MinecraftForge.EVENT_BUS.register(playbackTutorial);
		
		super.init(ev);
	}
	public void postInit(FMLPostInitializationEvent ev) {
		super.postInit(ev);
	}
	public static TutorialHandler getPlaybackTutorial() {
		return playbackTutorial;
	}
}
