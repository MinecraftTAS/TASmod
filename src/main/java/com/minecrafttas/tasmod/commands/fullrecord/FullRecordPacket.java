package com.minecrafttas.tasmod.commands.fullrecord;

import com.minecrafttas.tasmod.ClientProxy;
import com.minecrafttas.tasmod.events.OpenGuiEvents;
import com.minecrafttas.tasmod.inputcontainer.TASstate;
import com.minecrafttas.tasmod.networking.Packet;
import com.minecrafttas.tasmod.networking.PacketSide;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;

public class FullRecordPacket implements Packet{

	@Override
	public void handle(PacketSide side, EntityPlayer player) {
		if(side.isClient()) {
			OpenGuiEvents.stateWhenOpened = TASstate.RECORDING;
			ClientProxy.virtual.getContainer().clear();
			Minecraft mc = Minecraft.getMinecraft();
			mc.addScheduledTask(()->{
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
