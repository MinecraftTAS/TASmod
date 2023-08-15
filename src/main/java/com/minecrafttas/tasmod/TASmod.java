package com.minecrafttas.tasmod;

import java.io.File;
import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.minecrafttas.common.CommandRegistry;
import com.minecrafttas.common.events.EventListenerRegistry;
import com.minecrafttas.common.events.EventServer.EventServerInit;
import com.minecrafttas.common.events.EventServer.EventServerStop;
import com.minecrafttas.common.server.PacketHandlerRegistry;
import com.minecrafttas.common.server.Server;
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
import com.minecrafttas.tasmod.ktrng.KillTheRNGHandler;
import com.minecrafttas.tasmod.networking.TASmodPackets;
import com.minecrafttas.tasmod.playback.server.TASstateServer;
import com.minecrafttas.tasmod.savestates.server.SavestateCommand;
import com.minecrafttas.tasmod.savestates.server.SavestateHandler;
import com.minecrafttas.tasmod.savestates.server.files.SavestateTrackerFile;
import com.minecrafttas.tasmod.tickratechanger.CommandTickrate;
import com.minecrafttas.tasmod.tickratechanger.TickrateChangerServer;
import com.minecrafttas.tasmod.ticksync.TickSyncServer;
import com.minecrafttas.tasmod.util.LoggerMarkers;
import com.minecrafttas.tasmod.util.TickScheduler;

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
	
	public static TASstateServer containerStateServer;
	
	public static SavestateHandler savestateHandler;
	
	public static KillTheRNGHandler ktrngHandler;
	
	public static TickrateChangerServer tickratechanger;
	
	public static TickSyncServer ticksyncServer;
	
	public static final TickScheduler tickSchedulerServer = new TickScheduler();
	
	public static Server server;
	
	@Override
	public void onServerInit(MinecraftServer server) {
		serverInstance = server;
		containerStateServer=new TASstateServer();
		// Command handling
		
		CommandRegistry.registerServerCommand(new CommandTickrate(), server);
		CommandRegistry.registerServerCommand(new CommandRecord(), server);
		CommandRegistry.registerServerCommand(new CommandPlay(), server);
		CommandRegistry.registerServerCommand(new CommandSaveTAS(), server);
		CommandRegistry.registerServerCommand(new CommandLoadTAS(), server);
		CommandRegistry.registerServerCommand(new CommandFolder(), server);
		CommandRegistry.registerServerCommand(new CommandClearInputs(), server);
		CommandRegistry.registerServerCommand(new SavestateCommand(), server);
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
		
		savestateHandler=new SavestateHandler(server, LOGGER);
		
		if(!server.isDedicatedServer()) {
			TASmod.tickratechanger.ticksPerSecond=0F;
			TASmod.tickratechanger.tickrateSaved=20F;
		}
	}
	
	@Override
	public void onServerStop(MinecraftServer mcserver) {
		serverInstance=null;
		try {
			if (server != null) server.close();
		} catch (IOException e) {
			LOGGER.error("Unable to close TASmod server: {}", e);
			e.printStackTrace();
		}
	}
	
	public static MinecraftServer getServerInstance() {
		return serverInstance;
	}

	@Override
	public void onInitialize() {
		
		// Events
		LOGGER.info("Initializing TASmod");
		EventListenerRegistry.register(this);
		
		ticksyncServer = new TickSyncServer();
		EventListenerRegistry.register(ticksyncServer);
		PacketHandlerRegistry.register(ticksyncServer);
		
		PacketHandlerRegistry.register(TASmodClient.virtual.getContainer());
		
		LOGGER.info("Testing connection with KillTheRNG");
		ktrngHandler=new KillTheRNGHandler(FabricLoaderImpl.INSTANCE.isModLoaded("killtherng"));
		EventListenerRegistry.register(ktrngHandler);
		
		tickratechanger = new TickrateChangerServer(LOGGER);
		EventListenerRegistry.register(tickratechanger);
		
		// Networking
		LOGGER.info(LoggerMarkers.Networking, "Registering network handlers");
		
//		// Savestates
//		PacketSerializer.registerPacket(SavestatePacket.class);
//		PacketSerializer.registerPacket(LoadstatePacket.class);
//		
//		PacketSerializer.registerPacket(InputSavestatesPacket.class);
//		PacketSerializer.registerPacket(SavestatePlayerLoadingPacket.class);
//
//		// KillTheRNG
//		PacketSerializer.registerPacket(KTRNGSeedPacket.class);
//		PacketSerializer.registerPacket(KTRNGStartSeedPacket.class);
//		
//		// Recording/Playback
//		PacketSerializer.registerPacket(SyncStatePacket.class);
//		PacketSerializer.registerPacket(InitialSyncStatePacket.class);
//		
//		PacketSerializer.registerPacket(ClearInputsPacket.class);
//		
//		PacketSerializer.registerPacket(FullRecordPacket.class);
//		PacketSerializer.registerPacket(FullPlayPacket.class);
//		
//		PacketSerializer.registerPacket(RestartAndPlayPacket.class);
//		
//		// Storing
//		PacketSerializer.registerPacket(SaveTASPacket.class);
//		PacketSerializer.registerPacket(LoadTASPacket.class);
//
//		// Misc
//		PacketSerializer.registerPacket(PlaybackController.TeleportPlayerPacket.class);
//		PacketSerializer.registerPacket(FolderPacket.class);
//		
//		PacketSerializer.registerPacket(PlayUntilPacket.class);
		
		try {
			server = new Server(5555, TASmodPackets.values());
		} catch (Exception e) {
			LOGGER.error("Unable to launch TASmod server: {}", e);
		}
		
	}
}
