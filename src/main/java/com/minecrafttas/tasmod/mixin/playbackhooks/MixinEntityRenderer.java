package com.minecrafttas.tasmod.mixin.playbackhooks;

import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalFloatRef;
import com.minecrafttas.tasmod.TASmodClient;
import com.minecrafttas.tasmod.util.Ducks.SubtickDuck;
import com.minecrafttas.tasmod.virtual.VirtualInput;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.math.MathHelper;
import org.apache.commons.lang3.tuple.Triple;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Redirects the camera to use {@link VirtualInput.VirtualCameraAngleInput}.<br>
 * Also conforms the camera to 20tps as
 */
@Mixin(EntityRenderer.class)
public class MixinEntityRenderer implements SubtickDuck {

	@Shadow
	private Minecraft mc;
	@Shadow
	private float smoothCamYaw;
	@Shadow
	private float smoothCamPitch;
	@Shadow
	private float smoothCamPartialTicks;
	@Shadow
	private float smoothCamFilterX;
	@Shadow
	private float smoothCamFilterY;

	@Inject(method = "updateCameraAndRender", at = @At(value = "INVOKE", target = "Lnet/minecraft/profiler/Profiler;startSection(Ljava/lang/String;)V", ordinal = 0, shift = At.Shift.AFTER))
	public void playback_injectAtStartSection(float partialTicks, long nanoTime, CallbackInfo ci) {
		// Calculate sensitivity
		float f = this.mc.gameSettings.mouseSensitivity * 0.6F + 0.2F;
		float f1 = f * f * f * 8.0F;

		if (this.mc.currentScreen == null) { // No Gui
			mc.mouseHelper.mouseXYChange();
			mc.getTutorial().handleMouse(mc.mouseHelper);
			TASmodClient.virtual.CAMERA_ANGLE.updateNextCameraAngle((float) -(mc.mouseHelper.deltaY * f1 * 0.15D), (float) (mc.mouseHelper.deltaX * f1 * 0.15D));
		}
	}

	@Redirect(method = "updateCameraAndRender", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/EntityPlayerSP;turn(FF)V"))
	public void playback_stopVanilla(EntityPlayerSP player, float deltaYaw, float deltaPitch){
		if(TASmodClient.tickratechanger.ticksPerSecond == 0){
			player.turn(deltaYaw, deltaPitch);
		}
	}

	@Override
	public void runUpdate(float partialTicks) {
			if(mc.player == null){
				return;
			}
			float prevPitch = mc.player.rotationPitch;
			float prevYaw = mc.player.rotationYaw;
			TASmodClient.virtual.CAMERA_ANGLE.nextCameraTick();
			mc.player.rotationPitch = TASmodClient.virtual.CAMERA_ANGLE.getCurrentPitch();
			mc.player.rotationYaw = TASmodClient.virtual.CAMERA_ANGLE.getCurrentYaw();

			mc.player.prevRotationPitch = prevPitch;
			mc.player.prevRotationYaw = prevYaw;
	}
    
	@ModifyArg(method = "orientCamera", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GlStateManager;rotate(FFFF)V", ordinal = 8), index = 0)
	public float redirect_orientCameraPitch(float pitch, @Share("pitch") LocalFloatRef sharedPitch) {
		sharedPitch.set(pitch);
		return 0f;
	}
	
	@ModifyArg(method = "orientCamera", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GlStateManager;rotate(FFFF)V", ordinal = 9), index = 0)
	public float redirect_orientCameraYawAnimal(float yawAnimal, @Share("pitch") LocalFloatRef sharedPitch) {
		return redirectCam(yawAnimal, sharedPitch.get());
	}
	
	@ModifyArg(method = "orientCamera", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GlStateManager;rotate(FFFF)V", ordinal = 10), index = 0)
	public float redirect_orientCameraYaw(float yaw, @Share("pitch") LocalFloatRef sharedPitch) {
		return redirectCam(yaw, sharedPitch.get());
	}
	
	private float redirectCam(float yaw, float pitch) {
		Triple<Float, Float, Float> interpolated = TASmodClient.virtual.CAMERA_ANGLE.getInterpolatedState(Minecraft.getMinecraft().timer.renderPartialTicks, pitch, yaw, TASmodClient.controller.isPlayingback());
		GlStateManager.rotate(interpolated.getLeft(), 1.0f, 0.0f, 0.0f);
		GlStateManager.rotate(interpolated.getRight(), 0.0f, 0.0f, 1.0f);
		return interpolated.getMiddle();
	}
}
