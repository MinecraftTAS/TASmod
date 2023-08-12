package com.minecrafttas.tasmod.commands.savetas;

import java.io.IOException;
import java.nio.charset.Charset;

import com.minecrafttas.server.interfaces.PacketID;
import com.minecrafttas.tasmod.TASmod;
import com.minecrafttas.tasmod.TASmodClient;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

public class SaveTASPacket implements PacketID {
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
					TASmodClient.virtual.saveInputs(name);
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
