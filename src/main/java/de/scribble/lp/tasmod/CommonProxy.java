package de.scribble.lp.tasmod;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.scribble.lp.tasmod.commands.clearinputs.ClearInputsPacket;
import de.scribble.lp.tasmod.commands.clearinputs.ClearInputsPacketHandler;
import de.scribble.lp.tasmod.commands.folder.FolderPacket;
import de.scribble.lp.tasmod.commands.folder.FolderPacketHandler;
import de.scribble.lp.tasmod.commands.fullplay.FullPlayPacket;
import de.scribble.lp.tasmod.commands.fullplay.FullPlayPacketHandler;
import de.scribble.lp.tasmod.commands.fullrecord.FullRecordPacket;
import de.scribble.lp.tasmod.commands.fullrecord.FullRecordPacketHandler;
import de.scribble.lp.tasmod.commands.loadtas.LoadTASPacket;
import de.scribble.lp.tasmod.commands.loadtas.LoadTASPacketHandler;
import de.scribble.lp.tasmod.commands.savetas.SaveTASPacket;
import de.scribble.lp.tasmod.commands.savetas.SaveTASPacketHandler;
import de.scribble.lp.tasmod.inputcontainer.InputContainer;
import de.scribble.lp.tasmod.savestates.client.InputSavestatesPacket;
import de.scribble.lp.tasmod.savestates.client.InputSavestatesPacketHandler;
import de.scribble.lp.tasmod.savestates.server.LoadstatePacket;
import de.scribble.lp.tasmod.savestates.server.LoadstatePacketHandler;
import de.scribble.lp.tasmod.savestates.server.SavestatePacket;
import de.scribble.lp.tasmod.savestates.server.SavestatePacketHandler;
import de.scribble.lp.tasmod.savestates.server.motion.MotionPacket;
import de.scribble.lp.tasmod.savestates.server.motion.MotionPacketHandler;
import de.scribble.lp.tasmod.savestates.server.motion.RequestMotionPacket;
import de.scribble.lp.tasmod.savestates.server.motion.RequestMotionPacketHandler;
import de.scribble.lp.tasmod.savestates.server.playerloading.SavestatePlayerLoadingPacket;
import de.scribble.lp.tasmod.savestates.server.playerloading.SavestatePlayerLoadingPacketHandler;
import de.scribble.lp.tasmod.tickratechanger.AdvanceTickratePacket;
import de.scribble.lp.tasmod.tickratechanger.AdvanceTickratePacket.AdvanceTickratePacketHandler;
import de.scribble.lp.tasmod.tickratechanger.ChangeTickratePacket;
import de.scribble.lp.tasmod.tickratechanger.ChangeTickratePacket.ChangeTickratePacketHandler;
import de.scribble.lp.tasmod.tickratechanger.PauseTickratePacket;
import de.scribble.lp.tasmod.tickratechanger.PauseTickratePacket.PauseTickratePacketHandler;
import de.scribble.lp.tasmod.ticksync.TickSyncPackage;
import de.scribble.lp.tasmod.ticksync.TickSyncPacketHandler;
import de.scribble.lp.tasmod.util.changestates.RequestStatePacket;
import de.scribble.lp.tasmod.util.changestates.RequestStatePacketHandler;
import de.scribble.lp.tasmod.util.changestates.SyncStatePacket;
import de.scribble.lp.tasmod.util.changestates.SyncStatePacketHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class CommonProxy {
	public static SimpleNetworkWrapper NETWORK;
	public static Logger logger = LogManager.getLogger("TASmod");

	public void preInit(FMLPreInitializationEvent ev) {
		NETWORK = NetworkRegistry.INSTANCE.newSimpleChannel("tasmod");
		int i = -1;
		
		// Tickrate
		NETWORK.registerMessage(ChangeTickratePacketHandler.class, ChangeTickratePacket.class, i++, Side.SERVER);
		NETWORK.registerMessage(ChangeTickratePacketHandler.class, ChangeTickratePacket.class, i++, Side.CLIENT);
		
		NETWORK.registerMessage(AdvanceTickratePacketHandler.class, AdvanceTickratePacket.class, i++, Side.SERVER);
		NETWORK.registerMessage(AdvanceTickratePacketHandler.class, AdvanceTickratePacket.class, i++, Side.CLIENT);
		
		NETWORK.registerMessage(PauseTickratePacketHandler.class, PauseTickratePacket.class, i++, Side.SERVER);
		NETWORK.registerMessage(PauseTickratePacketHandler.class, PauseTickratePacket.class, i++, Side.CLIENT);

		// Ticksync
		NETWORK.registerMessage(TickSyncPacketHandler.class, TickSyncPackage.class, i++, Side.CLIENT);

		// Sync State
		NETWORK.registerMessage(RequestStatePacketHandler.class, RequestStatePacket.class, i++, Side.CLIENT);

		NETWORK.registerMessage(SyncStatePacketHandler.class, SyncStatePacket.class, i++, Side.CLIENT);
		NETWORK.registerMessage(SyncStatePacketHandler.class, SyncStatePacket.class, i++, Side.SERVER);

		// Trigger savestates/loadstates on the client
		NETWORK.registerMessage(SavestatePacketHandler.class, SavestatePacket.class, i++, Side.SERVER);
		NETWORK.registerMessage(SavestatePacketHandler.class, SavestatePacket.class, i++, Side.CLIENT);
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
		
		NETWORK.registerMessage(FullPlayPacketHandler.class, FullPlayPacket.class, i++, Side.CLIENT);

	}

	public void init(FMLInitializationEvent ev) {
	}

	public void postInit(FMLPostInitializationEvent ev) {
	}

}
