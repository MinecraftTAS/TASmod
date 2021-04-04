package de.scribble.lp.tasmod;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.pfannekuchen.killtherng.KillTheRng;
import de.pfannekuchen.killtherng.networking.UpdateSeedPacket;
import de.pfannekuchen.killtherng.networking.UpdateSeedPacketHandler;
import de.scribble.lp.tasmod.events.TASmodEvents;
import de.scribble.lp.tasmod.playback.PlaybackPacket;
import de.scribble.lp.tasmod.playback.PlaybackPacketHandler;
import de.scribble.lp.tasmod.recording.savestates.RecordingSavestatePacket;
import de.scribble.lp.tasmod.recording.savestates.RecordingSavestatePacketHandler;
import de.scribble.lp.tasmod.savestates.LoadstatePacket;
import de.scribble.lp.tasmod.savestates.LoadstatePacketHandler;
import de.scribble.lp.tasmod.savestates.SavestatePacket;
import de.scribble.lp.tasmod.savestates.SavestatePacketHandler;
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
		int i=-1;
		NETWORK.registerMessage(TickratePacketHandler.class, TickratePacket.class, i++, Side.SERVER);
		NETWORK.registerMessage(TickratePacketHandler.class, TickratePacket.class, i++, Side.CLIENT);
		NETWORK.registerMessage(TickSyncPacketHandler.class, TickSyncPackage.class, i++, Side.CLIENT);
		NETWORK.registerMessage(PlaybackPacketHandler.class, PlaybackPacket.class, i++, Side.CLIENT);
		NETWORK.registerMessage(SavestatePlayerLoadingPacketHandler.class, SavestatePlayerLoadingPacket.class, i++, Side.CLIENT);
		NETWORK.registerMessage(MotionPacketHandler.class, MotionPacket.class, i++, Side.SERVER);
		NETWORK.registerMessage(SavestatePacketHandler.class, SavestatePacket.class, i++, Side.SERVER);
		NETWORK.registerMessage(SavestatePacketHandler.class, SavestatePacket.class, i++, Side.CLIENT);
		NETWORK.registerMessage(LoadstatePacketHandler.class, LoadstatePacket.class, i++, Side.SERVER);
		NETWORK.registerMessage(LoadstatePacketHandler.class, LoadstatePacket.class, i++, Side.CLIENT);
		NETWORK.registerMessage(UpdateSeedPacketHandler.class, UpdateSeedPacket.class, i++, Side.SERVER);
		NETWORK.registerMessage(RecordingSavestatePacketHandler.class, RecordingSavestatePacket.class, i++, Side.CLIENT);
		KillTheRng.init();
	}

	public void init(FMLInitializationEvent ev) {
		MinecraftForge.EVENT_BUS.register(new TASmodEvents());
	}

	public void postInit(FMLPostInitializationEvent ev) {
	}

}
