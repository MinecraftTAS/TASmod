package tasmod.virtual;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.minecrafttas.tasmod.virtual.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.minecrafttas.tasmod.virtual.VirtualInput;

class VirtualInputTest {

	private final Logger LOGGER = LogManager.getLogger("TASmod");
	
	/**
	 * Test constructor initializing keyboard, mouse and camera_angle
	 */
	@Test
	void testConstructor() {
		VirtualInput virtual = new VirtualInput(LOGGER);
		
		assertNotNull(virtual.KEYBOARD);
		assertNotNull(virtual.MOUSE);
		assertNotNull(virtual.CAMERA_ANGLE);
	}
	
	/**
	 * Tests if a keyboard can be preloaded
	 */
	@Test
	void testPreloadedConstructor() {
		VirtualKeyboard preloadedKeyboard = new VirtualKeyboard();
		VirtualMouse preloadedMouse = new VirtualMouse();
		VirtualCameraAngle preloadedCameraAngle = new VirtualCameraAngle();
		
		preloadedKeyboard.update(VirtualKey.W.getKeycode(), true, 'w');
		preloadedMouse.update(VirtualKey.LC.getKeycode(), true, 15, 0, 0);
		preloadedCameraAngle.update(1f, 2f);
		
		
		VirtualInput virtual = new VirtualInput(LOGGER, preloadedKeyboard, preloadedMouse, preloadedCameraAngle);
		
		virtual.KEYBOARD.nextKeyboardTick();
		assertTrue(virtual.KEYBOARD.nextKeyboardSubtick());
		assertEquals(VirtualKey.W.getKeycode(), virtual.KEYBOARD.getEventKeyboardKey());
		
		virtual.MOUSE.nextMouseTick();
		assertTrue(virtual.MOUSE.nextMouseSubtick());
		assertEquals(VirtualKey.LC.getKeycode(), virtual.MOUSE.getEventMouseKey());
		
		assertEquals(1f, virtual.CAMERA_ANGLE.getCurrentPitch());
		assertEquals(2f, virtual.CAMERA_ANGLE.getCurrentYaw());
	}
	
	/**
	 * Simulate key presses
	 */
	@Test
	void testKeyboardAddPresses() {
		VirtualInput virtual = new VirtualInput(LOGGER);
		
		// Simulate pressing keys WAS on the keyboard
		virtual.KEYBOARD.updateNextKeyboard(VirtualKey.W.getKeycode(), true, 'w');
		virtual.KEYBOARD.updateNextKeyboard(VirtualKey.A.getKeycode(), true, 'a');
		virtual.KEYBOARD.updateNextKeyboard(VirtualKey.S.getKeycode(), true, 's');
		
		// Load the next keyboard events
		virtual.KEYBOARD.nextKeyboardTick();
		
		//W
		
		// Load new subtick
		assertTrue(virtual.KEYBOARD.nextKeyboardSubtick());
		
		// Read out values from the subtick
		assertEquals(VirtualKey.W.getKeycode(), virtual.KEYBOARD.getEventKeyboardKey());
		assertTrue(virtual.KEYBOARD.getEventKeyboardState());
		assertEquals('w', virtual.KEYBOARD.getEventKeyboardCharacter());
		
		//A
		
		// Load new subtick
		assertTrue(virtual.KEYBOARD.nextKeyboardSubtick());
		
		// Read out values from the subtick
		assertEquals(VirtualKey.A.getKeycode(), virtual.KEYBOARD.getEventKeyboardKey());
		assertTrue(virtual.KEYBOARD.getEventKeyboardState());
		assertEquals('a', virtual.KEYBOARD.getEventKeyboardCharacter());
		
		//S
		
		// Load new subtick
		assertTrue(virtual.KEYBOARD.nextKeyboardSubtick());
		
		// Read out values from the subtick
		assertEquals(VirtualKey.S.getKeycode(), virtual.KEYBOARD.getEventKeyboardKey());
		assertEquals(true, virtual.KEYBOARD.getEventKeyboardState());
		assertEquals('s', virtual.KEYBOARD.getEventKeyboardCharacter());
		
		// Check if subtick list is empty
		assertFalse(virtual.KEYBOARD.nextKeyboardSubtick());
	}

	/**
	 * Test simulating button removals
	 */
	@Test
	void testKeyboardRemovePresses() {
		VirtualKeyboard preloadedKeyboard = new VirtualKeyboard();
		
		preloadedKeyboard.update(VirtualKey.W.getKeycode(), true, 'w');
		VirtualInput virtual = new VirtualInput(LOGGER, preloadedKeyboard, new VirtualMouse(), new VirtualCameraAngle());
		
		virtual.KEYBOARD.updateNextKeyboard(VirtualKey.W.getKeycode(), false, Character.MIN_VALUE);
		
		// Load the next keyboard events
		virtual.KEYBOARD.nextKeyboardTick();
		
		// Load a new subtick
		assertTrue(virtual.KEYBOARD.nextKeyboardSubtick());
		
		// Read out values from the subtick
		assertEquals(VirtualKey.W.getKeycode(), virtual.KEYBOARD.getEventKeyboardKey());
		assertFalse(virtual.KEYBOARD.getEventKeyboardState());
		assertEquals(Character.MIN_VALUE, virtual.KEYBOARD.getEventKeyboardCharacter());
		
		// Check if subtick list is empty
		assertFalse(virtual.KEYBOARD.nextKeyboardSubtick());
	}
}
