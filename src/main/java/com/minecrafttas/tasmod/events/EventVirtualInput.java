package com.minecrafttas.tasmod.events;

import com.minecrafttas.tasmod.mctcommon.events.EventListenerRegistry.EventBase;
import com.minecrafttas.tasmod.virtual.VirtualCameraAngle;
import com.minecrafttas.tasmod.virtual.VirtualInput.VirtualCameraAngleInput;
import com.minecrafttas.tasmod.virtual.VirtualInput.VirtualKeyboardInput;
import com.minecrafttas.tasmod.virtual.VirtualInput.VirtualMouseInput;
import com.minecrafttas.tasmod.virtual.VirtualKeyboard;
import com.minecrafttas.tasmod.virtual.VirtualMouse;
import com.minecrafttas.tasmod.virtual.event.VirtualKeyboardEvent;
import com.minecrafttas.tasmod.virtual.event.VirtualMouseEvent;

public interface EventVirtualInput {

	/**
	 * Fired when the {@link VirtualKeyboardInput#currentKeyboard} is updated
	 * 
	 * @see VirtualKeyboardInput#nextKeyboardTick()
	 */
	@FunctionalInterface
	interface EventVirtualKeyboardTick extends EventBase {

		/**
		 * Fired when the {@link VirtualKeyboard} ticks
		 * 
		 * @param vkeyboard The {@link VirtualKeyboardInput#nextKeyboard} that is
		 *                  supposed to be pressed
		 * @returns The redirected keyboard
		 * @see VirtualKeyboardInput#nextKeyboardTick()
		 */
		public VirtualKeyboard onVirtualKeyboardTick(VirtualKeyboard vkeyboard);
	}

	/**
	 * Fired when the {@link VirtualMouseInput#currentMouse} is updated
	 * 
	 * @see VirtualMouseInput#nextMouseTick()
	 */
	@FunctionalInterface
	interface EventVirtualMouseTick extends EventBase {

		/**
		 * Fired when the {@link VirtualMouseInput#currentMouse} is updated
		 * 
		 * @param vmouse The {@link VirtualMouseInput#nextMouse} that is supposed to be
		 *               pressed
		 * @returns The redirected mouse
		 * @see VirtualMouseInput#nextMouseTick()
		 */
		public VirtualMouse onVirtualMouseTick(VirtualMouse vmouse);
	}

	/**
	 * Fired when the {@link VirtualCameraAngleInput#currentCameraAngle} is updated
	 * 
	 * @see VirtualCameraAngleInput#nextCameraTick()
	 */
	@FunctionalInterface
	interface EventVirtualCameraAngleTick extends EventBase {

		/**
		 * Fired when the {@link VirtualCameraAngleInput#currentCameraAngle} is updated
		 * 
		 * @param vcamera The {@link VirtualCameraAngleInput#nextCameraAngle}
		 * @returns The redirected cameraAngle
		 * @see VirtualCameraAngleInput#nextCameraTick()
		 */
		public VirtualCameraAngle onVirtualCameraTick(VirtualCameraAngle vcamera);
	}

	/**
	 * Fired when the {@link VirtualKeyboardInput#currentKeyboardEvent} is updated
	 *
	 * @see VirtualKeyboardInput#nextKeyboardSubtick()
	 */
	@FunctionalInterface
	interface EventVirtualKeyboardSubtick extends EventBase {

		/**
		 * Fired when the {@link VirtualKeyboardInput#currentKeyboardEvent} is updated
		 *
		 * @param event The keyboard event (An input event, not an eventlistener event!)
		 * @see VirtualKeyboardInput#nextKeyboardSubtick()
		 */
		public void onVirtualKeyboardSubtick(VirtualKeyboardEvent event);
	}

	/**
	 * Fired when the {@link VirtualMouseInput#currentMouseEvent} is updated
	 *
	 * @see VirtualMouseInput#nextMouseSubtick()
	 */
	@FunctionalInterface
	interface EventVirtualMouseSubtick extends EventBase {
		/**
		 * Fired when the {@link VirtualMouseInput#currentMouseEvent} is updated
		 *
		 * @param event The keyboard event (An input event, not an eventlistener event!)
		 * @see VirtualMouseInput#nextMouseSubtick()
		 */
		public void onVirtualMouseSubtick(VirtualMouseEvent event);
	}

	// Doesn't exist yet... Maybe in the future?
//	@FunctionalInterface
//	interface EventVirtualCameraAngleSubtick extends EventBase {
//		public void onVirtualCameraSubtick(boolean isPolled);
//	}
}

