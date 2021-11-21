package de.scribble.lp.tasmod.tickratechanger;

import de.scribble.lp.tasmod.CommonProxy;
import de.scribble.lp.tasmod.TASmod;
import net.minecraft.entity.player.EntityPlayerMP;

public class TickrateChangerServer {
	/**
	 * The current tickrate of the client
	 */
	public static float ticksPerSecond=20F;
	
	/**
	 * How long the server should sleep
	 */
	public static long millisecondsPerTick=50L;
	
	/**
	 * If true, interrupts the infinite loop that is used to enable tickratae 0 on the server
	 */
	public static boolean interrupt=false;
	
	/**
	 * The tickrate before {@link #ticksPerSecond} was changed to 0, used to toggle
	 * pausing
	 */
	public static float tickrateSaved=20F;
	/**
	 * True if the tickrate is 20 and the server should advance 1 tick
	 */
	public static boolean advanceTick=false;
	
	/**
	 * Changes both client and server tickrates
	 * 
	 * @param tickrate The new tickrate of client and server
	 */
	public static void changeTickrate(float tickrate) {
		changeClientTickrate(tickrate);
		changeServerTickrate(tickrate);
	}
	
	/**
	 * Changes the tickrate of all clients. Sends a {@link ChangeTickratePacket}
	 * 
	 * @param tickrate The new tickrate of the client
	 */
	public static void changeClientTickrate(float tickrate) {
		CommonProxy.NETWORK.sendToAll(new ChangeTickratePacket(tickrate));
	}

	/**
	 * Changes the tickrate of the server
	 * 
	 * @param tickrate The new tickrate of the server
	 */
	public static void changeServerTickrate(float tickrate) {
		interrupt=true;
        if(tickrate>0) {
        	millisecondsPerTick = (long)(1000L / tickrate);
        }else if(tickrate==0) {
        	if(ticksPerSecond!=0) {
        		tickrateSaved=ticksPerSecond;
        	}
        	millisecondsPerTick = Long.MAX_VALUE;
        }
        ticksPerSecond = tickrate;
        log("Setting the server tickrate to "+ ticksPerSecond);
	}
	
	/**
	 * Toggles between tickrate 0 and tickrate > 0
	 */
	public static void togglePause() {
    	if(ticksPerSecond>0) {
			changeTickrate(0);
    	}
    	else if (ticksPerSecond==0) {
    		changeTickrate(tickrateSaved);
    	}
    }
	
	/**
	 * Enables tickrate 0
	 * @param pause True if the game should be paused, false if unpause
	 */
	public static void pauseGame(boolean pause) {
		if(pause) {
			changeTickrate(0);
    	}
    	else {
    		advanceTick=false;
    		changeTickrate(tickrateSaved);
    	}
	}
	
	/**
	 * Advances the game by 1 tick.
	 */
    public static void advanceTick() {
    	advanceServerTick();
    	advanceClientTick();
    }
    
    /**
     * Sends a {@link AdvanceTickratePacket} to all clients
     */
    private static void advanceClientTick() {
    	CommonProxy.NETWORK.sendToAll(new AdvanceTickratePacket());
	}

    /**
     * Advances the server by 1 tick
     */
	private static void advanceServerTick() {
		if(ticksPerSecond==0) {
    		advanceTick=true;
    		changeServerTickrate(tickrateSaved);
    	}
	}

	/**
     * Fired when a player joined the server
     * @param player The player that joins the server
     */
	public static void joinServer(EntityPlayerMP player) {
		if(TASmod.getServerInstance().isDedicatedServer()) {
			TASmod.logger.info("Sending the current tickrate ({}) to {}", ticksPerSecond, player.getName());
			CommonProxy.NETWORK.sendTo(new ChangeTickratePacket(ticksPerSecond), player);
		}
	}
	
	private static void log(String msg) {
//		TASmod.logger.info(msg);
	}
}
