package de.scribble.lp.tasmod.util;

import de.scribble.lp.tasmod.ClientProxy;
import de.scribble.lp.tasmod.CommonProxy;
import de.scribble.lp.tasmod.util.changestates.SyncStatePacket;
import net.minecraft.client.Minecraft;

public enum TASstate {
	RECORDING,
	PLAYBACK,
	NONE;
	
	public static void setOrSend(TASstate state) {
		if(Minecraft.getMinecraft().player!=null) {
			CommonProxy.NETWORK.sendToServer(new SyncStatePacket(state));
		}else {
			ClientProxy.virtual.getContainer().setTASState(state);
		}
	}
}
