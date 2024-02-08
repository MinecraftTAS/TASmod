package com.minecrafttas.tasmod.virtual;

import java.io.Serializable;

public class VirtualCameraAngle implements Serializable {
	private Float pitch;
	private Float yaw;


	public VirtualCameraAngle() {
		this(null, null);
	}

	public VirtualCameraAngle(Float pitch, Float yaw) {
		this.pitch = pitch;
		this.yaw = yaw;
	}

	public void update(Float pitch, Float yaw) {
		this.pitch = pitch;
		this.yaw = yaw;
	}
	
	public Float getPitch() {
		return pitch;
	}

	public Float getYaw() {
		return yaw;
	}

	@Override
	public VirtualCameraAngle clone() {
		return new VirtualCameraAngle(pitch, yaw);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof VirtualCameraAngle) {
			VirtualCameraAngle angle = (VirtualCameraAngle) obj;
			return pitch == angle.pitch && yaw == angle.yaw;
		}
		return super.equals(obj);
	}

	@Override
	public String toString() {
		return String.format("%s;%s", pitch, yaw);
	}
}
