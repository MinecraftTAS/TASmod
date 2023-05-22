package com.minecrafttas.tasmod.handlers;

import com.minecrafttas.common.events.client.EventCamera;
import com.minecrafttas.tasmod.TASmodClient;
import com.minecrafttas.tasmod.mixin.accessors.AccessorRunStuff;
import com.minecrafttas.tasmod.playback.PlaybackController.TickInputContainer;
import com.minecrafttas.tasmod.playback.controlbytes.ControlByteHandler;

import net.minecraft.client.Minecraft;
import net.minecraft.util.math.MathHelper;

/**
 * Adds interpolation to the camera
 * @author Pancake
 *
 */
public class InterpolationHandler implements EventCamera{
	
	public static float rotationPitch = 0f;
	public static float rotationYaw = 0f;
	
	@Override
	public CameraData onCameraEvent(CameraData dataIn) {
		if (TASmodClient.virtual.getContainer().isPlayingback() && ControlByteHandler.shouldInterpolate) {
			TickInputContainer input = TASmodClient.virtual.getContainer().get();
			if (input == null) return dataIn;
			float nextPitch = input.getSubticks().getPitch();
			float nextYaw = input.getSubticks().getYaw();
			dataIn.pitch = (float) MathHelper.clampedLerp(rotationPitch, nextPitch, ((AccessorRunStuff) Minecraft.getMinecraft()).timer().renderPartialTicks);
			dataIn.yaw = (float) MathHelper.clampedLerp(rotationYaw, nextYaw+180, ((AccessorRunStuff) Minecraft.getMinecraft()).timer().renderPartialTicks);
		} else {
			dataIn.pitch = rotationPitch;
			dataIn.yaw = rotationYaw;
		}
		return dataIn;
	}
}
