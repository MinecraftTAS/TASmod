package de.scribble.lp.tasmod.ticksync;

import de.scribble.lp.tasmod.ClientProxy;
import de.scribble.lp.tasmod.gui.GuiMultiplayerTimeOut;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;

/**
 * Makes tickrate only dependent on the server, e.g. when the server lags, the
 * client lags too
 * 
 * @author ScribbleLP
 *
 */
public class TickSyncClient {
	private int servertickcounter;
	private int clienttickcounter;
	private int softLockTimer;

	/**
	 * The tick counter of the server
	 * 
	 * @return
	 */
	public int getServertickcounter() {
		return servertickcounter;
	}

	/**
	 * The tick counter of the client
	 * 
	 * @return
	 */
	public int getClienttickcounter() {
		return clienttickcounter;
	}

	/**
	 * Increment tickcounter and reset softlock timer, called in runTick
	 */
	public void incrementClienttickcounter() {
		softLockTimer = 0;
		clienttickcounter++;
	}

	/**
	 * Setting the tickcounter of the server coming from the TicksyncPacketHandler
	 * 
	 * @param counter
	 */
	public void setServerTickcounter(int counter) {
		servertickcounter = counter;
	}

	/**
	 * Reset the tick counter on server start
	 */
	public void resetTickCounter() {
		clienttickcounter = 0;
		servertickcounter = 0;
	}

	public int getTickAmount(Minecraft mc) {
		if (mc.world != null) {
			int ticking = servertickcounter - clienttickcounter;
			if (ticking < 0) {
				
				if (!ClientProxy.isDevEnvironment) { // For the Dev environment to stop a disconnect when debugging on the server side
					softLockTimer++;
				}
				
				if (softLockTimer == 100) {
					mc.world.sendQuittingDisconnectingPacket();
					mc.loadWorld((WorldClient) null);
					mc.displayGuiScreen(new GuiMultiplayerTimeOut());
				}
			}
			return Math.max(ticking + 1, 0);
		} else {
			return 1;
		}
	}

}
