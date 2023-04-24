package com.minecrafttas.tasmod;

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
import com.minecrafttas.tasmod.inputcontainer.InputContainer;
import com.minecrafttas.tasmod.inputcontainer.server.InitialSyncStatePacket;
import com.minecrafttas.tasmod.inputcontainer.server.SyncStatePacket;
import com.minecrafttas.tasmod.ktrng.KTRNGSeedPacket;
import com.minecrafttas.tasmod.ktrng.KTRNGStartSeedPacket;
import com.minecrafttas.tasmod.networking.IdentificationPacket;
import com.minecrafttas.tasmod.networking.PacketSerializer;
import com.minecrafttas.tasmod.savestates.client.InputSavestatesPacket;
import com.minecrafttas.tasmod.savestates.server.LoadstatePacket;
import com.minecrafttas.tasmod.savestates.server.SavestatePacket;
import com.minecrafttas.tasmod.savestates.server.motion.MotionPacket;
import com.minecrafttas.tasmod.savestates.server.motion.RequestMotionPacket;
import com.minecrafttas.tasmod.savestates.server.playerloading.SavestatePlayerLoadingPacket;
import com.minecrafttas.tasmod.tickratechanger.AdvanceTickratePacket;
import com.minecrafttas.tasmod.tickratechanger.ChangeTickratePacket;
import com.minecrafttas.tasmod.tickratechanger.PauseTickratePacket;
import com.minecrafttas.tasmod.tickratechanger.TickrateChangerServer;
import com.minecrafttas.tasmod.ticksync.TickSyncPacket;
import com.minecrafttas.tasmod.util.TickScheduler;

import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;

public class CommonProxy {
	public static SimpleNetworkWrapper NETWORK;
	public static Logger logger = LogManager.getLogger("TASmod");
	
	public static TickScheduler tickSchedulerServer = new TickScheduler();
	
	public void preInit(FMLPreInitializationEvent ev) {
		
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
		PacketSerializer.registerPacket(InputContainer.TeleportPlayerPacket.class);
		PacketSerializer.registerPacket(FolderPacket.class);
		
		PacketSerializer.registerPacket(PlayUntilPacket.class);
		
	}

	public void init(FMLInitializationEvent ev) {
	}

	public void postInit(FMLPostInitializationEvent ev) {
	}

}
