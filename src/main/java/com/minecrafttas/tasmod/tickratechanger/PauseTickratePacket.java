package com.minecrafttas.tasmod.tickratechanger;

import com.minecrafttas.tasmod.networking.Packet;
import com.minecrafttas.tasmod.networking.PacketSide;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;

/**
 * Sets the game to tickrate 0 and back
 * 
 * @author ScribbleLP
 *
 */
public class PauseTickratePacket implements Packet {

	private short status;

	/**
	 * Toggles the tickrate between 0 and tickrate > 0
	 */
	public PauseTickratePacket() {
		status = 0;
	}

	/**
	 * Changes the state to either PAUSE UNPAUSE or TOGGLED
	 * 
	 * @param state The state
	 */
	public PauseTickratePacket(State state) {
		this.status = state.toShort();
	}

	public State getState() {
		return State.fromShort(status);
	}


	/**
	 * Can be {@link State#PAUSE}, {@link State#UNPAUSE} or {@link State#TOGGLE}
	 * 
	 * @author ScribbleLP
	 *
	 */
	public enum State {
		/**
		 * Set's the game to tickrate 0
		 */
		PAUSE((short) 1),
		/**
		 * Set's the game to "tickrate saved"
		 */
		UNPAUSE((short) 2),
		/**
		 * Toggles between {@link #PAUSE} and {@link #UNPAUSE}
		 */
		TOGGLE((short) 0);

		private short id;

		State(short i) {
			id = i;
		}

		public short toShort() {
			return id;
		}

		public static State fromShort(short i) {
			switch (i) {
			case 1:
				return PAUSE;
			case 2:
				return UNPAUSE;
			default:
				return TOGGLE;
			}
		}
	}

	@Override
	public void handle(PacketSide side, EntityPlayer player) {
		if (side.isServer()) {
			if (player.canUseCommand(2, "tickrate")) {
				State state = getState();
				if (state == State.PAUSE)
					TickrateChangerServer.pauseGame(true);
				else if (state == State.UNPAUSE)
					TickrateChangerServer.pauseGame(false);
				else if (state == State.TOGGLE)
					TickrateChangerServer.togglePause();
			}
		} else if (side.isClient()) {
			State state = getState();
			if (state == State.PAUSE)
				TickrateChangerClient.pauseClientGame(true);
			else if (state == State.UNPAUSE)
				TickrateChangerClient.pauseClientGame(false);
			else if (state == State.TOGGLE)
				TickrateChangerClient.togglePauseClient();
		}
	}

	@Override
	public void serialize(PacketBuffer buf) {
		buf.writeShort(status);
	}

	@Override
	public void deserialize(PacketBuffer buf) {
		status = buf.readShort();
	}
}
