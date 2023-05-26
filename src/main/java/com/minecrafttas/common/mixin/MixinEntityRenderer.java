package com.minecrafttas.common.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import com.minecrafttas.common.events.EventClient.EventCamera;
import com.minecrafttas.common.events.EventClient.EventCamera.CameraData;

import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.GlStateManager;

@Mixin(EntityRenderer.class)
public class MixinEntityRenderer {
	
	private float currentPitch;
	
	@ModifyArg(method = "orientCamera", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GlStateManager;rotate(FFFF)V", ordinal = 8), index = 0)
	public float redirect_orientCameraPitch(float pitch) {
		currentPitch = pitch;
		return 0;
	}
	
	@ModifyArg(method = "orientCamera", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GlStateManager;rotate(FFFF)V", ordinal = 9), index = 0)
	public float redirect_orientCameraYawAnimal(float yawAnimal) {
		return redirectCam(yawAnimal);
	}
	
	@ModifyArg(method = "orientCamera", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GlStateManager;rotate(FFFF)V", ordinal = 10), index = 0)
	public float redirect_orientCameraYaw(float yaw) {
		return redirectCam(yaw);
	}
	
	private float redirectCam(float yaw) {
		CameraData data = EventCamera.fireCameraEvent(new CameraData(currentPitch, yaw));
		GlStateManager.rotate(data.pitch, 1.0f, 0.0f, 0.0f);
		GlStateManager.rotate(data.roll, 0.0f, 0.0f, 1.0f);
		return data.yaw;
	}
}
