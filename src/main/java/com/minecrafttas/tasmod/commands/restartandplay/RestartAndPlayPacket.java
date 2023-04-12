package com.minecrafttas.tasmod.commands.restartandplay;

import java.nio.charset.Charset;

import com.minecrafttas.tasmod.ClientProxy;
import com.minecrafttas.tasmod.networking.Packet;
import com.minecrafttas.tasmod.networking.PacketSide;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.FMLCommonHandler;

public class RestartAndPlayPacket implements Packet{
	private String name;
	
	public RestartAndPlayPacket() {
	}

	public RestartAndPlayPacket(String name) {
		this.name=name;
	}

	@Override
	public void handle(PacketSide side, EntityPlayer player) {
		if(side.isClient()) {
			try {
				Thread.sleep(100L);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			Minecraft.getMinecraft().addScheduledTask(() -> {
				ClientProxy.config.get("General", "fileToLoad", "").set(name);
				ClientProxy.config.save();
				FMLCommonHandler.instance().exitJava(0, false);
			});
		}
	}

	@Override
	public void serialize(PacketBuffer buf) {
		buf.writeInt(name.getBytes().length);
		buf.writeCharSequence(name, Charset.defaultCharset());
		
	}

	@Override
	public void deserialize(PacketBuffer buf) {
		int length = buf.readInt();
		name = (String) buf.readCharSequence(length, Charset.defaultCharset());
	}
}
