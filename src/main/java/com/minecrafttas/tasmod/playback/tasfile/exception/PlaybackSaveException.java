package com.minecrafttas.tasmod.playback.tasfile.exception;

public class PlaybackSaveException extends PlaybackLoadException {

	public PlaybackSaveException(String msg) {
		super(msg);
	}

	public PlaybackSaveException(String msg, Object... args) {
		super(String.format(msg, args));
	}

	public PlaybackSaveException(Throwable cause, String msg) {
		super(msg, cause, false, false);
	}

	public PlaybackSaveException(Throwable cause, String msg, Object... args) {
		super(cause, String.format(msg, args));
	}
}

