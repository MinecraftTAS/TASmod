package com.minecrafttas.tasmod;

import java.io.File;
import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.minecrafttas.common.CommandRegistry;
import com.minecrafttas.common.events.EventListener;
import com.minecrafttas.common.events.EventServer.EventServerInit;
import com.minecrafttas.common.events.EventServer.EventServerStop;
import com.minecrafttas.tasmod.commands.clearinputs.ClearInputsPacket;
import com.minecrafttas.tasmod.commands.clearinputs.CommandClearInputs;
import com.minecrafttas.tasmod.commands.folder.CommandFolder;
import com.minecrafttas.tasmod.commands.folder.FolderPacket;
import com.minecrafttas.tasmod.commands.fullplay.CommandFullPlay;
import com.minecrafttas.tasmod.commands.fullplay.FullPlayPacket;
import com.minecrafttas.tasmod.commands.fullrecord.CommandFullRecord;
import com.minecrafttas.tasmod.commands.fullrecord.FullRecordPacket;
import com.minecrafttas.tasmod.commands.loadtas.CommandLoadTAS;
import com.minecrafttas.tasmod.commands.loadtas.LoadTASPacket;
import com.minecrafttas.tasmod.commands.playback.CommandPlay;
import com.minecrafttas.tasmod.commands.playuntil.CommandPlayUntil;
import com.minecrafttas.tasmod.commands.playuntil.PlayUntilPacket;
import com.minecrafttas.tasmod.commands.recording.CommandRecord;
import com.minecrafttas.tasmod.commands.restartandplay.CommandRestartAndPlay;
import com.minecrafttas.tasmod.commands.restartandplay.RestartAndPlayPacket;
import com.minecrafttas.tasmod.commands.savetas.CommandSaveTAS;
import com.minecrafttas.tasmod.commands.savetas.SaveTASPacket;
import com.minecrafttas.tasmod.ktrng.KTRNGSeedPacket;
import com.minecrafttas.tasmod.ktrng.KTRNGStartSeedPacket;
import com.minecrafttas.tasmod.ktrng.KillTheRNGHandler;
import com.minecrafttas.tasmod.networking.IdentificationPacket;
import com.minecrafttas.tasmod.networking.PacketSerializer;
import com.minecrafttas.tasmod.networking.TASmodNetworkServer;
import com.minecrafttas.tasmod.playback.PlaybackController;
import com.minecrafttas.tasmod.playback.server.InitialSyncStatePacket;
import com.minecrafttas.tasmod.playback.server.SyncStatePacket;
import com.minecrafttas.tasmod.playback.server.TASstateServer;
import com.minecrafttas.tasmod.savestates.client.InputSavestatesPacket;
import com.minecrafttas.tasmod.savestates.server.LoadstatePacket;
import com.minecrafttas.tasmod.savestates.server.SavestateCommand;
import com.minecrafttas.tasmod.savestates.server.SavestateHandler;
import com.minecrafttas.tasmod.savestates.server.SavestatePacket;
import com.minecrafttas.tasmod.savestates.server.files.SavestateTrackerFile;
import com.minecrafttas.tasmod.savestates.server.motion.MotionPacket;
import com.minecrafttas.tasmod.savestates.server.motion.RequestMotionPacket;
import com.minecrafttas.tasmod.savestates.server.playerloading.SavestatePlayerLoadingPacket;
import com.minecrafttas.tasmod.tickratechanger.AdvanceTickratePacket;
import com.minecrafttas.tasmod.tickratechanger.ChangeTickratePacket;
import com.minecrafttas.tasmod.tickratechanger.CommandTickrate;
import com.minecrafttas.tasmod.tickratechanger.PauseTickratePacket;
import com.minecrafttas.tasmod.tickratechanger.TickrateChangerServer;
import com.minecrafttas.tasmod.ticksync.TickSyncPacket;
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
	
	public static TASmodNetworkServer packetServer;
	
	public static TickrateChangerServer tickratechanger;
	
	public static final TickScheduler tickSchedulerServer = new TickScheduler();

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
		
		try {
			packetServer = new TASmodNetworkServer(LOGGER);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		if(!server.isDedicatedServer()) {
			TASmod.tickratechanger.ticksPerSecond=0F;
			TASmod.tickratechanger.tickrateSaved=20F;
		}
	}
	
	@Override
	public void onServerStop(MinecraftServer server) {
		serverInstance=null;
		packetServer.close();
	}
	
	public static MinecraftServer getServerInstance() {
		return serverInstance;
	}

	@Override
	public void onInitialize() {
		LOGGER.info("Initializing TASmod");
		EventListener.register(this);
		
		LOGGER.info("Testing connection with KillTheRNG");
		ktrngHandler=new KillTheRNGHandler(FabricLoaderImpl.INSTANCE.isModLoaded("killtherng"));
		EventListener.register(ktrngHandler);
		
		tickratechanger = new TickrateChangerServer(LOGGER);
		EventListener.register(tickratechanger);
		
		
		PacketSerializer.registerPacket(IdentificationPacket.class);
		// Ticksync
		PacketSerializer.registerPacket(TickSyncPacket.class);

		
		//Tickratechanger
		PacketSerializer.registerPacket(ChangeTickratePacket.class);
		PacketSerializer.registerPacket(PauseTickratePacket.class);
		PacketSerializer.registerPacket(AdvanceTickratePacket.class);
		
		
		// Savestates
		PacketSerializer.registerPacket(SavestatePacket.class);
		PacketSerializer.registerPacket(LoadstatePacket.class);
		
		PacketSerializer.registerPacket(InputSavestatesPacket.class);
		PacketSerializer.registerPacket(SavestatePlayerLoadingPacket.class);
		
		PacketSerializer.registerPacket(RequestMotionPacket.class);
		PacketSerializer.registerPacket(MotionPacket.class);
		
		// KillTheRNG
		PacketSerializer.registerPacket(KTRNGSeedPacket.class);
		PacketSerializer.registerPacket(KTRNGStartSeedPacket.class);
		
		// Recording/Playback
		PacketSerializer.registerPacket(SyncStatePacket.class);
		PacketSerializer.registerPacket(InitialSyncStatePacket.class);
		
		PacketSerializer.registerPacket(ClearInputsPacket.class);
		
		PacketSerializer.registerPacket(FullRecordPacket.class);
		PacketSerializer.registerPacket(FullPlayPacket.class);
		
		PacketSerializer.registerPacket(RestartAndPlayPacket.class);
		
		// Storing
		PacketSerializer.registerPacket(SaveTASPacket.class);
		PacketSerializer.registerPacket(LoadTASPacket.class);

		// Misc
		PacketSerializer.registerPacket(PlaybackController.TeleportPlayerPacket.class);
		PacketSerializer.registerPacket(FolderPacket.class);
		
		PacketSerializer.registerPacket(PlayUntilPacket.class);
		
	}
}
