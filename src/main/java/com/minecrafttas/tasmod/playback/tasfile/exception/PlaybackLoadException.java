package com.minecrafttas.tasmod.playback.tasfile.exception;

import com.minecrafttas.tasmod.TASmod;

public class PlaybackLoadException extends RuntimeException {

	public PlaybackLoadException(long line, long tick, int subtick, String msg) {
		this(printTick(line, tick, subtick) + msg);
	}

	public PlaybackLoadException(String msg) {
		super(msg);
	}

	public PlaybackLoadException(long line, long tick, int subtick, String msg, Object... args) {
		this(printTick(line, tick, subtick) + msg, args);
	}

	public PlaybackLoadException(String msg, Object... args) {
		super(String.format(msg, args));
	}

	public PlaybackLoadException(long line, long tick, int subtick, Throwable cause) {
		this(printTick(line, tick, subtick), cause);
	}

	public PlaybackLoadException(Throwable cause) {
		super(cause);
	}

	public PlaybackLoadException(long line, long tick, int subtick, Throwable cause, String msg) {
		this(cause, printTick(line, tick, subtick) + msg);
	}

	public PlaybackLoadException(Throwable cause, String msg) {
		super(msg, cause, false, false);
	}

	public PlaybackLoadException(long line, long tick, int subtick, Throwable cause, String msg, Object... args) {
		this(cause, printTick(line, tick, subtick) + msg, args);
	}

	public PlaybackLoadException(Throwable cause, String msg, Object... args) {
		this(cause, String.format(msg, args));
	}

	private static String printTick(long line, long tick, int subtick) {
		return String.format("Line %s, Tick %s, Subtick %s: ", line, tick, subtick);
	}

	@Override
	public void printStackTrace() {
		TASmod.LOGGER.catching(this);
	}
}

