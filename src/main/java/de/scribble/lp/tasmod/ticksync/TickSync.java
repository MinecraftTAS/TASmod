package de.scribble.lp.tasmod.ticksync;

import de.scribble.lp.tasmod.ClientProxy;
import de.scribble.lp.tasmod.gui.GuiMultiplayerTimeOut;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;

/**
 * Makes tickrate only dependent on the server, e.g. when the server lags, the client lags too
 * @author ScribbleLP
 *
 */
public class TickSync {
	private static int servertickcounter;
	private static int clienttickcounter;
	private static boolean enabled=true;
	private static int softLockTimer;
	
	/**
	 * Turns the ticksync on and off
	 * @param enable
	 */
	public static void sync(boolean enable) {
		enabled=enable;
	}
	
	/**
	 * If ticksync is enabled
	 * @return enabled
	 */
	public static boolean isEnabled() {
		return enabled;
	}
	
	/**
	 * The tick counter of the server
	 * @return
	 */
	public static int getServertickcounter() {
		return servertickcounter;
	}
	
	/**
	 * The tick counter of the client
	 * @return
	 */
	public static int getClienttickcounter() {
		return clienttickcounter;
	}
	
	/**
	 * Increment tickcounter and reset softlock timer, called in runTick
	 */
	public static void incrementClienttickcounter() {
		softLockTimer=0;
		clienttickcounter++;
	}
	
	/**
	 * Setting the tickcounter of the server coming from the TicksyncPacketHandler
	 * @param counter
	 */
	public static void setServerTickcounter(int counter) {
		servertickcounter=counter;
	}
	
	/**
	 * Reset the tick counter on server start
	 */
	public static void resetTickCounter(){
		clienttickcounter=0;
		servertickcounter=0;
	}
	
	public static int getTickAmount(Minecraft mc) {
		if(mc.world!=null) {
			int ticking=servertickcounter-clienttickcounter;
			
			if(ticking<0) {
				if(!ClientProxy.isDevEnvironment) { //For the Dev environment to stop a disconnect when debugging on the server side
					softLockTimer++;
				}
				if(softLockTimer==100) {
					mc.world.sendQuittingDisconnectingPacket();
					mc.loadWorld((WorldClient)null);
					mc.displayGuiScreen(new GuiMultiplayerTimeOut());
				}
			}
			return Math.max(ticking+1, 0);
		}else {
			return 1;
		}
	}
	
}
