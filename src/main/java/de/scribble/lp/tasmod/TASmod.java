package de.scribble.lp.tasmod;

import java.io.File;
import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.scribble.lp.killtherng.KillTheRNG;
import de.scribble.lp.tasmod.commands.changestates.ContainerStateServer;
import de.scribble.lp.tasmod.commands.clearinputs.CommandClearInputs;
import de.scribble.lp.tasmod.commands.folder.CommandFolder;
import de.scribble.lp.tasmod.commands.loadtas.CommandLoadTAS;
import de.scribble.lp.tasmod.commands.playback.CommandPlay;
import de.scribble.lp.tasmod.commands.recording.CommandRecord;
import de.scribble.lp.tasmod.commands.savetas.CommandSaveTAS;
import de.scribble.lp.tasmod.commands.tutorial.CommandPlaybacktutorial;
import de.scribble.lp.tasmod.savestates.server.SavestateHandler;
import de.scribble.lp.tasmod.savestates.server.SavestateTrackerFile;
import de.scribble.lp.tasmod.tickratechanger.CommandTickrate;
import de.scribble.lp.tasmod.util.ModIncompatibleException;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;

/**
 * ModLoader for TASmod
 * 
 * @author ScribbleLP
 *
 */
@Mod(modid = "tasmod", name = "Tool-Assisted Speedrun Mod", version = TASmod.VERSION)
public class TASmod {
	@Instance
	public static TASmod instance = new TASmod();

	@SidedProxy(serverSide = "de.scribble.lp.tasmod.CommonProxy", clientSide = "de.scribble.lp.tasmod.ClientProxy")
	public static CommonProxy proxy;

	public static final String VERSION = "${version}";
	public static final String MCVERSION = "${mcversion}";

	private static MinecraftServer serverInstance;
	
	public static boolean isKTRNGLoaded;

	public static final Logger logger = LogManager.getLogger("TASMod");
	
	public static ContainerStateServer containerStateServer;
	
	public static SavestateHandler savestateHandler;

	@EventHandler
	public void preInit(FMLPreInitializationEvent ev) throws Exception {
		proxy.preInit(ev);
		if (Loader.isModLoaded("tastools")) {
			throw new ModIncompatibleException("\n========================================================================\n" + "\n" + "Detected TASTools to be loaded. TASMod and TASTools are incompatible!\n" + "\n" + "========================================================================");
		} else if (Loader.isModLoaded("dupemod")) {
			throw new ModIncompatibleException("\n========================================================================\n" + "\n" + "Detected Dupemod to be loaded. TASMod and Dupemod are incompatible!\n" + "\n" + "========================================================================");
		} else if (Loader.isModLoaded("tickratechanger")) {
			throw new ModIncompatibleException("\n========================================================================\n" + "\n" + "Detected Tickratechanger to be loaded. TASMod and Tickratechanger are incompatible!\n" + "\n" + "========================================================================");
		} else if (Loader.isModLoaded("lotas")) {
			throw new ModIncompatibleException("\n========================================================================\n" + "\n" + "Detected LoTAS to be loaded. TASMod and LoTAS are incompatible!\n" + "\n" + "========================================================================");
		}
	}

	@EventHandler
	public void init(FMLInitializationEvent ev) {
		logger.info("Initializing TASmod");
		logger.info("Testing connection with KillTheRNG");
		isKTRNGLoaded=Loader.isModLoaded("killtherng");
		if (isKTRNGLoaded) {
			KillTheRNG.LOGGER.info("Connection established with TASmod");
		}else {
			logger.info("KillTheRNG doesn't appear to be loaded");
		}
		proxy.init(ev);
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent ev) {
		proxy.postInit(ev);
	}

	@EventHandler
	public void serverStart(FMLServerStartingEvent ev) {
		serverInstance = ev.getServer();
		containerStateServer=new ContainerStateServer();
		// Command handling
		ev.registerServerCommand(new CommandTickrate());
		ev.registerServerCommand(new CommandRecord());
		ev.registerServerCommand(new CommandPlay());
		ev.registerServerCommand(new CommandSaveTAS());
		ev.registerServerCommand(new CommandLoadTAS());
		ev.registerServerCommand(new CommandPlaybacktutorial());
		ev.registerServerCommand(new CommandFolder());
		ev.registerServerCommand(new CommandClearInputs());

		// Save Loadstate Count
		File savestateDirectory = new File(serverInstance.getDataDirectory() + File.separator + "saves" + File.separator + "savestates" + File.separator);
		try {
			new SavestateTrackerFile(new File(savestateDirectory, ev.getServer().getFolderName() + "-info.txt"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		savestateHandler=new SavestateHandler(ev.getServer());
	}

	public static TASmod getInstance() {
		return instance;
	}

	public static MinecraftServer getServerInstance() {
		return serverInstance;
	}
}
