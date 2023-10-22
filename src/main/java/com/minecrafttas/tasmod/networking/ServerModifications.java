package com.minecrafttas.tasmod.networking;

import com.minecrafttas.tasmod.TASmod;
import com.minecrafttas.tasmod.TASmodClient;
import com.minecrafttas.tasmod.events.EventClient.EventClientTickrateChange;
import com.minecrafttas.tasmod.events.EventServer.EventServerTickrateChange;

public class ServerModifications implements EventClientTickrateChange, EventServerTickrateChange{

	@Override
	public void onServerTickrateChange(float tickrate) {
		long timeout;
		if(tickrate>0) {
			long millisecondsPerTick = (long) (1000L / tickrate);
			timeout = millisecondsPerTick * 10 * 20;
			
		} else {
			timeout = Long.MAX_VALUE;
		}
		TASmod.LOGGER.debug("Setting server timeout to {}", timeout);
		TASmod.server.setTimeoutTime(timeout);
	}

	@Override
	public void onClientTickrateChange(float tickrate) {
		long timeout;
		if(tickrate>0) {
			long millisecondsPerTick = (long) (1000L / tickrate);
			timeout = millisecondsPerTick * 10 * 20;
			
		} else {
			timeout = Long.MAX_VALUE;
		}
		TASmod.LOGGER.debug("Setting client timeout to {}", timeout);
		TASmodClient.client.setTimeoutTime(timeout);
	}

}
