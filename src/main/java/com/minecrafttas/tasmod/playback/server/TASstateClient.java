package com.minecrafttas.tasmod.playback.server;

import com.minecrafttas.tasmod.ClientProxy;
import com.minecrafttas.tasmod.playback.PlaybackController.TASstate;

import net.minecraft.client.Minecraft;

public class TASstateClient {
	
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
