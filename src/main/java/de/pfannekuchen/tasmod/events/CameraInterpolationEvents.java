package de.pfannekuchen.tasmod.events;

import net.minecraftforge.client.event.EntityViewRenderEvent.CameraSetup;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class CameraInterpolationEvents {
	
	public static float rotationPitch = 0f;
	public static float rotationYaw = 0f;
	
	public void inter(CameraSetup ev) {
		ev.setPitch(rotationPitch);
		ev.setYaw(rotationYaw);
	}
	
}
