package com.minecrafttas.tasmod.mixin.playbackhooks;

import org.apache.commons.lang3.tuple.Triple;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalFloatRef;
import com.minecrafttas.tasmod.TASmodClient;
import com.minecrafttas.tasmod.util.Ducks.SubtickDuck;
import com.minecrafttas.tasmod.virtual.VirtualInput;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.math.MathHelper;

/**
 * Redirects the camera to use {@link VirtualInput.VirtualCameraAngleInput}.<br>
 * Also conforms the camera to 20tps as
 */
@Mixin(EntityRenderer.class)
public class MixinEntityRenderer implements SubtickDuck {

    @Shadow
    private Minecraft mc;

    
    @ModifyArgs(method = "updateCameraAndRender", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/entity/EntityPlayerSP;turn(FF)V"))
    public void playback_injectAfterTurn(Args arguments) {
    	double pitchDelta = (double)(float) arguments.get(1);
    	double yawDelta = (double)(float) arguments.get(0);
    	
    	pitchDelta *=0.15D;
    	yawDelta *= 0.15D;
    	
    	float pitch = (float) ((double)mc.player.rotationPitch - pitchDelta);
    	pitch = MathHelper.clamp(pitch, -90.0F, 90.0F);
    	
    	float yaw = (float) ((double)mc.player.rotationYaw + yawDelta);
    	
    	TASmodClient.virtual.CAMERA_ANGLE.updateNextCameraAngle(pitch, yaw);
    	
    	arguments.set(0, 0f);
    	arguments.set(1, 0f);
    }
    
    @Override
    public void runUpdate() {
    	TASmodClient.virtual.CAMERA_ANGLE.nextCameraTick();
    }
    
	@ModifyArg(method = "orientCamera", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GlStateManager;rotate(FFFF)V", ordinal = 8), index = 0)
	public float redirect_orientCameraPitch(float pitch, @Share("pitch") LocalFloatRef sharedPitch) {
		sharedPitch.set(pitch);
		return 0;
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
