package com.minecrafttas.tasmod.virtual;

import java.io.Serializable;

import com.minecrafttas.tasmod.playback.PlaybackSerialiser;

public class VirtualSubticks implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -2038332459318568985L;
	private float pitch;
	private float yaw;

	public VirtualSubticks() {
		pitch = 0;
		yaw = 0;
	}

	public VirtualSubticks(float pitch, float yaw) {
		this.pitch = pitch;
		this.yaw = yaw;
	}

	public float getPitch() {
		return pitch;
	}

	public float getYaw() {
		return yaw;
	}

	@Override
	public VirtualSubticks clone() {
		return new VirtualSubticks(pitch, yaw);
	}
	
	@Override
	public String toString() {
		return PlaybackSerialiser.SectionsV1.CAMERA.getName()+":"+pitch+";"+yaw;
	}
}
