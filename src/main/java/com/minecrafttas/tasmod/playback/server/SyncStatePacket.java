package com.minecrafttas.tasmod.playback.server;

import com.minecrafttas.common.server.interfaces.PacketID;
import com.minecrafttas.tasmod.TASmod;
import com.minecrafttas.tasmod.TASmodClient;
import com.minecrafttas.tasmod.playback.PlaybackController;
import com.minecrafttas.tasmod.playback.PlaybackController.TASstate;
import com.minecrafttas.tasmod.util.TickScheduler.TickTask;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.TextComponentString;

/**
 * Syncs the current state of the input recorder with the state on the server side and with the state on all other clients
 * 
 * @author Scribble
 *
 */
public class SyncStatePacket implements PacketID {


	private short state;
	private boolean verbose;

	public SyncStatePacket() {
		state = 0;
	}

	public SyncStatePacket(TASstate state) {
		verbose = true;
		this.state = (short) state.ordinal();
	}

	public SyncStatePacket(TASstate state, boolean verbose) {
		this.verbose = verbose;
		this.state = (short) state.ordinal();
	}

	@Override
	public void serialize(PacketBuffer buf) {
		buf.writeShort(state);
		buf.writeBoolean(verbose);
	}

	@Override
	public void deserialize(PacketBuffer buf) {
		state = buf.readShort();
		verbose = buf.readBoolean();
	}

	protected TASstate getState() {
		return TASstate.fromIndex(state);
	}

	public boolean isVerbose() {
		return verbose;
	}

	@Override
	public void handle(PacketSide side, EntityPlayer player) {
		if(side.isServer()) {
			TASmod.containerStateServer.onPacket((EntityPlayerMP)player, getState());
		}
		else {
			
			TASstate state = getState();
			
			TickTask task = ()->{
				
				PlaybackController container = TASmodClient.virtual.getContainer();
				if (state != container.getState()) {
					String chatMessage = container.setTASState(state, verbose);
					if (!chatMessage.isEmpty()) {
						Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage(new TextComponentString(chatMessage));
					}
				}
				
			};
			
			
			if((state == TASstate.RECORDING || state == TASstate.PLAYBACK) && TASmodClient.tickratechanger.ticksPerSecond != 0) {
				TASmodClient.tickSchedulerClient.add(task);	// Starts a recording in the next tick
			} else {
				TASmodClient.gameLoopSchedulerClient.add(task);	// Starts a recording in the next frame
			}
		}
	}
}
