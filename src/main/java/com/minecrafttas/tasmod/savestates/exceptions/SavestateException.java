package com.minecrafttas.tasmod.savestates.exceptions;

public class SavestateException extends RuntimeException {

	public SavestateException() {
	}

	public SavestateException(String msg) {
		super(msg);
	}

	public SavestateException(String msg, Object... args) {
		super(String.format(msg, args));
	}

	public SavestateException(Throwable t, String msg) {
		super(msg, t);
	}

	public SavestateException(Throwable t, String msg, Object... args) {
		super(String.format(msg, args), t);
	}
}

