package com.minecrafttas.tasmod.virtual;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import net.minecraft.util.Mth;

/**
 * Stores the values of the camera angle of the player in a given timeframe.<br>
 * <br>
 * Similar to {@link VirtualKeyboard} and {@link VirtualMouse} with the difference,<br>
 * that no difference calculation is applied and only the absolute camera coordinates are used.<br>
 * This makes the playback desync proof to different mouse sensitivity across PCs.<br>
 * 
 */
public class VirtualCameraAngle extends Subtickable<VirtualCameraAngle> implements Serializable {
	/**
	 * Controls the up/down coordinate of the camera. Clamped between -90 and +90
	 */
	private Float pitch;
	/**
	 * Controls the left/right coordinate of the camera. In this case the camera is clamped between -180 and +180
	 */
	private Float yaw;

	/**
	 * Creates an empty camera angle with pitch and yaw = 0
	 */
	public VirtualCameraAngle() {
		this(null, null, new ArrayList<>(), true);
	}

	/**
	 * Creates a subtick camera angle with {@link Subtickable#subtickList} uninitialized
	 * @param pitch {@link #pitch}
	 * @param yaw {@link #yaw}
	 */
	public VirtualCameraAngle(Float pitch, Float yaw) {
		this(pitch, yaw, null);
	}

	/**
	 * Creates a parent camera angle
	 * @param pitch {@link #pitch}
	 * @param yaw {@link #yaw}
	 * @param ignoreFirstUpdate {@link Subtickable#ignoreFirstUpdate}
	 */
	public VirtualCameraAngle(Float pitch, Float yaw, boolean ignoreFirstUpdate) {
		this(pitch, yaw, new ArrayList<>(), ignoreFirstUpdate);
	}

	/**
	 * Creates a camera angle with existing values
	 * @param pitch {@link #pitch}
	 * @param yaw {@link #yaw}
	 * @param subtickList {@link Subtickable#subtickList}
	 */
	public VirtualCameraAngle(Float pitch, Float yaw, List<VirtualCameraAngle> subtickList) {
		this(pitch, yaw, subtickList, false);
	}

	/**
	 * Creates a camera angle with initialized values
	 * @param pitch {@link VirtualCameraAngle#pitch}
	 * @param yaw {@link VirtualCameraAngle#yaw}
	 * @param subtickList {@link Subtickable#subtickList}
	 * @param ignoreFirstUpdate {@link Subtickable#ignoreFirstUpdate}
	 */
	public VirtualCameraAngle(Float pitch, Float yaw, List<VirtualCameraAngle> subtickList, boolean ignoreFirstUpdate) {
		super(subtickList, ignoreFirstUpdate);
		this.pitch = pitch;
		this.yaw = yaw;
	}

	/**
	 * Updates the camera angle.
	 * @param pitchDelta The difference between absolute coordinates of the pitch, is added to {@link VirtualCameraAngle#pitch}
	 * @param yawDelta The difference between absolute coordinates of the yaw, is added to {@link VirtualCameraAngle#yaw}
	 */
	public void updateFromEvent(float pitchDelta, float yawDelta) {
		updateFromEvent(pitchDelta, yawDelta, true);
	}

	/**
	 * Updates the camera angle.
	 * @param pitchDelta The difference between absolute coordinates of the pitch, is added to {@link VirtualCameraAngle#pitch}
	 * @param yawDelta The difference between absolute coordinates of the yaw, is added to {@link VirtualCameraAngle#yaw}
	 * @param updateSubtick If the previous camera should be added to {@link Subtickable#subtickList}
	 */
	public void updateFromEvent(float pitchDelta, float yawDelta, boolean updateSubtick) {
		if (pitch == null || yaw == null) {
			return;
		}
		createSubtick(updateSubtick);
		this.pitch = Mth.clamp(this.pitch + pitchDelta, -90F, 90F);
		this.yaw += yawDelta;
	}

	public void updateFromState(Float pitch, Float yaw) {
		if (this.pitch != null && this.yaw != null) {
			createSubtick(true);
		}
		if (pitch != null) {
			pitch = Mth.clamp(pitch, -90F, 90F);
		}
		this.pitch = pitch;
		this.yaw = yaw;
	}

	public void createSubtick(boolean updateSubtick) {
		if (isParent() && !ignoreFirstUpdate() && updateSubtick) {
			addSubtick(shallowClone());
		}
	}

	/**
	 * Setting the absolute camera coordinates directly
	 * @param pitch {@link #pitch}
	 * @param yaw {@link #yaw}
	 */
	public void set(float pitch, float yaw) {
		this.pitch = pitch;
		this.yaw = yaw;
	}

	/**
	 * A list of all camera states in this VirtualCameraAngle.
	 * It consists of: {@link Subtickable#subtickList} + this
	 * @param reference A list of VirtualCameraAngles with the newest being the current camera angle
	 */
	public void getStates(List<VirtualCameraAngle> reference) {
		if (isParent()) {
			reference.addAll(subtickList);
			reference.add(this.shallowClone());		// Fixes a crash where "this" can become null in the reference.
		}
	}

	/**
	 * Moves the data from another camera angle into this camera without creating a new object.
	 * @param camera The camera to copy from
	 */
	public void moveFrom(VirtualCameraAngle camera) {
		if (camera == null)
			return;
		this.pitch = camera.pitch;
		this.yaw = camera.yaw;
		this.subtickList.clear();
		this.subtickList.addAll(camera.subtickList);
		camera.subtickList.clear();
	}

	/**
	 * Copies the data from another camera angle into this camera without creating a new object.
	 * @param camera The camera to copy from
	 */
	public void deepCopyFrom(VirtualCameraAngle camera) {
		if (camera == null || !camera.isParent())
			return;
		this.pitch = camera.pitch;
		this.yaw = camera.yaw;
		this.subtickList.clear();
		this.subtickList.addAll(camera.subtickList);
	}

	/**
	 * Sets {@link #pitch} and {@link #yaw} to null
	 */
	@Override
	public void clear() {
		this.pitch = null;
		this.yaw = null;
		super.clear();
	}

	/**
	 * Creates a clone of this object as a subtick
	 */
	public VirtualCameraAngle shallowClone() {
		return new VirtualCameraAngle(pitch, yaw);
	}

	@Override
	public VirtualCameraAngle clone() {
		return new VirtualCameraAngle(pitch, yaw, new ArrayList<>(subtickList), isIgnoreFirstUpdate());
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof VirtualCameraAngle) {
			VirtualCameraAngle angle = (VirtualCameraAngle) obj;
			if (pitch == null && angle.pitch != null) {
				return false;
			}
			if (yaw == null && angle.yaw != null) {
				return false;
			}
			if (pitch != null && !pitch.equals(angle.pitch)) {
				return false;
			}
			if (yaw != null && !yaw.equals(angle.yaw))
				return false;
			return true;
		}
		return super.equals(obj);
	}

	@Override
	public String toString() {
		if (isParent()) {
			return getAll().stream().map(VirtualCameraAngle::toString2).collect(Collectors.joining("\n"));
		} else {
			return toString2();
		}
	}

	public String toString2() {
		return String.format("%s;%s", yaw, pitch);
	}

	/**
	 * @return {@link #pitch}
	 */
	public Float getPitch() {
		return pitch;
	}

	/**
	 * @return {@link #yaw}
	 */
	public Float getYaw() {
		return yaw;
	}

	@Override
	public boolean isEmpty() {
		return super.isEmpty() && (pitch == null || pitch == 0) && (yaw == null || yaw == 0);
	}
}

