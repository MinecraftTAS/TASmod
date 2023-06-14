package com.minecrafttas.tasmod.savestates.client;

import java.io.IOException;
import java.nio.charset.Charset;

import com.minecrafttas.tasmod.TASmod;
import com.minecrafttas.tasmod.savestates.server.exceptions.SavestateException;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;

public class InputSavestatesPacket implements Packet{
	private boolean mode;
	private String name;
	
	public InputSavestatesPacket() {
	}
	/**
	 * Makes a savestate of the recording on the <u>Client</u> 
	 * @param mode If true: Make a savestate, else load the savestate
	 * @param name Name of the savestated file
	 */
	public InputSavestatesPacket(boolean mode,String name) {
		this.mode=mode;
		this.name=name;
	}

	@Override
	public void handle(PacketSide side, EntityPlayer player) {
		if(side.isClient()) {
			if (mode == true) {
				try {
					InputSavestatesHandler.savestate(name);
				} catch (SavestateException e) {
					TASmod.LOGGER.error(e.getMessage());
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else {
				try {
					InputSavestatesHandler.loadstate(name);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
	}
	@Override
	public void serialize(PacketBuffer buf) {
		buf.writeInt(name.length());
		buf.writeCharSequence(name, Charset.defaultCharset());
		buf.writeBoolean(mode);
	}
	@Override
	public void deserialize(PacketBuffer buf) {
		int length=buf.readInt();
		name=(String) buf.readCharSequence(length, Charset.defaultCharset());
		mode=buf.readBoolean();
	}
}
