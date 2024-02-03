package com.minecrafttas.tasmod.mixin;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.minecrafttas.tasmod.TASmodClient;
import com.minecrafttas.tasmod.handlers.InterpolationHandler;
import com.minecrafttas.tasmod.util.Ducks.SubtickDuck;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.util.math.MathHelper;

@Mixin(EntityRenderer.class)
public class MixinEntityRenderer implements SubtickDuck {

	public double dX = 0;
	public double dY = 0;

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

	@Inject(method = "updateCameraAndRender", at = @At(value = "INVOKE", target = "Lnet/minecraft/profiler/Profiler;startSection(Ljava/lang/String;)V", ordinal = 0, shift = Shift.AFTER))
	public void injectAtStartSection(float partialTicks, long nanoTime, CallbackInfo ci) {
		// Calculate sensitivity
		float f = this.mc.gameSettings.mouseSensitivity * 0.6F + 0.2F;
		float f1 = f * f * f * 8.0F;

		// No Gui
		if (this.mc.currentScreen == null) {
			mc.mouseHelper.mouseXYChange();
			mc.getTutorial().handleMouse(mc.mouseHelper);
			dX += mc.mouseHelper.deltaX;
			dY += mc.mouseHelper.deltaY;
		} else {
			// In the gui
			dX = 0;
			dY = 0;
		}
		if (TASmodClient.controller.isPlayingback()) {
			dX = 0;
			dY = 0;
		} else {
			// Comment this out to disable interpolation, also comment out @SubscribeEvent
			// in InterpolationEvents
			if (this.mc.currentScreen == null) {
				InterpolationHandler.rotationYaw = ((float) ((double) InterpolationHandler.rotationYaw + (double) mc.mouseHelper.deltaX * f1 * 0.15D));
				InterpolationHandler.rotationPitch = (float) ((double) InterpolationHandler.rotationPitch - (double) mc.mouseHelper.deltaY * f1 * 0.15D);
				InterpolationHandler.rotationPitch = MathHelper.clamp(InterpolationHandler.rotationPitch, -90.0F, 90.0F);
			}
		}
	}

	@Redirect(at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;inGameHasFocus:Z", opcode = Opcodes.GETFIELD, ordinal = 1), method = "updateCameraAndRender")
	public boolean stopVanilla(Minecraft mc) {
		if (TASmodClient.tickratechanger.ticksPerSecond != 0) {
			return false;
		} else {
			return mc.inGameHasFocus;
		}
	}

	@Override
	public void runSubtick(float partialTicks) {
		boolean flag = Display.isActive();
		if (flag && Minecraft.IS_RUNNING_ON_MAC && mc.inGameHasFocus && !Mouse.isInsideWindow()) {
			Mouse.setGrabbed(false);
			Mouse.setCursorPosition(Display.getWidth() / 2, Display.getHeight() / 2 - 20);
			Mouse.setGrabbed(true);
		}

		if (mc.inGameHasFocus && flag) {
			mc.getTutorial().handleMouse(mc.mouseHelper);
			float f = mc.gameSettings.mouseSensitivity * 0.6F + 0.2F;
			float f1 = f * f * f * 8.0F;
			float f2 = (float) dX * f1;
			float f3 = (float) dY * f1;
			int i = 1;

			dX = 0;
			dY = 0;

			if (mc.gameSettings.invertMouse) {
				i = -1;
			}

			if (mc.gameSettings.smoothCamera) {
				smoothCamYaw += f2;
				smoothCamPitch += f3;
				float f4 = partialTicks - smoothCamPartialTicks;
				smoothCamPartialTicks = partialTicks;
				f2 = smoothCamFilterX * f4;
				f3 = smoothCamFilterY * f4;
				mc.player.turn(f2, f3 * (float) i);
			} else {
				smoothCamYaw = 0.0F;
				smoothCamPitch = 0.0F;
				mc.player.turn(f2, f3 * (float) i);
			}
			TASmodClient.virtual.CAMERA_ANGLE.updateCameraAngle(mc.player.rotationPitch, mc.player.rotationYaw);
			mc.player.rotationPitch = TASmodClient.virtual.CAMERA_ANGLE.getPitch();
			mc.player.rotationYaw = TASmodClient.virtual.CAMERA_ANGLE.getYaw();
			InterpolationHandler.rotationPitch = mc.player.rotationPitch;
			InterpolationHandler.rotationYaw = 180f + mc.player.rotationYaw;
		}
	}
}
