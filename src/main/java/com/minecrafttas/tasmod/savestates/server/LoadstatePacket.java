package com.minecrafttas.tasmod.savestates.server;

import com.minecrafttas.server.interfaces.PacketID;
import com.minecrafttas.tasmod.TASmod;
import com.minecrafttas.tasmod.savestates.server.SavestateHandler.SavestateState;
import com.minecrafttas.tasmod.savestates.server.chunkloading.SavestatesChunkControl;
import com.minecrafttas.tasmod.savestates.server.exceptions.LoadstateException;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

public class LoadstatePacket implements PacketID {

	public int index;

	/**
	 * Load a savestate at the current index
	 */
	public LoadstatePacket() {
		index = -1;
	}

	/**
	 * Load the savestate at the specified index
	 * 
	 * @param index The index to load the savestate
	 */
	public LoadstatePacket(int index) {
		this.index = index;
	}

	@Override
	public void handle(PacketSide side, EntityPlayer playerz) {
		if (side.isServer()) {
			EntityPlayerMP player = (EntityPlayerMP) playerz;
			player.getServerWorld().addScheduledTask(()->{
				if (!player.canUseCommand(2, "tickrate")) {
					player.sendMessage(new TextComponentString(TextFormatting.RED + "You don't have permission to do that"));
					return;
				}
				try {
					TASmod.savestateHandler.loadState(index, true);
				} catch (LoadstateException e) {
					player.sendMessage(new TextComponentString(TextFormatting.RED + "Failed to load a savestate: " + e.getMessage()));
				} catch (Exception e) {
					player.sendMessage(new TextComponentString(TextFormatting.RED + "Failed to load a savestate: " + e.getCause().toString()));
					e.printStackTrace();
				} finally {
					TASmod.savestateHandler.state = SavestateState.NONE;
				}
			});
		} else {
			Minecraft.getMinecraft().addScheduledTask(()->{
				SavestatesChunkControl.unloadAllClientChunks();
			});
		}
	}

	@Override
	public void serialize(PacketBuffer buf) {
		buf.writeInt(index);
	}

	@Override
	public void deserialize(PacketBuffer buf) {
		index = buf.readInt();
	}

}
