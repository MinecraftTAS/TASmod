package com.minecrafttas.tasmod;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.minecrafttas.tasmod.commands.clearinputs.ClearInputsPacket;
import com.minecrafttas.tasmod.commands.clearinputs.ClearInputsPacket.ClearInputsPacketHandler;
import com.minecrafttas.tasmod.commands.folder.FolderPacket;
import com.minecrafttas.tasmod.commands.folder.FolderPacket.FolderPacketHandler;
import com.minecrafttas.tasmod.commands.fullplay.FullPlayPacket;
import com.minecrafttas.tasmod.commands.fullplay.FullPlayPacket.FullPlayPacketHandler;
import com.minecrafttas.tasmod.commands.fullrecord.FullRecordPacket;
import com.minecrafttas.tasmod.commands.fullrecord.FullRecordPacket.FullRecordPacketHandler;
import com.minecrafttas.tasmod.commands.loadtas.LoadTASPacket;
import com.minecrafttas.tasmod.commands.loadtas.LoadTASPacket.LoadTASPacketHandler;
import com.minecrafttas.tasmod.commands.restartandplay.RestartAndPlayPacket;
import com.minecrafttas.tasmod.commands.restartandplay.RestartAndPlayPacket.RestartAndPlayPacketHandler;
import com.minecrafttas.tasmod.commands.savetas.SaveTASPacket;
import com.minecrafttas.tasmod.commands.savetas.SaveTASPacket.SaveTASPacketHandler;
import com.minecrafttas.tasmod.inputcontainer.InputContainer;
import com.minecrafttas.tasmod.inputcontainer.server.InitialSyncStatePacket;
import com.minecrafttas.tasmod.inputcontainer.server.SyncStatePacket;
import com.minecrafttas.tasmod.ktrng.KTRNGSeedPacket;
import com.minecrafttas.tasmod.ktrng.KTRNGStartSeedPacket;
import com.minecrafttas.tasmod.networking.IdentificationPacket;
import com.minecrafttas.tasmod.networking.PacketSerializer;
import com.minecrafttas.tasmod.savestates.client.InputSavestatesPacket;
import com.minecrafttas.tasmod.savestates.client.InputSavestatesPacket.InputSavestatesPacketHandler;
import com.minecrafttas.tasmod.savestates.server.LoadstatePacket;
import com.minecrafttas.tasmod.savestates.server.LoadstatePacket.LoadstatePacketHandler;
import com.minecrafttas.tasmod.savestates.server.SavestatePacket;
import com.minecrafttas.tasmod.savestates.server.motion.MotionPacket;
import com.minecrafttas.tasmod.savestates.server.motion.MotionPacket.MotionPacketHandler;
import com.minecrafttas.tasmod.savestates.server.motion.RequestMotionPacket;
import com.minecrafttas.tasmod.savestates.server.motion.RequestMotionPacket.RequestMotionPacketHandler;
import com.minecrafttas.tasmod.savestates.server.playerloading.SavestatePlayerLoadingPacket;
import com.minecrafttas.tasmod.savestates.server.playerloading.SavestatePlayerLoadingPacket.SavestatePlayerLoadingPacketHandler;
import com.minecrafttas.tasmod.tickratechanger.AdvanceTickratePacket;
import com.minecrafttas.tasmod.tickratechanger.AdvanceTickratePacket.AdvanceTickratePacketHandler;
import com.minecrafttas.tasmod.tickratechanger.ChangeTickratePacket;
import com.minecrafttas.tasmod.tickratechanger.ChangeTickratePacket.ChangeTickratePacketHandler;
import com.minecrafttas.tasmod.tickratechanger.PauseTickratePacket;
import com.minecrafttas.tasmod.tickratechanger.PauseTickratePacket.PauseTickratePacketHandler;
import com.minecrafttas.tasmod.tickratechanger.TickrateChangerServer;
import com.minecrafttas.tasmod.ticksync.TickSyncPacket;
import com.minecrafttas.tasmod.util.TickScheduler;

import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class CommonProxy {
	public static SimpleNetworkWrapper NETWORK;
	public static Logger logger = LogManager.getLogger("TASmod");
	
	public static TickScheduler tickSchedulerServer = new TickScheduler();
	
	public void preInit(FMLPreInitializationEvent ev) {
		
		TickrateChangerServer.logger = logger;
		
		NETWORK = NetworkRegistry.INSTANCE.newSimpleChannel("tasmod");
		int i = -1;

		// Tickrate
		NETWORK.registerMessage(ChangeTickratePacketHandler.class, ChangeTickratePacket.class, i++, Side.SERVER);
		NETWORK.registerMessage(ChangeTickratePacketHandler.class, ChangeTickratePacket.class, i++, Side.CLIENT);

		NETWORK.registerMessage(AdvanceTickratePacketHandler.class, AdvanceTickratePacket.class, i++, Side.SERVER);
		NETWORK.registerMessage(AdvanceTickratePacketHandler.class, AdvanceTickratePacket.class, i++, Side.CLIENT);

		NETWORK.registerMessage(PauseTickratePacketHandler.class, PauseTickratePacket.class, i++, Side.SERVER);
		NETWORK.registerMessage(PauseTickratePacketHandler.class, PauseTickratePacket.class, i++, Side.CLIENT);

		// Trigger savestates/loadstates on the client
		NETWORK.registerMessage(LoadstatePacketHandler.class, LoadstatePacket.class, i++, Side.SERVER);
		NETWORK.registerMessage(LoadstatePacketHandler.class, LoadstatePacket.class, i++, Side.CLIENT);

		// Sync player motion between client and server
		NETWORK.registerMessage(RequestMotionPacketHandler.class, RequestMotionPacket.class, i++, Side.CLIENT);
		NETWORK.registerMessage(MotionPacketHandler.class, MotionPacket.class, i++, Side.SERVER);

		// Create or load a savestate of the recording or playback on the client
		NETWORK.registerMessage(InputSavestatesPacketHandler.class, InputSavestatesPacket.class, i++, Side.CLIENT);

		// When loadstating, send the data of the client from server to client
		NETWORK.registerMessage(SavestatePlayerLoadingPacketHandler.class, SavestatePlayerLoadingPacket.class, i++, Side.CLIENT);

		// Trigger saving the inputs to file on the client
		NETWORK.registerMessage(SaveTASPacketHandler.class, SaveTASPacket.class, i++, Side.CLIENT);
		NETWORK.registerMessage(SaveTASPacketHandler.class, SaveTASPacket.class, i++, Side.SERVER);
		NETWORK.registerMessage(LoadTASPacketHandler.class, LoadTASPacket.class, i++, Side.CLIENT);
		NETWORK.registerMessage(LoadTASPacketHandler.class, LoadTASPacket.class, i++, Side.SERVER);
		NETWORK.registerMessage(ClearInputsPacketHandler.class, ClearInputsPacket.class, i++, Side.CLIENT);
		NETWORK.registerMessage(ClearInputsPacketHandler.class, ClearInputsPacket.class, i++, Side.SERVER);

		// Misc
		NETWORK.registerMessage(FolderPacketHandler.class, FolderPacket.class, i++, Side.CLIENT);
		NETWORK.registerMessage(InputContainer.TeleportPlayerPacketHandler.class, InputContainer.TeleportPlayerPacket.class, i++, Side.SERVER);

		// Fullrecord
		NETWORK.registerMessage(FullRecordPacketHandler.class, FullRecordPacket.class, i++, Side.CLIENT);
		// Fullplay
		NETWORK.registerMessage(FullPlayPacketHandler.class, FullPlayPacket.class, i++, Side.CLIENT);
		// RestartAndPlay
		NETWORK.registerMessage(RestartAndPlayPacketHandler.class, RestartAndPlayPacket.class, i++, Side.CLIENT);
		
		PacketSerializer.registerPacket(IdentificationPacket.class);
		
		PacketSerializer.registerPacket(TickSyncPacket.class);
		
		PacketSerializer.registerPacket(KTRNGSeedPacket.class);
		PacketSerializer.registerPacket(KTRNGStartSeedPacket.class);
		
		PacketSerializer.registerPacket(SyncStatePacket.class);
		PacketSerializer.registerPacket(InitialSyncStatePacket.class);
		
		PacketSerializer.registerPacket(SavestatePacket.class);
	}

	public void init(FMLInitializationEvent ev) {
	}

	public void postInit(FMLPostInitializationEvent ev) {
	}

}
