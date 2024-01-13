package com.minecrafttas.tasmod.virtual;

import java.io.Serializable;

import com.minecrafttas.tasmod.playback.PlaybackSerialiser;

public class VirtualCameraAngle implements Serializable{
	private float pitch;
	private float yaw;

	public VirtualCameraAngle() {
		pitch = 0;
		yaw = 0;
	}

	public VirtualCameraAngle(float pitch, float yaw) {
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
	public VirtualCameraAngle clone() {
		return new VirtualCameraAngle(pitch, yaw);
	}
	
	@Override
	public String toString() {
		return PlaybackSerialiser.SectionsV1.CAMERA.getName()+":"+pitch+";"+yaw;
	}
}
