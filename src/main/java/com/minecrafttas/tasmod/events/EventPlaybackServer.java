package com.minecrafttas.tasmod.events;

import com.minecrafttas.tasmod.mctcommon.events.EventListenerRegistry.EventBase;
import com.minecrafttas.tasmod.playback.PlaybackControllerClient.TASstate;
import com.minecrafttas.tasmod.playback.PlaybackControllerServer;

public interface EventPlaybackServer {

	/**
	 * Fired when {@link PlaybackControllerServer#setTASStateServer(TASstate)} is called
	 */
	@FunctionalInterface
	public interface EventControllerStateChange extends EventBase {
		/**
		 * Fired when {@link PlaybackControllerServer#setTASStateServer(TASstate)} is called
		 * @param newstate The new state that the playback controller is about to be set
		 * @param oldstate The current state that is about to be replaced by newstate
		 */
		public void onControllerStateChange(TASstate newstate, TASstate oldstate);
	}

	/**
	 * Fired when a recording is cleared in {@link PlaybackControllerServer#clearInputs()}
	 */
	@FunctionalInterface
	public interface EventRecordClear extends EventBase {

		/**
		 * Fired when a recording is cleared in {@link PlaybackControllerServer#clearInputs()}
		 */
		public void onRecordingClear();
	}
}

