package de.scribble.lp.tasmod.tickratechanger;

import org.apache.logging.log4j.Logger;

import de.scribble.lp.tasmod.CommonProxy;
import de.scribble.lp.tasmod.TASmod;
import net.minecraft.entity.player.EntityPlayerMP;

/**
 * Controls the tickrate on the server side
 * 
 * The tickrate is controlled in MinecraftServer.run() where the server is halted for 50 milliseconds minus the time the tick took to execute.
 * <p>
 * To change the tickrate on server and all clients use {@link #changeTickrate(float)}.
 * <p>
 * You can individually set the tickrate with {@link #changeClientTickrate(float)} and {@link #changeServerTickrate(float)}.
 * <p>
 * 
 * 
 * @author Scribble
 *
 */
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
	 * The tickrate before {@link #ticksPerSecond} was changed to 0, used to toggle
	 * pausing
	 */
	public static float tickrateSaved=20F;
	
	/**
	 * True if the tickrate is 20 and the server should advance 1 tick
	 */
	public static boolean advanceTick=false;
	
	/**
	 * The logger used for logging. Has to be set seperately
	 */
	public static Logger logger;
	
	/**
	 * Changes both client and server tickrates.
	 * <p>
	 * Tickrates can be tickrate>=0 with 0 pausing the game.
	 * 
	 * @param tickrate The new tickrate of client and server
	 */
	public static void changeTickrate(float tickrate) {
		changeClientTickrate(tickrate);
		changeServerTickrate(tickrate);
	}
	
	public static void changeClientTickrate(float tickrate) {
		changeClientTickrate(tickrate, false);
	}
	
	/**
	 * Changes the tickrate of all clients. Sends a {@link ChangeTickratePacket}
	 * 
	 * @param tickrate The new tickrate of the client
	 * @param log If a message should logged
	 */
	public static void changeClientTickrate(float tickrate, boolean log) {
		if(log)
			log("Changing the tickrate "+ tickrate + " to all clients");
		CommonProxy.NETWORK.sendToAll(new ChangeTickratePacket(tickrate));
	}

	/**
	 * Changes the tickrate of the server
	 * 
	 * @param tickrate The new tickrate of the server
	 */
	public static void changeServerTickrate(float tickrate) {
		changeServerTickrate(tickrate, true);
	}
	
	/**
	 * Changes the tickrate of the server
	 * 
	 * @param tickrate The new tickrate of the server
	 * @param log If a message should logged
	 */
	public static void changeServerTickrate(float tickrate, boolean log) {
        if(tickrate>0) {
        	millisecondsPerTick = (long)(1000L / tickrate);
        }else if(tickrate==0) {
        	if(ticksPerSecond!=0) {
        		tickrateSaved=ticksPerSecond;
        	}
        }
        ticksPerSecond = tickrate;
        if(log) {
        	log("Setting the server tickrate to "+ ticksPerSecond);
        }
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
	 * Pauses the game without sending a command to the clients
	 * @param pause The state of the server
	 */
	public static void pauseServerGame(boolean pause) {
		if(pause) {
			changeServerTickrate(0F);
		}else {
			changeServerTickrate(tickrateSaved);
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
			log("Sending the current tickrate ("+ticksPerSecond+") to " +player.getName());
			CommonProxy.NETWORK.sendTo(new ChangeTickratePacket(ticksPerSecond), player);
		}
	}
	
	/**
	 * The message to log
	 * @param msg 
	 */
	private static void log(String msg) {
		logger.info(msg);
	}
}
