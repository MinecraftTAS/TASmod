package com.minecrafttas.tasmod;

import java.io.File;
import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.minecrafttas.tasmod.commands.clearinputs.ClearInputsPacket;
import com.minecrafttas.tasmod.commands.folder.FolderPacket;
import com.minecrafttas.tasmod.commands.fullplay.FullPlayPacket;
import com.minecrafttas.tasmod.commands.fullrecord.FullRecordPacket;
import com.minecrafttas.tasmod.commands.loadtas.LoadTASPacket;
import com.minecrafttas.tasmod.commands.playuntil.PlayUntilPacket;
import com.minecrafttas.tasmod.commands.restartandplay.RestartAndPlayPacket;
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
import com.minecrafttas.tasmod.savestates.server.SavestateHandler;
import com.minecrafttas.tasmod.savestates.server.SavestatePacket;
import com.minecrafttas.tasmod.savestates.server.files.SavestateTrackerFile;
import com.minecrafttas.tasmod.savestates.server.motion.MotionPacket;
import com.minecrafttas.tasmod.savestates.server.motion.RequestMotionPacket;
import com.minecrafttas.tasmod.savestates.server.playerloading.SavestatePlayerLoadingPacket;
import com.minecrafttas.tasmod.tickratechanger.AdvanceTickratePacket;
import com.minecrafttas.tasmod.tickratechanger.ChangeTickratePacket;
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
public class TASmod implements ModInitializer{

	private static MinecraftServer serverInstance;
	
	public static final Logger logger = LogManager.getLogger("TASMod");
	
	public static TASstateServer containerStateServer;
	
	public static SavestateHandler savestateHandler;
	
	public static KillTheRNGHandler ktrngHandler;
	
	public static TASmodNetworkServer packetServer;
	
	public static final TickScheduler tickSchedulerServer = new TickScheduler();


	public void serverStart(MinecraftServer server) {
		serverInstance = server;
		containerStateServer=new TASstateServer();
		// Command handling
		
		//TODO register commands
//		ev.registerServerCommand(new CommandTickrate());
//		ev.registerServerCommand(new CommandRecord());
//		ev.registerServerCommand(new CommandPlay());
//		ev.registerServerCommand(new CommandSaveTAS());
//		ev.registerServerCommand(new CommandLoadTAS());
//		ev.registerServerCommand(new CommandPlaybacktutorial());
//		ev.registerServerCommand(new CommandFolder());
//		ev.registerServerCommand(new CommandClearInputs());
//		ev.registerServerCommand(new SavestateCommand());
//		ev.registerServerCommand(new CommandFullRecord());
//		ev.registerServerCommand(new CommandFullPlay());
//		ev.registerServerCommand(new CommandRestartAndPlay());
//		ev.registerServerCommand(new CommandPlayUntil());

		// Save Loadstate Count
		File savestateDirectory = new File(serverInstance.getDataDirectory() + File.separator + "saves" + File.separator + "savestates" + File.separator);
		try {
			new SavestateTrackerFile(new File(savestateDirectory, server.getFolderName() + "-info.txt"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		savestateHandler=new SavestateHandler(server, logger);
		
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
	
	public void serverStop(MinecraftServer server) {
		serverInstance=null;
		packetServer.close();
	}

	public static MinecraftServer getServerInstance() {
		return serverInstance;
	}

	@Override
	public void onInitialize() {
		logger.info("Initializing TASmod");
		logger.info("Testing connection with KillTheRNG");
		ktrngHandler=new KillTheRNGHandler(FabricLoaderImpl.INSTANCE.isModLoaded("killtherng"));
		
		TickrateChangerServer.logger = logger;
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
