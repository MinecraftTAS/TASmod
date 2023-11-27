package com.minecrafttas.tasmod;

import java.io.File;
import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.minecrafttas.mctcommon.CommandRegistry;
import com.minecrafttas.mctcommon.events.EventListenerRegistry;
import com.minecrafttas.mctcommon.events.EventServer.EventServerInit;
import com.minecrafttas.mctcommon.events.EventServer.EventServerStop;
import com.minecrafttas.mctcommon.server.PacketHandlerRegistry;
import com.minecrafttas.mctcommon.server.Server;
import com.minecrafttas.tasmod.commands.CommandClearInputs;
import com.minecrafttas.tasmod.commands.CommandFolder;
import com.minecrafttas.tasmod.commands.CommandFullPlay;
import com.minecrafttas.tasmod.commands.CommandFullRecord;
import com.minecrafttas.tasmod.commands.CommandLoadTAS;
import com.minecrafttas.tasmod.commands.CommandPlay;
import com.minecrafttas.tasmod.commands.CommandPlayUntil;
import com.minecrafttas.tasmod.commands.CommandRecord;
import com.minecrafttas.tasmod.commands.CommandRestartAndPlay;
import com.minecrafttas.tasmod.commands.CommandSaveTAS;
import com.minecrafttas.tasmod.commands.CommandSavestate;
import com.minecrafttas.tasmod.commands.CommandTickrate;
import com.minecrafttas.tasmod.ktrng.KillTheRNGHandler;
import com.minecrafttas.tasmod.networking.TASmodPackets;
import com.minecrafttas.tasmod.playback.PlaybackControllerServer;
import com.minecrafttas.tasmod.savestates.SavestateHandlerServer;
import com.minecrafttas.tasmod.savestates.files.SavestateTrackerFile;
import com.minecrafttas.tasmod.tickratechanger.TickrateChangerServer;
import com.minecrafttas.tasmod.ticksync.TickSyncServer;
import com.minecrafttas.tasmod.util.LoggerMarkers;
import com.minecrafttas.tasmod.util.Scheduler;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.impl.FabricLoaderImpl;
import net.minecraft.server.MinecraftServer;

/**
 * ModContainer for TASmod
 * 
 * @author Scribble
 *
 */
public class TASmod implements ModInitializer, EventServerInit, EventServerStop{

	private static MinecraftServer serverInstance;
	
	public static final Logger LOGGER = LogManager.getLogger("TASmod");
	
	public static PlaybackControllerServer playbackControllerServer=new PlaybackControllerServer();;
	
	public static SavestateHandlerServer savestateHandlerServer;
	
	public static KillTheRNGHandler ktrngHandler;
	
	public static TickrateChangerServer tickratechanger;
	
	public static TickSyncServer ticksyncServer;
	
	public static final Scheduler tickSchedulerServer = new Scheduler();
	
	public static Server server;

	public static final int networkingport = 8999;

	public static final boolean isDevEnvironment = FabricLoaderImpl.INSTANCE.isDevelopmentEnvironment();
	
	@Override
	public void onInitialize() {
		
		LOGGER.info("Initializing TASmod");
		
		
		// Start ticksync
		ticksyncServer = new TickSyncServer();
		
		// Initilize KillTheRNG
		LOGGER.info("Testing connection with KillTheRNG");
		ktrngHandler=new KillTheRNGHandler(FabricLoaderImpl.INSTANCE.isModLoaded("killtherng"));
		
		// Initialize TickrateChanger
		tickratechanger = new TickrateChangerServer(LOGGER);
		
		// Register event listeners
		EventListenerRegistry.register(this);
		EventListenerRegistry.register(ticksyncServer);
		EventListenerRegistry.register(tickratechanger);
		EventListenerRegistry.register(ktrngHandler);
		
		// Register packet handlers
		LOGGER.info(LoggerMarkers.Networking, "Registering network handlers");
		PacketHandlerRegistry.register(ticksyncServer);
		PacketHandlerRegistry.register(tickratechanger);
		PacketHandlerRegistry.register(ktrngHandler);
		PacketHandlerRegistry.register(playbackControllerServer);
	}
	
	@Override
	public void onServerInit(MinecraftServer server) {
		LOGGER.info("Initializing server");
		serverInstance = server;
		
		// Command handling
		
		CommandRegistry.registerServerCommand(new CommandTickrate(), server);
		CommandRegistry.registerServerCommand(new CommandRecord(), server);
		CommandRegistry.registerServerCommand(new CommandPlay(), server);
		CommandRegistry.registerServerCommand(new CommandSaveTAS(), server);
		CommandRegistry.registerServerCommand(new CommandLoadTAS(), server);
		CommandRegistry.registerServerCommand(new CommandFolder(), server);
		CommandRegistry.registerServerCommand(new CommandClearInputs(), server);
		CommandRegistry.registerServerCommand(new CommandSavestate(), server);
		CommandRegistry.registerServerCommand(new CommandFullRecord(), server);
		CommandRegistry.registerServerCommand(new CommandFullPlay(), server);
		CommandRegistry.registerServerCommand(new CommandRestartAndPlay(), server);
		CommandRegistry.registerServerCommand(new CommandPlayUntil(), server);

		// Save Loadstate Count
		File savestateDirectory = new File(server.getDataDirectory() + File.separator + "saves" + File.separator + "savestates" + File.separator);
		try {
			new SavestateTrackerFile(new File(savestateDirectory, server.getFolderName() + "-info.txt"));
		} catch (IOException e) {
			e.printStackTrace();
		}

		savestateHandlerServer = new SavestateHandlerServer(server, LOGGER);
		PacketHandlerRegistry.register(savestateHandlerServer);
		
		if(!server.isDedicatedServer()) {
			TASmod.tickratechanger.ticksPerSecond=0F;
			TASmod.tickratechanger.tickrateSaved=20F;
		} else {
			// Starting custom server instance
			try {
				TASmod.server = new Server(networkingport, TASmodPackets.values());
			} catch (Exception e) {
				LOGGER.error("Unable to launch TASmod server: {}", e.getMessage());
			}
		}
	}
	
	@Override
	public void onServerStop(MinecraftServer mcserver) {
		serverInstance=null;
		
		if(mcserver.isDedicatedServer()) {
			try {
				if (server != null) server.close();
			} catch (IOException e) {
				LOGGER.error("Unable to close TASmod server: {}", e);
				e.printStackTrace();
			}
		}
		
		if(savestateHandlerServer != null) {
			PacketHandlerRegistry.unregister(savestateHandlerServer);	// Unregistering the savestatehandler, as a new instance is registered in onServerStart()
			savestateHandlerServer = null;
		}
	}
	
	public static MinecraftServer getServerInstance() {
		return serverInstance;
	}

}
