package com.minecrafttas.tasmod.virtual;

import java.io.Serializable;

public class VirtualCameraAngle2 implements Serializable {
	private Float pitch;
	private Float yaw;


	public VirtualCameraAngle2() {
		this(null, null);
	}

	public VirtualCameraAngle2(Float pitch, Float yaw) {
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
	public VirtualCameraAngle2 clone() {
		return new VirtualCameraAngle2(pitch, yaw);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof VirtualCameraAngle2) {
			VirtualCameraAngle2 angle = (VirtualCameraAngle2) obj;
			return pitch == angle.pitch && yaw == angle.yaw;
		}
		return super.equals(obj);
	}

	@Override
	public String toString() {
		return String.format("%s;%s", pitch, yaw);
	}
}
