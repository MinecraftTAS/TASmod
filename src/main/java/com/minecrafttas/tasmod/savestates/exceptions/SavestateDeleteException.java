package com.minecrafttas.tasmod.savestates.exceptions;

public class SavestateDeleteException extends RuntimeException {

	public SavestateDeleteException() {
	}

	public SavestateDeleteException(String msg) {
		super(msg);
	}

	public SavestateDeleteException(String msg, Object... args) {
		super(String.format(msg, args));
	}

	public SavestateDeleteException(Throwable t, String msg) {
		super(msg, t);
	}

	public SavestateDeleteException(Throwable t, String msg, Object... args) {
		super(String.format(msg, args), t);
	}
}

