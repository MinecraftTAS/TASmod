package com.minecrafttas.tasmod.virtual;

import java.io.Serializable;
import java.util.List;
import java.util.Queue;

public class VirtualCameraAngle2 implements Serializable {
	private Float pitch;
	private Float yaw;

	private final List<VirtualCameraAngle2> subtickList;

	public VirtualCameraAngle2() {
		this(null, null);
	}

	public VirtualCameraAngle2(Float pitch, Float yaw) {
		this(pitch, yaw, null);
	}

	public VirtualCameraAngle2(Float pitch, Float yaw, List<VirtualCameraAngle2> subtickList) {
		this.pitch = pitch;
		this.yaw = yaw;
		this.subtickList = subtickList;
	}

	public void update(Float pitch, Float yaw) {
		this.pitch = pitch;
		this.yaw = yaw;
		if(isParent()) {
			subtickList.add(clone());
		}
	}
	
	public Float getPitch() {
		return pitch;
	}

	public Float getYaw() {
		return yaw;
	}

	public boolean isParent() {
		return subtickList != null;
	}
	
	public void getCameraAngleSubticks(Queue<VirtualCameraAngle2> reference) {
		if(isParent()) {
			reference.addAll(subtickList);
		}
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
