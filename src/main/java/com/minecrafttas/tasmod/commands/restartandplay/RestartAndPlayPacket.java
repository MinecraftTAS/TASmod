package com.minecrafttas.tasmod.commands.restartandplay;

import java.nio.charset.Charset;

import com.minecrafttas.common.Configuration.ConfigOptions;
import com.minecrafttas.tasmod.TASmodClient;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;

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
				TASmodClient.config.set(ConfigOptions.FileToOpen, name);
				System.exit(0);
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
