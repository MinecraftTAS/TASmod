package de.scribble.lp.tasmod.virtual;

public class VirtualSubtickEvent {
	private int pitchDelta;
	private int yawDelta;

	public VirtualSubtickEvent(int pitch, int yaw) {
		super();
		this.pitchDelta = pitch;
		this.yawDelta = yaw;
	}

	public int getPitchDelta() {
		return pitchDelta;
	}

	public int getYawDelta() {
		return yawDelta;
	}
	
	@Override
	public String toString() {
		return pitchDelta+", "+yawDelta;
	}
}
