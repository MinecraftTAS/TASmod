package com.minecrafttas.tasmod.handlers;

import java.nio.ByteBuffer;

import com.minecrafttas.tasmod.mctcommon.networking.ByteBufferBuilder;
import com.minecrafttas.tasmod.mctcommon.networking.Client.Side;
import com.minecrafttas.tasmod.mctcommon.networking.exception.PacketNotImplementedException;
import com.minecrafttas.tasmod.mctcommon.networking.exception.WrongSideException;
import com.minecrafttas.tasmod.mctcommon.networking.interfaces.ClientPacketHandler;
import com.minecrafttas.tasmod.mctcommon.networking.interfaces.PacketID;
import com.minecrafttas.tasmod.mctcommon.networking.interfaces.ServerPacketHandler;
import com.minecrafttas.tasmod.TASmod;
import com.minecrafttas.tasmod.TASmodClient;
import com.minecrafttas.tasmod.events.EventPlaybackClient;
import com.minecrafttas.tasmod.networking.TASmodBufferBuilder;
import com.minecrafttas.tasmod.playback.PlaybackControllerClient;
import com.minecrafttas.tasmod.playback.PlaybackControllerClient.TASstate;
import com.minecrafttas.tasmod.registries.TASmodPackets;

/**
 * Feature for starting a recording after playing back a certain number of ticks
 * 
 * @author Scribble
 */
public class PlayUntilHandler implements ClientPacketHandler, ServerPacketHandler, EventPlaybackClient.EventPlaybackTickPre {

	/**
	 * If not null, play until a certain point
	 */
	private Integer playUntil = null;

	@Override
	public void onPlaybackTickPre(long index) {
		/* Playuntil logic */
		if (playUntil != null && playUntil == index) {
			TASmodClient.tickratechanger.pauseGame(true);
			PlaybackControllerClient controller = TASmodClient.controller;
			controller.setTASState(TASstate.RECORDING);
			controller.setIndex(controller.index() - 1);
			for (long i = controller.size() - 1; i >= index; i--) {
				controller.remove(i);
			}
			playUntil = null;
		}
	}

	public boolean isActive() {
		return playUntil != null;
	}

	public void setPlayUntil(int until) {
		this.playUntil = until;
	}

	@Override
	public PacketID[] getAcceptedPacketIDs() {
		return new TASmodPackets[] { TASmodPackets.PLAYBACK_PLAYUNTIL };
	}

	@Override
	public void onClientPacket(PacketID id, ByteBuffer buf, String username) throws PacketNotImplementedException, WrongSideException, Exception {
		TASmodPackets packet = (TASmodPackets) id;

		switch (packet) {
			case PLAYBACK_PLAYUNTIL:
				int until = ByteBufferBuilder.readInt(buf);
				setPlayUntil(until);
				break;
			default:
				throw new PacketNotImplementedException(packet, this.getClass(), Side.SERVER);
		}
	}

	@Override
	public void onServerPacket(PacketID id, ByteBuffer buf, String username) throws PacketNotImplementedException, WrongSideException, Exception {
		TASmodPackets packet = (TASmodPackets) id;

		switch (packet) {
			case PLAYBACK_PLAYUNTIL:
				TASmod.server.sendToAll(new TASmodBufferBuilder(buf));
				break;
			default:
				throw new PacketNotImplementedException(packet, this.getClass(), Side.SERVER);
		}
	}
}

