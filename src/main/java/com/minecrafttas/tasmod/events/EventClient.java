package com.minecrafttas.tasmod.events;

import com.minecrafttas.mctcommon.events.EventListenerRegistry.EventBase;
import com.minecrafttas.tasmod.virtual.VirtualCameraAngle;
import com.minecrafttas.tasmod.virtual.VirtualInput.VirtualCameraAngleInput;
import com.minecrafttas.tasmod.virtual.VirtualInput.VirtualKeyboardInput;
import com.minecrafttas.tasmod.virtual.VirtualInput.VirtualMouseInput;
import com.minecrafttas.tasmod.virtual.VirtualKeyboard;
import com.minecrafttas.tasmod.virtual.VirtualMouse;

import net.minecraft.client.Minecraft;

/**
 * TASmod specific events fired on the client side
 *
 * @author Scribble
 */
public interface EventClient {

	/**
	 * Fired when the hotbar is drawn on screen
	 */
	@FunctionalInterface
	public static interface EventDrawHotbar extends EventBase {
		/**
		 * Fired when the hotbar is drawn on screen
		 */
		public void onDrawHotbar();
	}

	/**
	 * Fired at the end of a client tick
	 */
	@FunctionalInterface
	public static interface EventClientTickPost extends EventBase {

		/**
		 * Fired at the end of a client tick
		 */
		public void onClientTickPost(Minecraft mc);
	}

	/**
	 * Fired when the tickrate changes on the client side
	 */
	@FunctionalInterface
	public static interface EventClientTickrateChange extends EventBase {

		/**
		 * Fired at the end of a client tick
		 */
		public void onClientTickrateChange(float tickrate);
	}

	
	/**
	 * Fired when the {@link VirtualKeyboardInput#currentKeyboard} is updated
	 * 
	 * @see VirtualKeyboardInput#nextKeyboardTick()
	 */
	@FunctionalInterface
	public static interface EventVirtualKeyboardTick extends EventBase {
		
		/**
		 * Fired when the {@link VirtualKeyboard} ticks
		 * 
		 * @param vkeyboard The {@link VirtualKeyboardInput#nextKeyboard} that is supposed to be pressed
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
	public static interface EventVirtualMouseTick extends EventBase {
		
		/**
		 * Fired when the {@link VirtualMouseInput#currentMouse} is updated
		 * 
		 * @param vmouse The {@link VirtualMouseInput#nextMouse} that is supposed to be pressed
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
	public static interface EventVirtualCameraAngleTick extends EventBase {
		
		/**
		 * Fired when the {@link VirtualCameraAngleInput#currentCameraAngle} is updated
		 * 
		 * @param vcamera The {@link VirtualCameraAngleInput#nextCameraAngle}
		 * @returns The redirected cameraAngle
		 * @see VirtualCameraAngleInput#nextCameraTick()
		 */
		public VirtualCameraAngle onVirtualCameraTick(VirtualCameraAngle vcamera);
	}
}
