package de.scribble.lp.tasmod;

import de.scribble.lp.tasmod.tickratechanger.TickratePacket;
import de.scribble.lp.tasmod.tickratechanger.TickratePacketHandler;
import de.scribble.lp.tasmod.ticksync.TickSyncPackage;
import de.scribble.lp.tasmod.ticksync.TickSyncPacketHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class CommonProxy {
	public static SimpleNetworkWrapper NETWORK;
	public void preInit(FMLPreInitializationEvent ev) {
		NETWORK= NetworkRegistry.INSTANCE.newSimpleChannel("tasmod");
		NETWORK.registerMessage(TickratePacketHandler.class, TickratePacket.class, 0, Side.SERVER);
		NETWORK.registerMessage(TickratePacketHandler.class, TickratePacket.class, 1, Side.CLIENT);
		NETWORK.registerMessage(TickSyncPacketHandler.class, TickSyncPackage.class, 2, Side.CLIENT);
	}

	public void init(FMLInitializationEvent ev) {
	}

	public void postInit(FMLPostInitializationEvent ev) {
	}

}
