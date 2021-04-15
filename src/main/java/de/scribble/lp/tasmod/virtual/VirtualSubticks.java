package de.scribble.lp.tasmod.virtual;

public class VirtualSubticks {
	private float pitch;
	private float yaw;
	
	public VirtualSubticks() {
		pitch=0;
		yaw=0;
	}
	public VirtualSubticks(float pitch, float yaw) {
		this.pitch=pitch;
		this.yaw=yaw;
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
}
