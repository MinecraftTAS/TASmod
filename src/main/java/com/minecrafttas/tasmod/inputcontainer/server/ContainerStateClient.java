package com.minecrafttas.tasmod.inputcontainer.server;

import com.minecrafttas.tasmod.ClientProxy;
import com.minecrafttas.tasmod.inputcontainer.TASstate;

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
