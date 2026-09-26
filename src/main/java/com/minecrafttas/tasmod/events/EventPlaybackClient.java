package com.minecrafttas.tasmod.events;

import com.minecrafttas.tasmod.mctcommon.events.EventListenerRegistry.EventBase;
import com.minecrafttas.tasmod.playback.PlaybackControllerClient;
import com.minecrafttas.tasmod.playback.PlaybackControllerClient.InputContainer;
import com.minecrafttas.tasmod.playback.PlaybackControllerClient.TASstate;

public interface EventPlaybackClient {

	/**
	 * Fired when
	 * {@link PlaybackControllerClient#setTASStateClient(com.minecrafttas.tasmod.playback.PlaybackControllerClient.TASstate, boolean)
	 * PlaybackControllerClient#setTASStateClient} is called
	 */
	@FunctionalInterface
	public interface EventControllerStateChange extends EventBase {

		/**
		 * Fired when
		 * {@link PlaybackControllerClient#setTASStateClient(com.minecrafttas.tasmod.playback.PlaybackControllerClient.TASstate, boolean)
		 * PlaybackControllerClient#setTASStateClient} is called
		 * 
		 * @param newstate The new state that the playback controller is about to be set
		 *                 to
		 * @param oldstate The current state that is about to be replaced by newstate
		 */
		public void onControllerStateChange(TASstate newstate, TASstate oldstate);
	}

	/**
	 * Fired after a player joined the world with a playback/recording running
	 */
	@FunctionalInterface
	public interface EventPlaybackJoinedWorld extends EventBase {

		/**
		 * Fired after a player joined the world with a playback/recording running
		 * 
		 * @param state The {@link PlaybackControllerClient#state state} of the
		 *              {@link PlaybackControllerClient} when the player joined the
		 *              world
		 */
		public void onPlaybackJoinedWorld(TASstate state);
	}

	/**
	 * Fired when a tick is being recorded
	 */
	@FunctionalInterface
	public interface EventRecordTick extends EventBase {

		/**
		 * Fired when a tick is being recorded
		 * 
		 * @param index     The index of the tick that is being recorded
		 * @param container The {@link InputContainer} that is being recorded
		 */
		public void onRecordTick(long index, InputContainer container);
	}

	/**
	 * Fired when a tick is being played back
	 */
	@FunctionalInterface
	public interface EventPlaybackTick extends EventBase {

		/**
		 * Fired when a tick is played back
		 * 
		 * @param index     The index of the tick that is played back
		 * @param container The {@link InputContainer} that is played back
		 */
		public void onPlaybackTick(long index, InputContainer container);
	}

	/**
	 * Fired when a recording is cleared
	 */
	@FunctionalInterface
	public interface EventRecordClear extends EventBase {

		/**
		 * Fired when a recording is cleared
		 */
		public void onRecordingClear();
	}

	/**
	 * Fired when an input is deleted
	 */
	@FunctionalInterface
	public interface EventInputDelete extends EventBase {

		/**
		 * Fired when an input is deleted
		 * 
		 * @param The index of the input
		 */
		public void onInputDelete(long index);
	}

	/**
	 * Fired when a tick is being played back before reading the inputs
	 */
	@FunctionalInterface
	public interface EventPlaybackTickPre extends EventBase {

		/**
		 * Fired when a tick is being played back before reading the inputs
		 * 
		 * @param index     The index of the tick that is played back
		 * @param container The {@link InputContainer} that is played back
		 */
		public void onPlaybackTickPre(long index);
	}
}

