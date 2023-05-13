package com.minecrafttas.tasmod;

import java.io.File;
import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.minecrafttas.tasmod.commands.clearinputs.CommandClearInputs;
import com.minecrafttas.tasmod.commands.folder.CommandFolder;
import com.minecrafttas.tasmod.commands.fullplay.CommandFullPlay;
import com.minecrafttas.tasmod.commands.fullrecord.CommandFullRecord;
import com.minecrafttas.tasmod.commands.loadtas.CommandLoadTAS;
import com.minecrafttas.tasmod.commands.playback.CommandPlay;
import com.minecrafttas.tasmod.commands.playuntil.CommandPlayUntil;
import com.minecrafttas.tasmod.commands.recording.CommandRecord;
import com.minecrafttas.tasmod.commands.restartandplay.CommandRestartAndPlay;
import com.minecrafttas.tasmod.commands.savetas.CommandSaveTAS;
import com.minecrafttas.tasmod.commands.tutorial.CommandPlaybacktutorial;
import com.minecrafttas.tasmod.ktrng.KillTheRNGHandler;
import com.minecrafttas.tasmod.networking.TASmodNetworkServer;
import com.minecrafttas.tasmod.playback.server.TASstateServer;
import com.minecrafttas.tasmod.savestates.server.SavestateCommand;
import com.minecrafttas.tasmod.savestates.server.SavestateHandler;
import com.minecrafttas.tasmod.savestates.server.files.SavestateTrackerFile;
import com.minecrafttas.tasmod.tickratechanger.CommandTickrate;
import com.minecrafttas.tasmod.tickratechanger.TickrateChangerServer;
import com.minecrafttas.tasmod.util.ModIncompatibleException;

import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent;

/**
 * ModLoader for TASmod
 * 
 * @author Scribble
 *
 */
@Mod(modid = "tasmod", name = "Tool-Assisted Speedrun Mod", version = TASmod.VERSION)
public class TASmod {

	@SidedProxy(serverSide = "com.minecrafttas.tasmod.CommonProxy", clientSide = "com.minecrafttas.tasmod.ClientProxy")
	public static CommonProxy proxy;

	public static final String VERSION = "${mod_version}";
	public static final String MCVERSION = "${mcversion}";

	private static MinecraftServer serverInstance;
	
	public static final Logger logger = LogManager.getLogger("TASMod");
	
	public static TASstateServer containerStateServer;
	
	public static SavestateHandler savestateHandler;
	
	public static KillTheRNGHandler ktrngHandler;
	
	public static TASmodNetworkServer packetServer;

	@EventHandler
	public void preInit(FMLPreInitializationEvent ev) throws Exception {
		logger.info("Initializing TASmod");
		logger.info("Testing connection with KillTheRNG");
		ktrngHandler=new KillTheRNGHandler(Loader.isModLoaded("killtherng"));
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
		for (ModContainer mod : Loader.instance().getModList())
        {
            if(mod.getModId().equals("forge")) {
            	String [] versionsplit=mod.getVersion().split("\\.");
            	int forgeversion=Integer.parseInt(versionsplit[3]);
            	if(MCVERSION.equals("1.12.2")&&forgeversion<2857) {
            		throw new SecurityException("\n========================================================================\n" + "\n" + "The forge version you are using contains a critical Log4J exploit: 14.23.5." +forgeversion+ "\n"
            				+ "Please update the forge version to something above 14.23.5.2857.\n"
            				+ "You can find new versions under https://files.minecraftforge.net/net/minecraftforge/forge/index_1.12.2.html\n\n ========================================================================");
            	}
            }
        }
	}

	@EventHandler
	public void init(FMLInitializationEvent ev) {
		proxy.init(ev);
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent ev) {
		proxy.postInit(ev);
	}

	@EventHandler
	public void serverStart(FMLServerStartingEvent ev) {
		serverInstance = ev.getServer();
		containerStateServer=new TASstateServer();
		// Command handling
		ev.registerServerCommand(new CommandTickrate());
		ev.registerServerCommand(new CommandRecord());
		ev.registerServerCommand(new CommandPlay());
		ev.registerServerCommand(new CommandSaveTAS());
		ev.registerServerCommand(new CommandLoadTAS());
		ev.registerServerCommand(new CommandPlaybacktutorial());
		ev.registerServerCommand(new CommandFolder());
		ev.registerServerCommand(new CommandClearInputs());
		ev.registerServerCommand(new SavestateCommand());
		ev.registerServerCommand(new CommandFullRecord());
		ev.registerServerCommand(new CommandFullPlay());
		ev.registerServerCommand(new CommandRestartAndPlay());
		ev.registerServerCommand(new CommandPlayUntil());

		// Save Loadstate Count
		File savestateDirectory = new File(serverInstance.getDataDirectory() + File.separator + "saves" + File.separator + "savestates" + File.separator);
		try {
			new SavestateTrackerFile(new File(savestateDirectory, ev.getServer().getFolderName() + "-info.txt"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		savestateHandler=new SavestateHandler(ev.getServer(), logger);
		
		try {
			packetServer = new TASmodNetworkServer(logger);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		if(!serverInstance.isDedicatedServer()) {
			TickrateChangerServer.ticksPerSecond=0F;
			TickrateChangerServer.tickrateSaved=20F;
		}
	}
	
	@EventHandler
	public void serverStop(FMLServerStoppingEvent ev) {
		serverInstance=null;
		packetServer.close();
	}

	public static MinecraftServer getServerInstance() {
		return serverInstance;
	}
}
