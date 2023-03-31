package com.minecrafttas.tasmod.inputcontainer.server;

import com.minecrafttas.tasmod.TASmod;
import com.minecrafttas.tasmod.inputcontainer.TASstate;
import com.minecrafttas.tasmod.networking.PacketSide;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;

/**
 * Sends the state of the client to the server on server join. Is only applied if you are the first player on the server and if you are operator
 * 
 * @author Scribble
 *
 */
public class InitialSyncStatePacket extends SyncStatePacket {

	public InitialSyncStatePacket() {
		super();
	}
	
	public InitialSyncStatePacket(TASstate state) {
		super(state);
	}
	
	@Override
	public void handle(PacketSide side, EntityPlayer player) {
		if(side.isServer())
			TASmod.containerStateServer.onInitialPacket((EntityPlayerMP)player, this.getState());
	}

}
