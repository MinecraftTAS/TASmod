package com.minecrafttas.tasmod.playback.server;

import com.minecrafttas.tasmod.TASmodClient;
import com.minecrafttas.tasmod.playback.PlaybackController.TASstate;

import net.minecraft.client.Minecraft;

public class TASstateClient {
	
	public static void setStateClient(TASstate state) {
		TASmodClient.virtual.getContainer().setTASState(state);
	}
	
	public static void setOrSend(TASstate state) {
		if(Minecraft.getMinecraft().player!=null) {
			TASmodClient.packetClient.sendToServer(new SyncStatePacket(state));
		}else {
			TASmodClient.virtual.getContainer().setTASState(state);
		}
	}
}
