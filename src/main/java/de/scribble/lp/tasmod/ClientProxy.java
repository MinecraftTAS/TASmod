package de.scribble.lp.tasmod;

import java.io.File;

import org.lwjgl.input.Keyboard;

import de.pfannekuchen.infogui.gui.InfoHud;
import de.pfannekuchen.tasmod.events.AimAssistEvents;
import de.pfannekuchen.tasmod.events.CameraInterpolationEvents;
import de.scribble.lp.tasmod.commands.tutorial.TutorialHandler;
import de.scribble.lp.tasmod.shield.ShieldDownloader;
import de.scribble.lp.tasmod.util.ContainerSerialiser;
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

	public static KeyBinding tickratezeroKey = new KeyBinding("Tickrate 0 Key", Keyboard.KEY_F8, "TASmod");

	public static KeyBinding tickAdvance = new KeyBinding("Advance Tick", Keyboard.KEY_F9, "TASmod");

	public static KeyBinding showNextLocation = new KeyBinding("Show Next Location", Keyboard.KEY_O, "TASmod");

	public static KeyBinding stopkey = new KeyBinding("Recording/Playback Stop", Keyboard.KEY_F10, "TASmod");

	public static KeyBinding savestateSaveKey = new KeyBinding("Create Savestate", Keyboard.KEY_J, "TASmod");

	public static KeyBinding savestateLoadKey = new KeyBinding("Load Latest Savestate", Keyboard.KEY_K, "TASmod");

	public static KeyBinding testingKey = new KeyBinding("Various Testing", Keyboard.KEY_F12, "TASmod");

	public static KeyBinding infoGuiKey = new KeyBinding("Open InfoGui Editor", Keyboard.KEY_F6, "TASmod");

	public static VirtualInput virtual = new VirtualInput();

	public static ContainerSerialiser serialiser = new ContainerSerialiser();

	public static final String tasdirectory = Minecraft.getMinecraft().mcDataDir.getAbsolutePath() + File.separator + "saves" + File.separator + "tasfiles";

	public static final String savestatedirectory = Minecraft.getMinecraft().mcDataDir.getAbsolutePath() + File.separator + "saves" + File.separator + "savestates";

	public static InfoHud hud;
	
	public static ShieldDownloader shieldDownloader;
	
	public void preInit(FMLPreInitializationEvent ev) {
		isDevEnvironment = (Boolean) Launch.blackboard.get("fml.deobfuscatedEnvironment");
		playbackTutorial = new TutorialHandler((short) 1);
		
		config = new Configuration(ev.getSuggestedConfigurationFile());
		Config.reloadClientConfig(config);

		super.preInit(ev);
	}

	public void init(FMLInitializationEvent ev) {
		hud = new InfoHud();
		shieldDownloader = new ShieldDownloader();

		MinecraftForge.EVENT_BUS.register(new InfoGui());
		MinecraftForge.EVENT_BUS.register(playbackTutorial);
		MinecraftForge.EVENT_BUS.register(new AimAssistEvents());
		MinecraftForge.EVENT_BUS.register(new CameraInterpolationEvents());

		ClientRegistry.registerKeyBinding(tickratezeroKey);
		ClientRegistry.registerKeyBinding(tickAdvance);
		ClientRegistry.registerKeyBinding(stopkey);
		ClientRegistry.registerKeyBinding(showNextLocation);
		ClientRegistry.registerKeyBinding(savestateSaveKey);
		ClientRegistry.registerKeyBinding(savestateLoadKey);
		ClientRegistry.registerKeyBinding(testingKey);
		ClientRegistry.registerKeyBinding(infoGuiKey);

		VirtualKeybindings.registerBlockedKeyBinding(infoGuiKey);
		VirtualKeybindings.registerBlockedKeyBinding(tickratezeroKey);
		VirtualKeybindings.registerBlockedKeyBinding(tickAdvance);
		VirtualKeybindings.registerBlockedKeyBinding(stopkey);
		VirtualKeybindings.registerBlockedKeyBinding(testingKey);

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
