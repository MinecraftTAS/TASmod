package de.scribble.lp.tasmod.util;

import de.scribble.lp.tasmod.ClientProxy;
import de.scribble.lp.tasmod.CommonProxy;
import de.scribble.lp.tasmod.util.changestates.SyncStatePacket;
import net.minecraft.client.Minecraft;

/**
 * State of the input recorder
 * @author ScribbleLP
 *
 */
public enum TASstate {
	RECORDING,
	PLAYBACK,
	PAUSED,	// #124
	NONE;
	
	public static void setOrSend(TASstate state) {
		if(Minecraft.getMinecraft().player!=null) {
			CommonProxy.NETWORK.sendToServer(new SyncStatePacket(state));
		}else {
			ClientProxy.virtual.getContainer().setTASState(state);
		}
	}
	
	public int getIndex() {
		switch(this) {
		case NONE:
			return 0;
		case PLAYBACK:
			return 1;
		case RECORDING:
			return 2;
		case PAUSED:
			return 3;
		default:
			return 0;	
		}
	}
	
	public static TASstate fromIndex(int state) {
		switch (state) {
		case 0:
			return NONE;
		case 1:
			return PLAYBACK;
		case 2:
			return RECORDING;
		case 3:
			return PAUSED;
		default:
			return NONE;
		}
	}
}
