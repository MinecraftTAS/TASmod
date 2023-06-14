package com.minecrafttas.tasmod.commands.fullplay;

import com.minecrafttas.tasmod.TASmodClient;
import com.minecrafttas.tasmod.events.OpenGuiEvents;
import com.minecrafttas.tasmod.playback.PlaybackController.TASstate;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;

public class FullPlayPacket implements Packet {

	@Override
	public void handle(PacketSide side, EntityPlayer player) {
		if(side.isClient()) {
			OpenGuiEvents.stateWhenOpened = TASstate.PLAYBACK;
			Minecraft mc = Minecraft.getMinecraft();
			TASmodClient.tickSchedulerClient.add(()->{
				mc.world.sendQuittingDisconnectingPacket();
				mc.loadWorld((WorldClient) null);
				mc.displayGuiScreen(new GuiMainMenu());
			});
		}
	}

	@Override
	public void serialize(PacketBuffer buf) {
	}

	@Override
	public void deserialize(PacketBuffer buf) {
	}

}
