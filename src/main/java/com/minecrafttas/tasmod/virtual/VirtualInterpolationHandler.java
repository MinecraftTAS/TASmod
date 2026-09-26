package com.minecrafttas.tasmod.virtual;

import java.util.ArrayList;
import java.util.List;

import com.minecrafttas.tasmod.TASmodClient;
import com.minecrafttas.tasmod.events.EventVirtualInput;
import com.minecrafttas.tasmod.playback.PlaybackControllerClient;
import com.minecrafttas.tasmod.util.Ducks.ScreenDuck;
import com.minecrafttas.tasmod.util.PointerNormalizer;

import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;

public class VirtualInterpolationHandler implements EventVirtualInput.EventVirtualMouseTick, EventVirtualInput.EventVirtualCameraAngleTick {

	/**
	 * Copy of the {@link PlaybackControllerClient#nextPlaybackMouse}
	 */
	private VirtualMouse nextMouse = new VirtualMouse();
	/**
	 * Copy of the {@link VirtualInput#CAMERA_ANGLE#nextCameraAngle}
	 */
	private VirtualCameraAngle nextCameraAngle = new VirtualCameraAngle();

	/**
	 * States of the {@link #nextMouse} made during the tick.<br>
	 * Is updated in {@link #onVirtualMouseTick()}
	 */
	private final List<VirtualMouse> mousePointerStates = new ArrayList<>();
	/**
	 * States of the {@link #nextCameraAngle} made during the tick.<br>
	 * Is updated in {@link #onVirtualCameraTick()}
	 */
	private final List<VirtualCameraAngle> cameraAngleStates = new ArrayList<>();

	@Override
	public VirtualMouse onVirtualMouseTick(VirtualMouse vmouse) {
		this.nextMouse = vmouse;
		mousePointerStates.clear();
		TASmodClient.controller.getNextMouse().getStates(mousePointerStates);
		return null;
	}

	@Override
	public VirtualCameraAngle onVirtualCameraTick(VirtualCameraAngle vcamera) {
		this.nextCameraAngle = vcamera;
		cameraAngleStates.clear();
		nextCameraAngle.getStates(cameraAngleStates);
		return null;
	}

	/**
	 * Interpolates the mouse cursor inbetween ticks based on the data from the next tick
	 * 
	 * @param partialTick The partial ticks used for interpolating
	 * @param enable If the interpolation should be enabled. Basically if {@link PlaybackControllerClient#isPlayingback()}
	 * @return A {@link MouseInterpolation} object with x and y coordinates
	 */
	public MouseInterpolation getInterpolatedMouseCursor(float partialTick, boolean enable) {

		int interpolatedPointerX = nextMouse.getCursorX();
		int interpolatedPointerY = nextMouse.getCursorY();

		if (enable && !mousePointerStates.isEmpty()) {
			partialTick = dynamicallyRound(partialTick, TASmodClient.tickratechanger.ticksPerSecond);
			int index = (int) Mth.clampedLerp(0, mousePointerStates.size() - 1, partialTick); // Get interpolate index
			VirtualMouse interpolatedCamera = mousePointerStates.get(index);

			interpolatedPointerX = interpolatedCamera.getCursorX();
			interpolatedPointerY = interpolatedCamera.getCursorY();

		}
		// Screen scaling API changed in 26.3 - skip for now
		// Minecraft mc = Minecraft.getInstance();
		// ScreenDuck gui = (ScreenDuck) mc.screen;
		// 
		// if (gui != null && !(mc.screen instanceof SubtickGuiScreen)) {
		// 	interpolatedPointerX = gui.rescaleX(PointerNormalizer.reapplyScalingX(interpolatedPointerX));
		// 	interpolatedPointerY = gui.rescaleY(PointerNormalizer.reapplyScalingY(interpolatedPointerY));
		// }

		return new MouseInterpolation(interpolatedPointerX, interpolatedPointerY);
	}

	/**
	 * <p>Rounds the partial tick to 1 depending on the tickrate.
	 * 
	 * <p>To correctly play back the mouse cursor, the partial ticks have to reach 1 at some point.<br>
	 * However this is not the case in higher tickrates.<br>
	 * The solution is to round the partial ticks to 1 after a certain threshold.
	 * 
	 * <p>The higher the tps, the lower the threshold for rounding.
	 * 
	 * @param partialTick The partial ticks to round
	 * @param tps The ticks per second used for setting the threshold
	 * @return The rounded partial ticks
	 */
	private float dynamicallyRound(float partialTick, float tps) {
		float percent = tps / 100;
		if (partialTick > 1 - percent)
			partialTick = 1;
		return partialTick;
	}

	/**
	 * Gets the interpolated coordinates of the camera angle
	 * 
	 * @param partialTick The partial ticks of the timer
	 * @param pitch The original pitch of the camera
	 * @param yaw The original yaw of the camera
	 * @param enable Whether the custom interpolation is enabled. Enabled during playback.
	 * @return A triple of pitch, yaw and roll, as left, middle and right respectively 
	 */
	public CameraInterpolation getInterpolatedState(float partialTick, float pitch, float yaw, boolean enable) {

		float interpolatedPitch = nextCameraAngle.getPitch() == null ? pitch : nextCameraAngle.getPitch();
		float interpolatedYaw = nextCameraAngle.getYaw() == null ? yaw : nextCameraAngle.getYaw() + 180;

		if (enable && !cameraAngleStates.isEmpty()) {
			int index = (int) Mth.clampedLerp(0, cameraAngleStates.size() - 1, partialTick); // Get interpolate index

			VirtualCameraAngle interpolatedCamera = cameraAngleStates.get(index);

			interpolatedPitch = interpolatedCamera.getPitch() == null ? 0 : interpolatedCamera.getPitch();
			interpolatedYaw = interpolatedCamera.getYaw() == null ? 0 : interpolatedCamera.getYaw() + 180;

		}
		return new CameraInterpolation(interpolatedPitch, interpolatedYaw);
	}

	public static class MouseInterpolation {
		final Integer x;
		final Integer y;

		public MouseInterpolation(Integer x, Integer y) {
			this.x = x;
			this.y = y;
		}

		public Integer getX() {
			return x;
		}

		public Integer getY() {
			return y;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof MouseInterpolation) {
				MouseInterpolation other = (MouseInterpolation) obj;
				return this.x.equals(other.x) && this.y.equals(other.y);
			}
			return super.equals(obj);
		}
	}

	public static class CameraInterpolation {
		final Float pitch;
		final Float yaw;

		public CameraInterpolation(Float pitch, Float yaw) {
			this.pitch = pitch;
			this.yaw = yaw;
		}

		public Float getPitch() {
			return pitch;
		}

		public Float getYaw() {
			return yaw;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof CameraInterpolation) {
				CameraInterpolation other = (CameraInterpolation) obj;
				return this.pitch.equals(other.pitch) && this.yaw.equals(other.yaw);
			}
			return super.equals(obj);
		}
	}
}

