package com.minecrafttas.tasmod.virtual;

import java.io.Serializable;

import com.minecrafttas.tasmod.playback.PlaybackSerialiser;

public class VirtualCamera implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -2038332459318568985L;
	private float pitch;
	private float yaw;

	public VirtualCamera() {
		pitch = 0;
		yaw = 0;
	}

	public VirtualCamera(float pitch, float yaw) {
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
	public VirtualCamera clone() {
		return new VirtualCamera(pitch, yaw);
	}
	
	@Override
	public String toString() {
		return PlaybackSerialiser.SectionsV1.CAMERA.getName()+":"+pitch+";"+yaw;
	}
}
