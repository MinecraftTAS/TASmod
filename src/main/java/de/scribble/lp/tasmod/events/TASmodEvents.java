package de.scribble.lp.tasmod.events;

import de.scribble.lp.tasmod.CommonProxy;
import de.scribble.lp.tasmod.tickratechanger.TickrateChangerClient;
import de.scribble.lp.tasmod.tickratechanger.TickrateChangerServer;
import de.scribble.lp.tasmod.ticksync.TickSyncPackage;
import de.scribble.lp.tasmod.ticksync.TickSyncServer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedOutEvent;

public class TASmodEvents {
	
	//TODO Abandon forge and use mixins
	@SubscribeEvent
	public void playerLogin(PlayerLoggedInEvent ev) {
		TickSyncServer.resetTickCounter();
		CommonProxy.NETWORK.sendToAll(new TickSyncPackage(TickSyncServer.getServertickcounter(), true, TickSyncServer.isEnabled()));

		if (TickrateChangerClient.TICKS_PER_SECOND == 0) {
			TickrateChangerServer.changeServerTickrate(0F);
		}
	}

	@SubscribeEvent
	public void playerLogout(PlayerLoggedOutEvent ev) {
		if (TickrateChangerServer.TICKS_PER_SECOND == 0) {
			TickrateChangerServer.changeServerTickrate(20F);
		}
	}

}
