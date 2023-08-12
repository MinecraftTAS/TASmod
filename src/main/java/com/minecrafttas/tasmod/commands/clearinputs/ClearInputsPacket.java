package com.minecrafttas.tasmod.commands.clearinputs;

import com.minecrafttas.server.interfaces.PacketID;
import com.minecrafttas.tasmod.TASmod;
import com.minecrafttas.tasmod.TASmodClient;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.PacketBuffer;

public class ClearInputsPacket implements PacketID{

	@Override
	public void handle(PacketSide side, EntityPlayer playerz) {
		if(side.isServer()) {
			EntityPlayerMP player = (EntityPlayerMP)playerz;
			player.getServerWorld().addScheduledTask(()->{
				if(player.canUseCommand(2, "clearinputs")) {
					TASmod.packetServer.sendToAll(this);
				}
			});
		} else {
			Minecraft.getMinecraft().addScheduledTask(()->{
				TASmodClient.virtual.getContainer().clear();
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
