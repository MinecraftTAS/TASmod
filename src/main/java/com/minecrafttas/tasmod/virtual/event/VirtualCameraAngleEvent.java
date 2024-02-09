package com.minecrafttas.tasmod.virtual.event;

public class VirtualCameraAngleEvent extends VirtualEvent {
	private float pitchDelta;
	private float yawDelta;
	
	public VirtualCameraAngleEvent(float pitchDelta, float yawDelta) {
		this.pitchDelta = pitchDelta;
		this.yawDelta = yawDelta;
	}
	
	public float getDeltaPitch() {
		return pitchDelta;
	}
	
	public float getDeltaYaw() {
		return yawDelta;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof VirtualCameraAngleEvent) {
			return ((VirtualCameraAngleEvent)obj).pitchDelta == pitchDelta && ((VirtualCameraAngleEvent)obj).yawDelta == yawDelta;
		}
		return super.equals(obj);
	}
}
