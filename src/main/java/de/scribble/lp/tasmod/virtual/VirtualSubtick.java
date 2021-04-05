package de.scribble.lp.tasmod.virtual;

import java.util.ArrayList;
import java.util.List;

public class VirtualSubtick {
	private int pitch;
	
	private int yaw;
	
	public VirtualSubtick() {
		pitch=0;
		yaw=0;
	}
	
	public VirtualSubtick(int pitch, int yaw) {
		this.pitch=pitch;
		
		this.yaw=yaw;
	}
	
	public void set(int pitchDelta, int yawDelta) {
		this.pitch=this.pitch+pitchDelta;
		
		this.yaw=this.yaw+yawDelta;
	}
	
	public List<VirtualSubtickEvent> getDifference(VirtualSubtick subticksToCompare){
		List<VirtualSubtickEvent> out= new ArrayList<VirtualSubtickEvent>();
		
		if(pitch!=subticksToCompare.pitch && yaw != subticksToCompare.yaw) {
			out.add(new VirtualSubtickEvent(subticksToCompare.pitch-pitch, subticksToCompare.yaw-yaw));
		}
		return out;
	}
	
	@Override
	protected VirtualSubtick clone() throws CloneNotSupportedException {
		return new VirtualSubtick(pitch, yaw);
	}
}
