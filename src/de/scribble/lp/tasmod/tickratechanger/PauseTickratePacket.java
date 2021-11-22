package de.scribble.lp.tasmod.tickratechanger;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * Sets the game to tickrate 0 and back
 * 
 * @author ScribbleLP
 *
 */
public class PauseTickratePacket implements IMessage {

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

	@Override
	public void fromBytes(ByteBuf buf) {
		status = buf.readShort();
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeShort(status);
	}

	public State getState() {
		return State.fromShort(status);
	}

	public static class PauseTickratePacketHandler implements IMessageHandler<PauseTickratePacket, IMessage> {

		public PauseTickratePacketHandler() {
		}

		@Override
		public IMessage onMessage(PauseTickratePacket message, MessageContext ctx) {
			if (ctx.side.isServer()) {
				if (ctx.getServerHandler().player.canUseCommand(2, "tickrate")) {
					if (message.getState() == State.PAUSE)
						TickrateChangerServer.pauseGame(true);
					else if (message.getState() == State.UNPAUSE)
						TickrateChangerServer.pauseGame(false);
					else if (message.getState() == State.TOGGLE)
						TickrateChangerServer.togglePause();
				}
			} else if (ctx.side.isClient()) {
				if (message.getState() == State.PAUSE)
					TickrateChangerClient.pauseClientGame(true);
				else if (message.getState() == State.UNPAUSE)
					TickrateChangerClient.pauseClientGame(false);
				else if (message.getState() == State.TOGGLE)
					TickrateChangerClient.togglePauseClient();
			}
			return null;
		}

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
}
