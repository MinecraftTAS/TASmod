package de.scribble.lp.tasmod;

import java.io.File;

import org.lwjgl.input.Keyboard;

import de.pfannekuchen.infogui.gui.InfoHud;
import de.pfannekuchen.tasmod.events.AimAssistEvents;
import de.pfannekuchen.tasmod.events.CameraInterpolationEvents;
import de.scribble.lp.tasmod.commands.tutorial.TutorialHandler;
import de.scribble.lp.tasmod.events.KeybindingEvents;
import de.scribble.lp.tasmod.events.PlayerJoinLeaveEvents;
import de.scribble.lp.tasmod.shield.ShieldDownloader;
import de.scribble.lp.tasmod.util.ContainerSerialiser;
import de.scribble.lp.tasmod.util.TickScheduler;
import de.scribble.lp.tasmod.virtual.VirtualInput;
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

public class ClientProxy extends CommonProxy {

	public static Configuration config;

	private static TutorialHandler playbackTutorial;

	public static boolean isDevEnvironment;

	public static VirtualInput virtual;

	public static ContainerSerialiser serialiser = new ContainerSerialiser();

	public static final String tasdirectory = Minecraft.getMinecraft().mcDataDir.getAbsolutePath() + File.separator + "saves" + File.separator + "tasfiles";

	public static final String savestatedirectory = Minecraft.getMinecraft().mcDataDir.getAbsolutePath() + File.separator + "saves" + File.separator + "savestates";

	public static InfoHud hud;
	
	public static ShieldDownloader shieldDownloader;
	
//	public static TickSyncClient ticksyncClient = new TickSyncClient();
	
	public static TickScheduler tickSchedulerClient = new TickScheduler();
	
	public void preInit(FMLPreInitializationEvent ev) {
		isDevEnvironment = (Boolean) Launch.blackboard.get("fml.deobfuscatedEnvironment");
		playbackTutorial = new TutorialHandler((short) 1);
		
		config = new Configuration(ev.getSuggestedConfigurationFile());
		Config.reloadClientConfig(config);

		config.load();
		String fileOnStart=config.get("General", "fileToLoad", "").getString();
		config.get("General", "fileToLoad", "").set("");
		config.save();
		
		if(fileOnStart.isEmpty()) {
			fileOnStart=null;
		}
		virtual=new VirtualInput(fileOnStart);
		
		super.preInit(ev);
	}

	public void init(FMLInitializationEvent ev) {
		
		hud = new InfoHud();
		shieldDownloader = new ShieldDownloader();

		MinecraftForge.EVENT_BUS.register(new InfoGui());
		MinecraftForge.EVENT_BUS.register(playbackTutorial);
		MinecraftForge.EVENT_BUS.register(new AimAssistEvents());
		MinecraftForge.EVENT_BUS.register(new CameraInterpolationEvents());
		
		//It pains me to do this ._.
		MinecraftForge.EVENT_BUS.register(new PlayerJoinLeaveEvents());

		ClientRegistry.registerKeyBinding(KeybindingEvents.tickratezeroKey);
		ClientRegistry.registerKeyBinding(KeybindingEvents.tickAdvance);
		ClientRegistry.registerKeyBinding(KeybindingEvents.stopkey);
		ClientRegistry.registerKeyBinding(KeybindingEvents.savestateSaveKey);
		ClientRegistry.registerKeyBinding(KeybindingEvents.savestateLoadKey);
		ClientRegistry.registerKeyBinding(KeybindingEvents.testingKey);
		ClientRegistry.registerKeyBinding(KeybindingEvents.infoGuiKey);
		ClientRegistry.registerKeyBinding(KeybindingEvents.bufferViewKey);
		
		if(TASmod.ktrngHandler.isLoaded()) {
			KeybindingEvents.ktrngKey=new KeyBinding("KTRNG SeedChange Pause", Keyboard.KEY_B, "TASmod");
			ClientRegistry.registerKeyBinding(KeybindingEvents.ktrngKey);
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

		super.init(ev);
	}

	public void postInit(FMLPostInitializationEvent ev) {
		super.postInit(ev);
	}

	public static TutorialHandler getPlaybackTutorial() {
		return playbackTutorial;
	}
	
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
}
