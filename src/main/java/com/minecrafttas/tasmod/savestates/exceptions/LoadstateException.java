package com.minecrafttas.tasmod.savestates.exceptions;

public class LoadstateException extends RuntimeException {

	public LoadstateException(String msg) {
		super(msg);
	}

	public LoadstateException(String msg, Object... args) {
		super(String.format(msg, args));
	}

	public LoadstateException(Throwable t, String msg) {
		super(msg, t);
	}

	public LoadstateException(Throwable t, String msg, Object... args) {
		super(String.format(msg, args), t);
	}
}

