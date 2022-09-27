package de.scribble.lp.tasmod.ticksync;

import de.scribble.lp.tasmod.CommonProxy;
import de.scribble.lp.tasmod.TASmod;
import de.scribble.lp.tasmod.mixin.accessors.AccessorMinecraftServer;
import net.minecraft.entity.player.EntityPlayerMP;

public class TickSyncServer {
	
	private int serverticksync = 0;

	public void incrementServerTickCounter() {
		serverticksync++;
		sendToClients(false);
	}

	public void resetTickCounter() {
		((AccessorMinecraftServer) TASmod.getServerInstance().getServer()).tickCounter(0);
		serverticksync = 0;
		sendToClients(true);
	}

	public int getServertickcounter() {
		return serverticksync;
	}

	public void onJoinServer(EntityPlayerMP player) {
		resetTickCounter();
	}

	public void onServerTick() {
		/* Overflow prevention */
		if (getServertickcounter() == Integer.MAX_VALUE - 1) {
			resetTickCounter();
		} else {
			incrementServerTickCounter();
		}
	}
	
	private void sendToClients(boolean reset) {
		
		if(TASmod.ktrngHandler.isLoaded()) {
			CommonProxy.NETWORK.sendToAll(new TickSyncKTRNGPacket(serverticksync, reset, TASmod.ktrngHandler.advanceGlobalSeedServer()));
		}
		else {
			CommonProxy.NETWORK.sendToAll(new TickSyncPacket(serverticksync, reset));
		}
	}
}
