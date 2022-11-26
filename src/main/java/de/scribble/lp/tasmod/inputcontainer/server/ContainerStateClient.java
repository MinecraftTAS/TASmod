package de.scribble.lp.tasmod.inputcontainer.server;

import de.scribble.lp.tasmod.ClientProxy;
import de.scribble.lp.tasmod.inputcontainer.TASstate;
import net.minecraft.client.Minecraft;

public class ContainerStateClient {
	
	public static void setStateClient(TASstate state) {
		ClientProxy.virtual.getContainer().setTASState(state);
	}
	
	public static void setOrSend(TASstate state) {
		if(Minecraft.getMinecraft().player!=null) {
			ClientProxy.packetClient.sendToServer(new SyncStatePacket(state));
		}else {
			ClientProxy.virtual.getContainer().setTASState(state);
		}
	}
}
