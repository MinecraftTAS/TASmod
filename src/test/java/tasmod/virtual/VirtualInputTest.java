package tasmod.virtual;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;

import com.minecrafttas.tasmod.virtual.VirtualCameraAngle2;
import com.minecrafttas.tasmod.virtual.VirtualInput2;
import com.minecrafttas.tasmod.virtual.VirtualKey2;
import com.minecrafttas.tasmod.virtual.VirtualKeyboard2;
import com.minecrafttas.tasmod.virtual.VirtualMouse2;

class VirtualInputTest {

	private final Logger LOGGER = LogManager.getLogger("TASmod");
	
	/**
	 * Test constructor initializing keyboard, mouse and camera_angle
	 */
	@Test
	void testConstructor() {
		VirtualInput2 virtual = new VirtualInput2(LOGGER);
		
		assertNotNull(virtual.KEYBOARD);
		assertNotNull(virtual.MOUSE);
		assertNotNull(virtual.CAMERA_ANGLE);
	}
	
	/**
	 * Tests if a keyboard can be preloaded
	 */
	@Test
	void testPreloadedConstructor() {
		VirtualKeyboard2 preloadedKeyboard = new VirtualKeyboard2();
		VirtualMouse2 preloadedMouse = new VirtualMouse2();
		VirtualCameraAngle2 preloadedCameraAngle = new VirtualCameraAngle2(1f, 2f);
		
		preloadedKeyboard.update(VirtualKey2.W.getKeycode(), true, 'w');
		preloadedMouse.update(VirtualKey2.LC.getKeycode(), true, 15, 0, 0);
		
		
		VirtualInput2 virtual = new VirtualInput2(LOGGER, preloadedKeyboard, preloadedMouse, preloadedCameraAngle);
		
		virtual.KEYBOARD.nextKeyboardTick();
		assertTrue(virtual.KEYBOARD.nextKeyboardSubtick());
		assertEquals(VirtualKey2.W.getKeycode(), virtual.KEYBOARD.getEventKeyboardKey());
		
		virtual.MOUSE.nextMouseTick();
		assertTrue(virtual.MOUSE.nextMouseSubtick());
		assertEquals(VirtualKey2.LC.getKeycode(), virtual.MOUSE.getEventMouseKey());
		
		assertEquals(1f, virtual.CAMERA_ANGLE.getPitch());
		assertEquals(2f, virtual.CAMERA_ANGLE.getYaw());
	}
	
	/**
	 * Simulate key presses
	 */
	@Test
	void testKeyboardAddPresses() {
		VirtualInput2 virtual = new VirtualInput2(LOGGER);
		
		// Simulate pressing keys WAS on the keyboard
		virtual.KEYBOARD.updateNextKeyboard(VirtualKey2.W.getKeycode(), true, 'w');
		virtual.KEYBOARD.updateNextKeyboard(VirtualKey2.A.getKeycode(), true, 'a');
		virtual.KEYBOARD.updateNextKeyboard(VirtualKey2.S.getKeycode(), true, 's');
		
		// Load the next keyboard events
		virtual.KEYBOARD.nextKeyboardTick();
		
		//W
		
		// Load new subtick
		assertTrue(virtual.KEYBOARD.nextKeyboardSubtick());
		
		// Read out values from the subtick
		assertEquals(VirtualKey2.W.getKeycode(), virtual.KEYBOARD.getEventKeyboardKey());
		assertTrue(virtual.KEYBOARD.getEventKeyboardState());
		assertEquals('w', virtual.KEYBOARD.getEventKeyboardCharacter());
		
		//A
		
		// Load new subtick
		assertTrue(virtual.KEYBOARD.nextKeyboardSubtick());
		
		// Read out values from the subtick
		assertEquals(VirtualKey2.A.getKeycode(), virtual.KEYBOARD.getEventKeyboardKey());
		assertTrue(virtual.KEYBOARD.getEventKeyboardState());
		assertEquals('a', virtual.KEYBOARD.getEventKeyboardCharacter());
		
		//S
		
		// Load new subtick
		assertTrue(virtual.KEYBOARD.nextKeyboardSubtick());
		
		// Read out values from the subtick
		assertEquals(VirtualKey2.S.getKeycode(), virtual.KEYBOARD.getEventKeyboardKey());
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
		VirtualKeyboard2 preloadedKeyboard = new VirtualKeyboard2();
		
		preloadedKeyboard.update(VirtualKey2.W.getKeycode(), true, 'w');
		VirtualInput2 virtual = new VirtualInput2(LOGGER, preloadedKeyboard, new VirtualMouse2(), new VirtualCameraAngle2());
		
		virtual.KEYBOARD.updateNextKeyboard(VirtualKey2.W.getKeycode(), false, Character.MIN_VALUE);
		
		// Load the next keyboard events
		virtual.KEYBOARD.nextKeyboardTick();
		
		// Load a new subtick
		assertTrue(virtual.KEYBOARD.nextKeyboardSubtick());
		
		// Read out values from the subtick
		assertEquals(VirtualKey2.W.getKeycode(), virtual.KEYBOARD.getEventKeyboardKey());
		assertFalse(virtual.KEYBOARD.getEventKeyboardState());
		assertEquals(Character.MIN_VALUE, virtual.KEYBOARD.getEventKeyboardCharacter());
		
		// Check if subtick list is empty
		assertFalse(virtual.KEYBOARD.nextKeyboardSubtick());
	}
}
