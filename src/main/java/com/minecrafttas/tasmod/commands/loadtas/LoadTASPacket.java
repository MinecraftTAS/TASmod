package com.minecrafttas.tasmod.commands.loadtas;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import com.minecrafttas.server.interfaces.PacketID;
import com.minecrafttas.tasmod.TASmod;
import com.minecrafttas.tasmod.TASmodClient;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

public class LoadTASPacket implements PacketID{
	private String name;
	
	public LoadTASPacket() {
	}

	public LoadTASPacket(String name) {
		this.name=name;
	}

	@Override
	public void handle(PacketSide side, EntityPlayer playerz) {
		if (side.isServer()) {
			EntityPlayerMP player = (EntityPlayerMP) playerz;
			player.getServerWorld().addScheduledTask(() -> {
				if (player.canUseCommand(2, "loadtas")) {
					TASmod.packetServer.sendToAll(this);
				}
			});
		} else {
			Minecraft mc = Minecraft.getMinecraft();
			mc.addScheduledTask(() -> {
				try {
					TASmodClient.virtual.loadInputs(name);
				} catch (IOException e) {
					mc.ingameGUI.getChatGUI().printChatMessage(new TextComponentString(TextFormatting.RED + e.getMessage()));
					return;
				}
				mc.ingameGUI.getChatGUI().printChatMessage(new TextComponentString(TextFormatting.GREEN + "Loaded inputs from " + name + ".mctas"));
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
		name = (String) buf.readCharSequence(length, StandardCharsets.UTF_8);
	}
}
