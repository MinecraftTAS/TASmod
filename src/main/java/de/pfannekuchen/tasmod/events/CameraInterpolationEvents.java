package de.pfannekuchen.tasmod.events;

import de.pfannekuchen.tasmod.controlbytes.ControlByteHandler;
import de.scribble.lp.tasmod.ClientProxy;
import de.scribble.lp.tasmod.inputcontainer.TickInputContainer;
import de.scribble.lp.tasmod.mixin.accessors.AccessorRunStuff;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.event.EntityViewRenderEvent.CameraSetup;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class CameraInterpolationEvents {
	
	public static float rotationPitch = 0f;
	public static float rotationYaw = 0f;
	@SubscribeEvent
	public void inter(CameraSetup ev) {
		if (ClientProxy.virtual.getContainer().isPlayingback() && ControlByteHandler.shouldInterpolate) {
			TickInputContainer input = ClientProxy.virtual.getContainer().get(ClientProxy.virtual.getContainer().index());
			if (input == null) return;
			float nextPitch = input.getSubticks().getPitch();
			float nextYaw = input.getSubticks().getYaw();
			ev.setPitch((float) MathHelper.clampedLerp(rotationPitch, nextPitch, ((AccessorRunStuff) Minecraft.getMinecraft()).timer().renderPartialTicks));
			ev.setYaw((float) MathHelper.clampedLerp(rotationYaw, nextYaw+180, ((AccessorRunStuff) Minecraft.getMinecraft()).timer().renderPartialTicks));
		} else {
			ev.setPitch(rotationPitch);
			ev.setYaw(rotationYaw);
		}
	}
	
}
