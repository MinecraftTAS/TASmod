package de.scribble.lp.tasmod.tickratechanger;

import de.scribble.lp.tasmod.ClientProxy;
import de.scribble.lp.tasmod.CommonProxy;
import de.scribble.lp.tasmod.util.TASstate;
import de.scribble.lp.tasmod.virtual.VirtualKeybindings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiControls;

public class TickrateChangerClient {
	public static float TICKS_PER_SECOND=20f;
	public static long MILISECONDS_PER_TICK=50L;
	public static boolean INTERRUPT=false;
	public static float TICKRATE_SAVED=20F;
	public static boolean ADVANCE_TICK=false;
	public static boolean WASZERO=false;
	

	public static void changeClientTickrate(float tickrate) {
		Minecraft mc = Minecraft.getMinecraft();
		if (tickrate > 0) {
			mc.timer.tickLength = 1000F / tickrate;
		} else if (tickrate == 0F) {
			if(TICKS_PER_SECOND!=0) {
				TICKRATE_SAVED=TICKS_PER_SECOND;
			}
			mc.timer.tickLength = Float.MAX_VALUE;
		}
		TICKS_PER_SECOND = tickrate;
	}

	public static void pauseUnpauseGame() {
		if(Minecraft.getMinecraft().world!=null) {
			CommonProxy.NETWORK.sendToServer(new TickratePacket(false, 20, true));
		}else {
			pauseUnpauseClient();
		}
    }
    public static void advanceTick() {
    	if(Minecraft.getMinecraft().world!=null) {
    		advanceServerTick();
    	}else {
    		advanceClientTick();
    	}
    }
    /**
     * Bypasses the tick system
     */
    public static void bypass() {
    	if(Minecraft.getMinecraft().currentScreen instanceof GuiControls) {
    		if(TICKS_PER_SECOND==0&&WASZERO==false) {
    			changeClientTickrate(20);
    			ClientProxy.virtual.getContainer().setTASState(TASstate.NONE);
    			WASZERO=true;
    		}
    		return;
    	}
    	if(WASZERO==true) {
			changeClientTickrate(0);
			WASZERO=false;
		}
    }

	public static void advanceClientTick() {
		changeClientTickrate(TICKRATE_SAVED);
		ADVANCE_TICK=true;
	}
	
	/**
	 * Sends a message to the server so it should advance the server ticks
	 */
	public static void advanceServerTick() {
		CommonProxy.NETWORK.sendToServer(new TickratePacket(true, 20, false));
	}
	/**
	 * Pauses and unpauses the client, used in main menus
	 */
	public static void pauseUnpauseClient() {
		if(TICKS_PER_SECOND>0) {
    		TICKRATE_SAVED=TICKS_PER_SECOND;
			changeClientTickrate(0F);
    	}
    	else if (TICKS_PER_SECOND==0) {
    		changeClientTickrate(TICKRATE_SAVED);
    	}
	}
}
