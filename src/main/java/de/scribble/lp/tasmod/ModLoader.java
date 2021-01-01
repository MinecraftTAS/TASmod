package de.scribble.lp.tasmod;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.scribble.lp.tasmod.playback.CommandPlay;
import de.scribble.lp.tasmod.recording.CommandRecord;
import de.scribble.lp.tasmod.tickratechanger.CommandTickrate;
import de.scribble.lp.tasmod.tutorial.CommandPlaybacktutorial;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;

/**
 * 
 * @author ScribbleLP
 *
 */
@Mod(modid = "tasmod", name = "Tool-Assisted Speedrun Mod", version =ModLoader.VERSION)
public class ModLoader {
	@Instance
	public static ModLoader instance = new ModLoader();
	
	@SidedProxy(serverSide = "de.scribble.lp.tasmod.CommonProxy", clientSide = "de.scribble.lp.tasmod.ClientProxy")
	public static CommonProxy proxy;
	
	public static final String VERSION="${version}";
	public static final String MCVERSION="${mcversion}";
	
	private MinecraftServer serverInstance;
	
	public static final Logger logger= LogManager.getFormatterLogger("TASMod");
	
	
	@EventHandler
	public void preInit(FMLPreInitializationEvent ev) {
		proxy.preInit(ev);
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
		serverInstance= ev.getServer();
		
		//Command handling
		ev.registerServerCommand(new CommandTickrate());
		ev.registerServerCommand(new CommandRecord());
		ev.registerServerCommand(new CommandPlay());
		ev.registerServerCommand(new CommandPlaybacktutorial());
	}
	public static ModLoader getInstance() {
		return instance;
	}
	public MinecraftServer getServerInstance() {
		return serverInstance;
	}
}
