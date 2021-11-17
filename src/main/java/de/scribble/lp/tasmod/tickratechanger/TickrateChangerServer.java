package de.scribble.lp.tasmod.tickratechanger;

import de.scribble.lp.tasmod.CommonProxy;
import de.scribble.lp.tasmod.TASmod;
import net.minecraft.entity.player.EntityPlayerMP;

public class TickrateChangerServer {
	public static float TICKS_PER_SECOND=20F;
	public static long MILISECONDS_PER_TICK=50L;
	public static boolean INTERRUPT=false;
	public static float TICKRATE_SAVED=20F;
	public static boolean ADVANCE_TICK=false;
	public static int cooldownKeyPause;
	public static int cooldownKeyAdvance;
	
	public static void changeClientTickrate(float tickrate) {
		CommonProxy.NETWORK.sendToAll(new TickratePacket(false, tickrate, false));
	}

	public static void changeServerTickrate(float tickrate) {
		INTERRUPT=true;
        if(tickrate>0) {
        	MILISECONDS_PER_TICK = (long)(1000L / tickrate);
        }else if(tickrate==0) {
        	if(TICKS_PER_SECOND!=0) {
        		TICKRATE_SAVED=TICKS_PER_SECOND;
        	}
        	MILISECONDS_PER_TICK = Long.MAX_VALUE;
        }
        TICKS_PER_SECOND = tickrate;
	}
	
	public static void togglePause() {
    	if(TICKS_PER_SECOND>0) {
			changeServerTickrate(0F);
			changeClientTickrate(0F);
    	}
    	else if (TICKS_PER_SECOND==0) {
    		changeServerTickrate(TICKRATE_SAVED);
    		changeClientTickrate(TICKRATE_SAVED);
    	}
    }
	
	/**
	 * Enables tickrate 0
	 * @param pause True if the game should be paused, false if unpause
	 */
	public static void pauseGame(boolean pause) {
		if(pause) {
			changeServerTickrate(0F);
			changeClientTickrate(0F);
    	}
    	else {
    		changeServerTickrate(TICKRATE_SAVED);
    		changeClientTickrate(TICKRATE_SAVED);
    	}
	}
	
    public static void advanceTick() {
    	if(TICKS_PER_SECOND==0) {
    		ADVANCE_TICK=true;
    		changeServerTickrate(TICKRATE_SAVED);
    		CommonProxy.NETWORK.sendToAll(new TickratePacket(true, 0F, false));
    	}
    }
    
    /**
     * Fired when a player joined the server
     * @param player
     */
	public static void joinServer(EntityPlayerMP player) {
		if(TASmod.getServerInstance().isDedicatedServer()) {
			TASmod.logger.info("Sending the current tickrate ({}) to {}", TICKS_PER_SECOND, player.getName());
			CommonProxy.NETWORK.sendTo(new TickratePacket(false, TICKS_PER_SECOND, false), player);
		}
	}
}
