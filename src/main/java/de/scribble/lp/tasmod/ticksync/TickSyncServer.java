package de.scribble.lp.tasmod.ticksync;

import de.scribble.lp.tasmod.CommonProxy;
import de.scribble.lp.tasmod.TASmod;
import de.scribble.lp.tasmod.mixin.accessors.AccessorMinecraftServer;
import net.minecraft.entity.player.EntityPlayerMP;

public class TickSyncServer {
	private static int serverticksync = 0;
	private static boolean enabled = true;

	public static void sync(boolean enable) {
		enabled = enable;
	}

	public static void incrementServerTickCounter() {
		serverticksync++;
	}

	public static void resetTickCounter() {
		((AccessorMinecraftServer) TASmod.getServerInstance().getServer()).tickCounter(0);
		serverticksync = 0;
	}

	public static int getServertickcounter() {
		return serverticksync;
	}

	public static boolean isEnabled() {
		return enabled;
	}
	
	public static void joinServer(EntityPlayerMP player) {
		TickSyncServer.resetTickCounter();
		CommonProxy.NETWORK.sendToAll(new TickSyncPackage(TickSyncServer.getServertickcounter(), true, TickSyncServer.isEnabled()));
	}
}
