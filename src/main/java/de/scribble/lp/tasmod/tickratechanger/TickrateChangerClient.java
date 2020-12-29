package de.scribble.lp.tasmod.tickratechanger;

import org.lwjgl.input.Keyboard;

import de.scribble.lp.tasmod.CommonProxy;
import net.minecraft.client.Minecraft;

public class TickrateChangerClient {
	public static float TICKS_PER_SECOND=20f;
	public static long MILISECONDS_PER_TICK=50L;
	public static boolean INTERRUPT=false;
	public static TickrateChangerClient INSTANCE= new TickrateChangerClient();
	public static float TICKRATE_SAVED=20F;
	public static boolean ADVANCE_TICK=false;
	public static int cooldownKeyPause;
	public static int cooldownKeyAdvance;
	

	public static void changeClientTickrate(float tickrate) {
		Minecraft mc = Minecraft.getMinecraft();
		if (tickrate > 0) {
			mc.timer.tickLength = 1000F / tickrate;
		} else if (tickrate == 0F) {
			TICKRATE_SAVED=TICKS_PER_SECOND;
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
    		CommonProxy.NETWORK.sendToServer(new TickratePacket(true, 20, false));
    	}else {
    		advanceClientTick();
    	}
    }
    /**
     * Bypasses the tick system
     */
    public void bypass() {
		if (Keyboard.isKeyDown(Keyboard.KEY_F8)&&cooldownKeyPause==0) {
			cooldownKeyPause=10;
			pauseUnpauseGame();
		} else if (Keyboard.isKeyDown(Keyboard.KEY_F9)&&cooldownKeyAdvance==0) {
			cooldownKeyAdvance=10;
			advanceTick();
		}
    }

	public static void advanceClientTick() {
		changeClientTickrate(TICKRATE_SAVED);
		ADVANCE_TICK=true;
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
