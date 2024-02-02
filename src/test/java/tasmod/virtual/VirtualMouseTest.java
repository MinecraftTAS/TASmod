package tasmod.virtual;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.minecrafttas.tasmod.virtual.VirtualKey2;
import com.minecrafttas.tasmod.virtual.VirtualMouse2;

class VirtualMouseTest {
	
	/**
	 * Test the empty constructor
	 */
	@Test
	void testEmptyConstructor() {
		VirtualMouse2 actual = new VirtualMouse2();
		assertTrue(actual.getPressedKeys().isEmpty());
		assertEquals(0, actual.getScrollWheel());
		assertNull(actual.getCursorX());
		assertNull(actual.getCursorY());
		assertTrue(actual.isParent());
	}
	
	/**
	 * Test constructor with premade keycode sets
	 */
	@Test
	void testSubtickConstructor() {
        Set<Integer> expected = new HashSet<>();
        expected.add(VirtualKey2.LC.getKeycode());
        expected.add(VirtualKey2.RC.getKeycode());

        VirtualMouse2 actual = new VirtualMouse2(expected, -15, 0, 2);

        assertIterableEquals(expected, actual.getPressedKeys());
        assertEquals(-15, actual.getScrollWheel());
        assertEquals(0, actual.getCursorX());
        assertEquals(2, actual.getCursorY());
        assertFalse(actual.isParent());
	}
	
    /**
     * Test setting the keycodes via setPressed to "pressed"
     */
    @Test
    void testSetPressedByKeycode(){
        VirtualMouse2 actual = new VirtualMouse2();
        actual.setPressed(VirtualKey2.LC.getKeycode(), true);

        assertIterableEquals(Arrays.asList(VirtualKey2.LC.getKeycode()), actual.getPressedKeys());
        assertTrue(actual.isParent());
    }
}
