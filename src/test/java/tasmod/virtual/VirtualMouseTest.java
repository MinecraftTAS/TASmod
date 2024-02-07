package tasmod.virtual;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.junit.jupiter.api.Test;

import com.minecrafttas.tasmod.virtual.VirtualKey2;
import com.minecrafttas.tasmod.virtual.VirtualKeyboard2;
import com.minecrafttas.tasmod.virtual.VirtualKeyboardEvent;
import com.minecrafttas.tasmod.virtual.VirtualMouse2;
import com.minecrafttas.tasmod.virtual.VirtualMouseEvent;

class VirtualMouseTest {
	
	/**
	 * Test the empty constructor
	 */
	@Test
	void testEmptyConstructor() {
		VirtualMouse2 actual = new VirtualMouse2();
		assertTrue(actual.getPressedKeys().isEmpty());
		assertEquals(0, actual.getScrollWheel());
		assertEquals(0, actual.getCursorX());
		assertEquals(0, actual.getCursorY());
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

	/**
	 * Test setting the keynames via setPressed to "pressed"
	 */
	@Test
	void testSetPressedByKeyname(){
		VirtualMouse2 actual = new VirtualMouse2();
		actual.setPressed("LC", true);

		assertIterableEquals(Arrays.asList(VirtualKey2.LC.getKeycode()), actual.getPressedKeys());
		assertTrue(actual.isParent());
	}

	/**
	 * Test setting the keycodes via setPressed to "unpressed"
	 */
	@Test
	void testSetUnPressedByKeycode(){
		Set<Integer> testKeycodeSet = new HashSet<>();
		testKeycodeSet.add(VirtualKey2.LC.getKeycode());
		testKeycodeSet.add(VirtualKey2.MBUTTON9.getKeycode());
		VirtualMouse2 actual = new VirtualMouse2(testKeycodeSet, 0, 0, 0);
		actual.setPressed(VirtualKey2.MBUTTON9.getKeycode(), false);

		assertIterableEquals(Arrays.asList(VirtualKey2.LC.getKeycode()), actual.getPressedKeys());
	}

	/**
	 * Test setting the keynames via setPressed to "unpressed"
	 */
	@Test
	void testSetUnPressedByKeyname(){
		Set<Integer> testKeycodeSet = new HashSet<>();
		testKeycodeSet.add(VirtualKey2.LC.getKeycode());
		testKeycodeSet.add(VirtualKey2.MBUTTON9.getKeycode());
		VirtualMouse2 actual = new VirtualMouse2(testKeycodeSet, 0, 0, 0);
		actual.setPressed("MBUTTON9", false);

		assertIterableEquals(Arrays.asList(VirtualKey2.LC.getKeycode()), actual.getPressedKeys());
	}

	/**
	 * Test the toString method <em>without</em> subticks
	 */
	@Test
	void testToString(){
		Set<Integer> testKeycodeSet = new LinkedHashSet<>();
		testKeycodeSet.add(VirtualKey2.LC.getKeycode());
		testKeycodeSet.add(VirtualKey2.MC.getKeycode());

		VirtualMouse2 actual = new VirtualMouse2(testKeycodeSet, 10, 100, 120);

		assertEquals("LC,MC;10,100,120", actual.toString());
	}

	/**
	 * Test the toString method <em>with</em> subticks
	 */
	@Test
	void testToStringSubtick(){
		VirtualMouse2 actual = new VirtualMouse2();
		actual.update(VirtualKey2.LC.getKeycode(), true, 10, 100, 120);
		actual.update(VirtualKey2.MC.getKeycode(), true, 0, 12, 3);

		assertEquals("LC;10,100,120\nLC,MC;0,12,3", actual.toString());
	}

	/**
	 * Test equals method
	 */
	@Test
	void testEquals() {
		Set<Integer> testKeycodeSet = new HashSet<>();
		testKeycodeSet.add(VirtualKey2.W.getKeycode());
		testKeycodeSet.add(VirtualKey2.S.getKeycode());


		VirtualMouse2 actual = new VirtualMouse2(testKeycodeSet, -15, 129, 340);
		VirtualMouse2 actual2 = new VirtualMouse2(testKeycodeSet, -15, 129, 340);

		assertEquals(actual, actual2);
	}

	/**
	 * Test where equals will fail
	 */
	@Test
	void testNotEquals() {
		Set<Integer> testKeycodeSet = new HashSet<>();
		testKeycodeSet.add(VirtualKey2.LC.getKeycode());

		VirtualMouse2 actual = new VirtualMouse2(testKeycodeSet, -15, 1, 1);

		Set<Integer> testKeycodeSet2 = new HashSet<>();
		testKeycodeSet.add(VirtualKey2.RC.getKeycode());
		VirtualMouse2 test2 = new VirtualMouse2(testKeycodeSet2, -15, 1, 1);



		VirtualMouse2 test3 = new VirtualMouse2(testKeycodeSet, -16, 1, 1);

		VirtualMouse2 test4 = new VirtualMouse2(testKeycodeSet, -15, 2, 1);
		VirtualMouse2 test5 = new VirtualMouse2(testKeycodeSet, -15, 1, 2);

		assertNotEquals(actual, test2);
		assertNotEquals(actual, test3);
		assertNotEquals(actual, test4);
		assertNotEquals(actual, test5);
		assertNotEquals(actual, null);
	}

	/**
	 * Test cloning the mouse
	 */
	@Test
	void testClone() {
		Set<Integer> testKeycodeSet = new HashSet<>();
		testKeycodeSet.add(VirtualKey2.LC.getKeycode());
		testKeycodeSet.add(VirtualKey2.MC.getKeycode());

		VirtualMouse2 actual = new VirtualMouse2(testKeycodeSet, 10, 3, 2);
		VirtualMouse2 test2 = actual.clone();

		assertEquals(actual, test2);
	}

	/**
	 * Test copyFrom method
	 */
	@Test
	void testCopyFrom() {
		VirtualMouse2 copyFrom = new VirtualMouse2();
		VirtualMouse2 actual = new VirtualMouse2();

		copyFrom.update(VirtualKey2.LC.getKeycode(), true, 0, 0, 0);
		copyFrom.update(VirtualKey2.MOUSEMOVED.getKeycode(), false, 120, 10, 20);

		VirtualMouse2 expected = copyFrom.clone();

		actual.update(VirtualKey2.MBUTTON12.getKeycode(), true, 0,0,0);
		actual.update(VirtualKey2.MOUSEMOVED.getKeycode(), true, -120, -10, -10);

		actual.copyFrom(copyFrom);

		assertIterableEquals(expected.getPressedKeys(), actual.getPressedKeys());
		assertEquals(expected.getScrollWheel(), actual.getScrollWheel());
		assertEquals(expected.getCursorX(), actual.getCursorX());
		assertEquals(expected.getCursorY(), actual.getCursorY());

		assertTrue(copyFrom.getSubticks().isEmpty());
		assertEquals(0, copyFrom.getScrollWheel());
		assertEquals(0, copyFrom.getCursorX());
		assertEquals(0, copyFrom.getCursorY());
	}

	/**
	 * Test subtick list being filled via update
	 */
	@Test
	void testUpdate(){
		VirtualMouse2 actual = new VirtualMouse2();
		actual.update(VirtualKey2.LC.getKeycode(), true, -30, 118, 42);
		actual.update(VirtualKey2.MOUSEMOVED.getKeycode(), false, 0, 23, 144);

		List<VirtualMouse2> expected = new ArrayList<>();
		expected.add(new VirtualMouse2(new HashSet<Integer>(Arrays.asList(VirtualKey2.LC.getKeycode())), -30, 118, 42));
		expected.add(new VirtualMouse2(new HashSet<Integer>(Arrays.asList(VirtualKey2.LC.getKeycode())), 0, 23, 144));

		assertIterableEquals(expected, actual.getAll());
	}
	
    /**
     * Tests getDifference
     */
    @Test
    void testGetDifference(){
        VirtualMouse2 test = new VirtualMouse2(new HashSet<>(Arrays.asList(VirtualKey2.LC.getKeycode())), 15, 0, 0);
        VirtualMouse2 test2 = new VirtualMouse2(new HashSet<>(Arrays.asList(VirtualKey2.LC.getKeycode(), VirtualKey2.RC.getKeycode())), 30, 1, 2);
        Queue<VirtualMouseEvent> actual = new ConcurrentLinkedQueue<>();
        test.getDifference(test2, actual);
        Queue<VirtualMouseEvent> expected = new ConcurrentLinkedQueue<>(Arrays.asList(new VirtualMouseEvent(VirtualKey2.RC.getKeycode(), true, 30, 1, 2)));

        assertIterableEquals(expected, actual);
    }
    
    /**
     * Tests generating virtual events going from an unpressed mouse to a pressed mouse state
     */
    @Test
    void testGetVirtualEventsPress() {
    	VirtualMouse2 unpressed = new VirtualMouse2();
    	
    	VirtualMouse2 pressed = new VirtualMouse2();
    	pressed.update(VirtualKey2.LC.getKeycode(), true, 15, 10, 12);
    	
    	// Load actual with the events
    	Queue<VirtualMouseEvent> actual = new ConcurrentLinkedQueue<>();
    	unpressed.getVirtualEvents(pressed, actual);
    	
    	// Load expected
    	List<VirtualMouseEvent> expected = Arrays.asList(new VirtualMouseEvent(VirtualKey2.LC.getKeycode(), true, 15, 10, 12));
    	
    	assertIterableEquals(expected, actual);
    }
    
    /**
     * Tests generating virtual events going from a pressed mouse to an unpressed mouse state
     */
    @Test
    void testGetVirtualEventsUnpress() {
    	VirtualMouse2 unpressed = new VirtualMouse2();
    	
    	VirtualMouse2 pressed = new VirtualMouse2();
    	pressed.update(VirtualKey2.LC.getKeycode(), true, 15, 10, 12);
    	
    	// Load actual with the events
    	Queue<VirtualMouseEvent> actual = new ConcurrentLinkedQueue<>();
    	pressed.getVirtualEvents(unpressed, actual);
    	
    	// Load expected
    	List<VirtualMouseEvent> expected = Arrays.asList(new VirtualMouseEvent(VirtualKey2.LC.getKeycode(), false, 0, 0, 0));
    	
    	assertIterableEquals(expected, actual);
    }
    
    /**
     * Tests 2 updates having the same value. Should return no additional button events
     */
    @Test
    void testSameUpdate() {
    	VirtualMouse2 unpressed = new VirtualMouse2();
    	
    	VirtualMouse2 pressed = new VirtualMouse2();
    	pressed.update(VirtualKey2.LC.getKeycode(), true, 15, 10, 12);
    	pressed.update(VirtualKey2.LC.getKeycode(), true, 15, 10, 12);
    	
    	// Load actual with the events
    	Queue<VirtualMouseEvent> actual = new ConcurrentLinkedQueue<>();
    	unpressed.getVirtualEvents(pressed, actual);
    	
    	// Load expected
    	List<VirtualMouseEvent> expected = Arrays.asList(
    			new VirtualMouseEvent(VirtualKey2.LC.getKeycode(), true, 15, 10, 12) // Should only have one keyboard event
    			); 
    	
    	assertIterableEquals(expected, actual);
    }
    
    /**
     * Tests 2 updates having the same pressed keys, but scrollWheel is different
     */
    @Test
    void testScrollWheelDifferent() {
    	VirtualMouse2 unpressed = new VirtualMouse2();
    	
    	VirtualMouse2 pressed = new VirtualMouse2();
    	pressed.update(VirtualKey2.LC.getKeycode(), true, 15, 10, 12);
    	pressed.update(VirtualKey2.LC.getKeycode(), true, -30, 10, 12);
    	
    	// Load actual with the events
    	Queue<VirtualMouseEvent> actual = new ConcurrentLinkedQueue<>();
    	unpressed.getVirtualEvents(pressed, actual);
    	
    	// Load expected
    	List<VirtualMouseEvent> expected = Arrays.asList(
    			new VirtualMouseEvent(VirtualKey2.LC.getKeycode(), true, 15, 10, 12),
    			new VirtualMouseEvent(VirtualKey2.MOUSEMOVED.getKeycode(), false, -30, 10, 12) // Adds an additional "MOUSEMOVED" event with the scroll wheel
    			);
    	
    	assertIterableEquals(expected, actual);
    }
    
    /**
     * Tests 2 updates having the same pressed keys, but scrollWheel is different
     */
    @Test
    void testCursorXDifferent() {
    	VirtualMouse2 unpressed = new VirtualMouse2();
    	
    	VirtualMouse2 pressed = new VirtualMouse2();
    	pressed.update(VirtualKey2.LC.getKeycode(), true, 15, 10, 12);
    	pressed.update(VirtualKey2.LC.getKeycode(), true, 15, 11, 12);
    	
    	// Load actual with the events
    	Queue<VirtualMouseEvent> actual = new ConcurrentLinkedQueue<>();
    	unpressed.getVirtualEvents(pressed, actual);
    	
    	// Load expected
    	List<VirtualMouseEvent> expected = Arrays.asList(
    			new VirtualMouseEvent(VirtualKey2.LC.getKeycode(), true, 15, 10, 12),
    			new VirtualMouseEvent(VirtualKey2.MOUSEMOVED.getKeycode(), false, 15, 11, 12) // Adds an additional "MOUSEMOVED" event with the cursorX
    			);
    	
    	assertIterableEquals(expected, actual);
    }
    
    /**
     * Tests 2 updates having the same pressed keys, but scrollWheel is different
     */
    @Test
    void testCursorYDifferent() {
    	VirtualMouse2 unpressed = new VirtualMouse2();
    	
    	VirtualMouse2 pressed = new VirtualMouse2();
    	pressed.update(VirtualKey2.LC.getKeycode(), true, 15, 10, 12);
    	pressed.update(VirtualKey2.LC.getKeycode(), true, 15, 10, 120);
    	
    	// Load actual with the events
    	Queue<VirtualMouseEvent> actual = new ConcurrentLinkedQueue<>();
    	unpressed.getVirtualEvents(pressed, actual);
    	
    	// Load expected
    	List<VirtualMouseEvent> expected = Arrays.asList(
    			new VirtualMouseEvent(VirtualKey2.LC.getKeycode(), true, 15, 10, 12),
    			new VirtualMouseEvent(VirtualKey2.MOUSEMOVED.getKeycode(), false, 15, 10, 120) // Adds an additional "MOUSEMOVED" event with the cursorY
    			);
    	
    	assertIterableEquals(expected, actual);
    }
}
