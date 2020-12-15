package de.scribble.lp.tasmod.events;

import de.scribble.lp.tasmod.CommonProxy;
import de.scribble.lp.tasmod.playback.PlaybackPacket;
import de.scribble.lp.tasmod.ticksync.TickSyncPackage;
import de.scribble.lp.tasmod.ticksync.TickSyncServer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;

public class TASmodEvents {
	@SubscribeEvent
	public void playerLogin(PlayerLoggedInEvent ev) {
		TickSyncServer.resetTickCounter();
		CommonProxy.NETWORK.sendToAll(new TickSyncPackage(TickSyncServer.getServertickcounter(), true, TickSyncServer.isEnabled()));
		
		CommonProxy.NETWORK.sendToAll(new PlaybackPacket());
	}
}
