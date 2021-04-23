package de.scribble.lp.tasmod;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import org.lwjgl.input.Keyboard;

import de.pfannekuchen.infogui.gui.SettingsGui;
import de.pfannekuchen.tasmod.events.AimAssistEvents;
import de.pfannekuchen.tasmod.events.CameraInterpolationEvents;
import de.scribble.lp.tasmod.savestates.SavestateEvents;
import de.scribble.lp.tasmod.savestates.motion.MotionEvents;
import de.scribble.lp.tasmod.tutorial.TutorialHandler;
import de.scribble.lp.tasmod.util.ContainerSerialiser;
import de.scribble.lp.tasmod.virtual.VirtualInput2;
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

	public static VirtualInput2 virtual = new VirtualInput2();

	public static ContainerSerialiser serialiser = new ContainerSerialiser();

	public static final String tasdirectory = Minecraft.getMinecraft().mcDataDir.getAbsolutePath() + File.separator + "saves" + File.separator + "tasfiles";

	public static final String savestatedirectory = Minecraft.getMinecraft().mcDataDir.getAbsolutePath() + "saves" + File.separator + "savestates";

	public void preInit(FMLPreInitializationEvent ev) {
		isDevEnvironment = (Boolean) Launch.blackboard.get("fml.deobfuscatedEnvironment");
		config = new Configuration(ev.getSuggestedConfigurationFile());
		Config.reloadClientConfig(config);

		super.preInit(ev);
	}

	public void init(FMLInitializationEvent ev) {
		try {
			SettingsGui.load(); // This goes first.. muhahahahaha
			Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {

				@Override
				public void run() {
					try {
						SettingsGui.save();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}));
		} catch (IOException e) {
			SettingsGui.p = new Properties();
			SettingsGui.p.setProperty("XYZ_visible", "true");
			SettingsGui.p.setProperty("XYZPRECISE_visible", "false");
			SettingsGui.p.setProperty("CXZ_visible", "false");
			SettingsGui.p.setProperty("WORLDSEED_visible", "false");
			SettingsGui.p.setProperty("RNGSEEDS_visible", "false");
			SettingsGui.p.setProperty("FACING_visible", "false");
			SettingsGui.p.setProperty("TICKS_visible", "false");
			SettingsGui.p.setProperty("TICKRATE_visible", "false");
			SettingsGui.p.setProperty("SAVESTATECOUNT_visible", "false");
			SettingsGui.p.setProperty("PREDICTEDXYZ_visible", "false");
			SettingsGui.p.setProperty("MOUSEPOS_visible", "false");
			SettingsGui.p.setProperty("TRAJECTORIES_visible", "false");

			SettingsGui.p.setProperty("XYZ_x", "0");
			SettingsGui.p.setProperty("XYZPRECISE_x", "0");
			SettingsGui.p.setProperty("CXZ_x", "0");
			SettingsGui.p.setProperty("WORLDSEED_x", "0");
			SettingsGui.p.setProperty("RNGSEEDS_x", "0");
			SettingsGui.p.setProperty("FACING_x", "0");
			SettingsGui.p.setProperty("TICKS_x", "0");
			SettingsGui.p.setProperty("TICKRATE_x", "0");
			SettingsGui.p.setProperty("SAVESTATECOUNT_x", "0");
			SettingsGui.p.setProperty("PREDICTEDXYZ_x", "0");
			SettingsGui.p.setProperty("MOUSEPOS_x", "0");
			SettingsGui.p.setProperty("TRAJECTORIES_x", "0");

			SettingsGui.p.setProperty("XYZ_y", "0");
			SettingsGui.p.setProperty("XYZPRECISE_y", "0");
			SettingsGui.p.setProperty("CXZ_y", "0");
			SettingsGui.p.setProperty("WORLDSEED_y", "0");
			SettingsGui.p.setProperty("RNGSEEDS_y", "0");
			SettingsGui.p.setProperty("FACING_y", "0");
			SettingsGui.p.setProperty("TICKS_y", "0");
			SettingsGui.p.setProperty("TICKRATE_y", "0");
			SettingsGui.p.setProperty("SAVESTATECOUNT_y", "0");
			SettingsGui.p.setProperty("PREDICTEDXYZ_y", "0");
			SettingsGui.p.setProperty("MOUSEPOS_y", "0");
			SettingsGui.p.setProperty("TRAJECTORIES_y", "0");

			SettingsGui.p.setProperty("XYZ_hideRect", "false");
			SettingsGui.p.setProperty("XYZPRECISE_hideRect", "false");
			SettingsGui.p.setProperty("CXZ_hideRect", "false");
			SettingsGui.p.setProperty("WORLDSEED_hideRect", "false");
			SettingsGui.p.setProperty("RNGSEEDS_hideRect", "false");
			SettingsGui.p.setProperty("FACING_hideRect", "false");
			SettingsGui.p.setProperty("TICKS_hideRect", "false");
			SettingsGui.p.setProperty("TICKRATE_hideRect", "false");
			SettingsGui.p.setProperty("SAVESTATECOUNT_hideRect", "false");
			SettingsGui.p.setProperty("PREDICTEDXYZ_hideRect", "false");
			SettingsGui.p.setProperty("MOUSEPOS_hideRect", "false");
			SettingsGui.p.setProperty("TRAJECTORIES_hideRect", "false");

			try {
				SettingsGui.save();
			} catch (IOException e420) {
				e420.printStackTrace();
			}
		}

		playbackTutorial = new TutorialHandler((short) 1);
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
		ClientRegistry.registerKeyBinding(infoGuiKey);

		VirtualKeybindings.registerBlockedKeyBinding(infoGuiKey);
		VirtualKeybindings.registerBlockedKeyBinding(tickratezeroKey);
		VirtualKeybindings.registerBlockedKeyBinding(tickAdvance);
		VirtualKeybindings.registerBlockedKeyBinding(stopkey);
		VirtualKeybindings.registerBlockedKeyBinding(testingKey);

		new File(tasdirectory).mkdir();
		new File(savestatedirectory).mkdir();

		super.init(ev);
	}

	public void postInit(FMLPostInitializationEvent ev) {
		super.postInit(ev);
	}

	public static TutorialHandler getPlaybackTutorial() {
		return playbackTutorial;
	}
}
