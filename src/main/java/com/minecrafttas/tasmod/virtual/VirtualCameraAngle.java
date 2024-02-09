package com.minecrafttas.tasmod.virtual;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.minecrafttas.tasmod.virtual.event.VirtualCameraAngleEvent;

public class VirtualCameraAngle extends Subtickable<VirtualCameraAngle> implements Serializable {
	private float pitch;
	private float yaw;
	
	public VirtualCameraAngle() {
		this(0, 0, new ArrayList<>(), true);
	}
	
	public VirtualCameraAngle(float pitch, float yaw) {
		this(pitch, yaw, null);
	}
	
	public VirtualCameraAngle(float pitch, float yaw, List<VirtualCameraAngle> subtickList) {
		this(pitch, yaw, subtickList, false);
	}

	public VirtualCameraAngle(float pitch, float yaw, List<VirtualCameraAngle> subtickList, boolean ignoreFirstUpdate) {
		super(subtickList, ignoreFirstUpdate);
		this.pitch = pitch;
		this.yaw = yaw;
	}

	public void update(float pitchDelta, float yawDelta) {
		if(isParent() && !ignoreFirstUpdate()) {
			addSubtick(clone());
		}
		this.pitch += pitchDelta;
		this.yaw += yawDelta;
	}
	
	public void getStates(List<VirtualCameraAngle> reference) {
		if (isParent()) {
			reference.addAll(subtickList);
			reference.add(this);
		}
	}
	
	public VirtualCameraAngleEvent getCollected(VirtualCameraAngle nextCameraAngle) {
		float pitchDelta = pitch;
		float yawDelta = yaw;
		for(VirtualCameraAngle subtick : nextCameraAngle.getAll()) {
			pitchDelta+=subtick.pitch;
			yawDelta+=subtick.yaw;
		}
		return new VirtualCameraAngleEvent(pitchDelta, yawDelta);
	}
	
	public void copyFrom(VirtualCameraAngle camera) {
		this.pitch = camera.pitch;
		this.yaw = camera.yaw;
		this.subtickList.clear();
		this.subtickList.addAll(camera.subtickList);
		camera.subtickList.clear();
		camera.resetFirstUpdate();
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
	
	public float getPitch() {
		return pitch;
	}

	public float getYaw() {
		return yaw;
	}
}
