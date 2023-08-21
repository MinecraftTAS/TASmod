package com.minecrafttas.tasmod.playback;

import java.nio.ByteBuffer;
import java.util.UUID;

import com.minecrafttas.common.server.Client.Side;
import com.minecrafttas.common.server.exception.PacketNotImplementedException;
import com.minecrafttas.common.server.exception.WrongSideException;
import com.minecrafttas.common.server.interfaces.ClientPacketHandler;
import com.minecrafttas.common.server.interfaces.PacketID;
import com.minecrafttas.tasmod.TASmod;
import com.minecrafttas.tasmod.TASmodClient;
import com.minecrafttas.tasmod.networking.TASmodBufferBuilder;
import com.minecrafttas.tasmod.networking.TASmodPackets;
import com.minecrafttas.tasmod.playback.PlaybackController.TASstate;
import com.minecrafttas.tasmod.util.LoggerMarkers;
import com.minecrafttas.tasmod.util.Scheduler.Task;

import net.minecraft.client.Minecraft;
import net.minecraft.util.text.TextComponentString;

public class TASstateClient implements ClientPacketHandler{
	
	public static void setStateClient(TASstate state) {
		TASmodClient.virtual.getContainer().setTASState(state);
	}
	
	public static void setOrSend(TASstate state) {
		if(Minecraft.getMinecraft().player!=null) {
			try {
				TASmodClient.client.send(new TASmodBufferBuilder(TASmodPackets.STATESYNC_INITIAL).writeTASState(state));
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			TASmodClient.virtual.getContainer().setTASState(state);
		}
	}
	
	@Override
	public PacketID[] getAcceptedPacketIDs() {
		return new TASmodPackets[] {TASmodPackets.STATESYNC_INITIAL, TASmodPackets.STATESYNC};
	}
	
	@Override
	public void onClientPacket(PacketID id, ByteBuffer buf, UUID clientID) throws PacketNotImplementedException, WrongSideException, Exception {
		TASmodPackets packet = (TASmodPackets) id;
		TASstate networkState = TASmodBufferBuilder.readTASState(buf);
		
		switch(packet) {
		case STATESYNC_INITIAL:
			throw new WrongSideException(id, Side.CLIENT);
			
		case STATESYNC:
			
			boolean verbose = TASmodBufferBuilder.readBoolean(buf);
			Task task = ()->{
				PlaybackController container = TASmodClient.virtual.getContainer();
				if (networkState != container.getState()) {
					
					String message = container.setTASState(networkState, verbose);
					
					if (!message.isEmpty()) {
						if(Minecraft.getMinecraft().world != null)
							Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage(new TextComponentString(message));
						else
							TASmod.LOGGER.debug(LoggerMarkers.Playback, message);
					} 
				}
				
			};
			
			
			if((networkState == TASstate.RECORDING || networkState == TASstate.PLAYBACK) && TASmodClient.tickratechanger.ticksPerSecond != 0) {
				TASmodClient.tickSchedulerClient.add(task);	// Starts a recording in the next tick
			} else {
				TASmodClient.gameLoopSchedulerClient.add(task);	// Starts a recording in the next frame
			}
			break;
			
		default:
				throw new PacketNotImplementedException(packet, this.getClass());
		}
	}

}
