package de.scribble.lp.tasmod.virtual;

public class VirtualSubticks {
	private int tick;
	private float pitch;
	private float yaw;
	
	public VirtualSubticks(int tick, float pitch, float yaw) {
		this.tick=tick;
		this.pitch=pitch;
		this.yaw=yaw;
	}
	public int getTick() {
		return tick;
	}
	public float getPitch() {
		return pitch;
	}
	public float getYaw() {
		return yaw;
	}
}
