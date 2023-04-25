package com.minecrafttas.tasmod.commands.savetas;

import java.io.IOException;
import java.nio.charset.Charset;

import com.minecrafttas.tasmod.ClientProxy;
import com.minecrafttas.tasmod.TASmod;
import com.minecrafttas.tasmod.networking.Packet;
import com.minecrafttas.tasmod.networking.PacketSide;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

public class SaveTASPacket implements Packet {
	String name;

	public SaveTASPacket() {
	}

	public SaveTASPacket(String recordingName) {
		name = recordingName;
	}

	@Override
	public void handle(PacketSide side, EntityPlayer playerz) {
		if (side.isClient()) {
			Minecraft mc = Minecraft.getMinecraft();
			mc.addScheduledTask(() -> {
				try {
					ClientProxy.virtual.saveInputs(name);
				} catch (IOException e) {
					mc.ingameGUI.getChatGUI().printChatMessage(new TextComponentString(TextFormatting.RED + e.getMessage()));
					return;
				}
				mc.ingameGUI.getChatGUI().printChatMessage(new TextComponentString(TextFormatting.GREEN + "Saved inputs to " + name + ".mctas"));
			});
		} else {
			EntityPlayerMP player = (EntityPlayerMP) playerz;
			player.getServerWorld().addScheduledTask(() -> {
				if (player.canUseCommand(2, "savetas")) {
					TASmod.packetServer.sendToAll(this);
				}
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
