package com.minecrafttas.tasmod.commands.clearinputs;

import com.minecrafttas.tasmod.ClientProxy;
import com.minecrafttas.tasmod.TASmod;
import com.minecrafttas.tasmod.networking.Packet;
import com.minecrafttas.tasmod.networking.PacketSide;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.PacketBuffer;

public class ClearInputsPacket implements Packet{

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
				ClientProxy.virtual.getContainer().clear();
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
