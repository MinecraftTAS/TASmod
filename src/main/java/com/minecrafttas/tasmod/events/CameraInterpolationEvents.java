package com.minecrafttas.tasmod.events;

import com.minecrafttas.tasmod.TASmodClient;
import com.minecrafttas.tasmod.mixin.accessors.AccessorRunStuff;
import com.minecrafttas.tasmod.playback.PlaybackController.TickInputContainer;
import com.minecrafttas.tasmod.playback.controlbytes.ControlByteHandler;

import net.minecraft.client.Minecraft;
import net.minecraft.util.math.MathHelper;

public class CameraInterpolationEvents {
	
//	public static float rotationPitch = 0f;
//	public static float rotationYaw = 0f;
//	public void inter(CameraSetup ev) {
//		if (ClientProxy.virtual.getContainer().isPlayingback() && ControlByteHandler.shouldInterpolate) {
//			TickInputContainer input = ClientProxy.virtual.getContainer().get(ClientProxy.virtual.getContainer().index());
//			if (input == null) return;
//			float nextPitch = input.getSubticks().getPitch();
//			float nextYaw = input.getSubticks().getYaw();
//			ev.setPitch((float) MathHelper.clampedLerp(rotationPitch, nextPitch, ((AccessorRunStuff) Minecraft.getMinecraft()).timer().renderPartialTicks));
//			ev.setYaw((float) MathHelper.clampedLerp(rotationYaw, nextYaw+180, ((AccessorRunStuff) Minecraft.getMinecraft()).timer().renderPartialTicks));
//		} else {
//			ev.setPitch(rotationPitch);
//			ev.setYaw(rotationYaw);
//		}
//	}
	
}