package de.scribble.lp.tasmod;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.scribble.lp.tasmod.events.TASmodEvents;
import de.scribble.lp.tasmod.playback.PlaybackPacket;
import de.scribble.lp.tasmod.playback.PlaybackPacketHandler;
import de.scribble.lp.tasmod.savestates.chunkloading.SavestateChunkLoadingPacket;
import de.scribble.lp.tasmod.savestates.chunkloading.SavestateChunkLoadingPacketHandler;
import de.scribble.lp.tasmod.savestates.motion.MotionPacket;
import de.scribble.lp.tasmod.savestates.motion.MotionPacketHandler;
import de.scribble.lp.tasmod.savestates.playerloading.SavestatePlayerLoadingPacket;
import de.scribble.lp.tasmod.savestates.playerloading.SavestatePlayerLoadingPacketHandler;
import de.scribble.lp.tasmod.tickratechanger.TickratePacket;
import de.scribble.lp.tasmod.tickratechanger.TickratePacketHandler;
import de.scribble.lp.tasmod.ticksync.TickSyncPackage;
import de.scribble.lp.tasmod.ticksync.TickSyncPacketHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class CommonProxy {
	public static SimpleNetworkWrapper NETWORK;
	public static Logger logger= LogManager.getLogger("TASmod");
	public void preInit(FMLPreInitializationEvent ev) {
		NETWORK= NetworkRegistry.INSTANCE.newSimpleChannel("tasmod");
		NETWORK.registerMessage(TickratePacketHandler.class, TickratePacket.class, 0, Side.SERVER);
		NETWORK.registerMessage(TickratePacketHandler.class, TickratePacket.class, 1, Side.CLIENT);
		NETWORK.registerMessage(TickSyncPacketHandler.class, TickSyncPackage.class, 2, Side.CLIENT);
		NETWORK.registerMessage(PlaybackPacketHandler.class, PlaybackPacket.class, 3, Side.CLIENT);
		NETWORK.registerMessage(SavestateChunkLoadingPacketHandler.class, SavestateChunkLoadingPacket.class, 4, Side.SERVER);
		NETWORK.registerMessage(SavestatePlayerLoadingPacketHandler.class, SavestatePlayerLoadingPacket.class, 5, Side.CLIENT);
		NETWORK.registerMessage(SavestatePlayerLoadingPacketHandler.class, SavestatePlayerLoadingPacket.class, 6, Side.SERVER);
		NETWORK.registerMessage(MotionPacketHandler.class, MotionPacket.class, 7, Side.SERVER);
	}

	public void init(FMLInitializationEvent ev) {
		MinecraftForge.EVENT_BUS.register(new TASmodEvents());
	}

	public void postInit(FMLPostInitializationEvent ev) {
	}

}
