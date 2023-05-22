package com.minecrafttas.common.events.client;

import com.minecrafttas.common.events.EventBase;
import com.minecrafttas.common.events.EventListener;

public interface EventCamera extends EventBase{
	
	public CameraData onCameraEvent(CameraData dataIn);
	
	public static CameraData fireCameraEvent(CameraData dataIn) {
		for (EventBase eventListener : EventListener.getEventListeners()) {
			if(eventListener instanceof EventCamera) {
				EventCamera event = (EventCamera) eventListener;
				CameraData data = event.onCameraEvent(dataIn);
				if(data.equals(dataIn)) {
					return data;
				}
			}
		}
		return dataIn;
	}
	
	public static class CameraData{
		public float pitch;
		public float yaw;
		public float roll;
		
		public CameraData(float pitch, float yaw) {
			this(pitch, yaw, 0f);
		}
		
		public CameraData(float pitch, float yaw, float roll) {
			this.pitch = pitch;
			this.yaw = yaw;
			this.roll = roll;
		}
		
		@Override
		public boolean equals(Object obj) {
			if(obj instanceof CameraData) {
				CameraData b = (CameraData) obj;
				return b.pitch == pitch && b.yaw == yaw;
			}
			return super.equals(obj);
		}
	}
}
