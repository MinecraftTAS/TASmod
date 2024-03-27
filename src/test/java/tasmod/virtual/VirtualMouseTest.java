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

import com.minecrafttas.tasmod.virtual.VirtualMouse;
import org.junit.jupiter.api.Test;

import com.minecrafttas.tasmod.virtual.VirtualKey;
import com.minecrafttas.tasmod.virtual.event.VirtualMouseEvent;

class VirtualMouseTest {
	
	/**
	 * Test the empty constructor
	 */
	@Test
	void testEmptyConstructor() {
		VirtualMouse actual = new VirtualMouse();
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
        expected.add(VirtualKey.LC.getKeycode());
        expected.add(VirtualKey.RC.getKeycode());

        VirtualMouse actual = new VirtualMouse(expected, -15, 0, 2);

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
        VirtualMouse actual = new VirtualMouse();
        actual.setPressed(VirtualKey.LC.getKeycode(), true);

        assertIterableEquals(Arrays.asList(VirtualKey.LC.getKeycode()), actual.getPressedKeys());
        assertTrue(actual.isParent());
    }

	/**
	 * Test setting the keynames via setPressed to "pressed"
	 */
	@Test
	void testSetPressedByKeyname(){
		VirtualMouse actual = new VirtualMouse();
		actual.setPressed("LC", true);

		assertIterableEquals(Arrays.asList(VirtualKey.LC.getKeycode()), actual.getPressedKeys());
		assertTrue(actual.isParent());
	}

	/**
	 * Test setting the keycodes via setPressed to "unpressed"
	 */
	@Test
	void testSetUnPressedByKeycode(){
		Set<Integer> testKeycodeSet = new HashSet<>();
		testKeycodeSet.add(VirtualKey.LC.getKeycode());
		testKeycodeSet.add(VirtualKey.MBUTTON9.getKeycode());
		VirtualMouse actual = new VirtualMouse(testKeycodeSet, 0, 0, 0);
		actual.setPressed(VirtualKey.MBUTTON9.getKeycode(), false);

		assertIterableEquals(Arrays.asList(VirtualKey.LC.getKeycode()), actual.getPressedKeys());
	}

	/**
	 * Test setting the keynames via setPressed to "unpressed"
	 */
	@Test
	void testSetUnPressedByKeyname(){
		Set<Integer> testKeycodeSet = new HashSet<>();
		testKeycodeSet.add(VirtualKey.LC.getKeycode());
		testKeycodeSet.add(VirtualKey.MBUTTON9.getKeycode());
		VirtualMouse actual = new VirtualMouse(testKeycodeSet, 0, 0, 0);
		actual.setPressed("MBUTTON9", false);

		assertIterableEquals(Arrays.asList(VirtualKey.LC.getKeycode()), actual.getPressedKeys());
	}

	/**
	 * Test the toString method <em>without</em> subticks
	 */
	@Test
	void testToString(){
		Set<Integer> testKeycodeSet = new LinkedHashSet<>();
		testKeycodeSet.add(VirtualKey.LC.getKeycode());
		testKeycodeSet.add(VirtualKey.MC.getKeycode());

		VirtualMouse actual = new VirtualMouse(testKeycodeSet, 10, 100, 120);

		assertEquals("LC,MC;10,100,120", actual.toString());
	}

	/**
	 * Test the toString method <em>with</em> subticks
	 */
	@Test
	void testToStringSubtick(){
		VirtualMouse actual = new VirtualMouse();
		actual.update(VirtualKey.LC.getKeycode(), true, 10, 100, 120);
		actual.update(VirtualKey.MC.getKeycode(), true, 0, 12, 3);

		assertEquals("LC;10,100,120\nLC,MC;0,12,3", actual.toString());
	}

	/**
	 * Test equals method
	 */
	@Test
	void testEquals() {
		Set<Integer> testKeycodeSet = new HashSet<>();
		testKeycodeSet.add(VirtualKey.W.getKeycode());
		testKeycodeSet.add(VirtualKey.S.getKeycode());


		VirtualMouse actual = new VirtualMouse(testKeycodeSet, -15, 129, 340);
		VirtualMouse actual2 = new VirtualMouse(testKeycodeSet, -15, 129, 340);

		assertEquals(actual, actual2);
	}

	/**
	 * Test where equals will fail
	 */
	@Test
	void testNotEquals() {
		Set<Integer> testKeycodeSet = new HashSet<>();
		testKeycodeSet.add(VirtualKey.LC.getKeycode());

		VirtualMouse actual = new VirtualMouse(testKeycodeSet, -15, 1, 1);

		Set<Integer> testKeycodeSet2 = new HashSet<>();
		testKeycodeSet.add(VirtualKey.RC.getKeycode());
		VirtualMouse test2 = new VirtualMouse(testKeycodeSet2, -15, 1, 1);



		VirtualMouse test3 = new VirtualMouse(testKeycodeSet, -16, 1, 1);

		VirtualMouse test4 = new VirtualMouse(testKeycodeSet, -15, 2, 1);
		VirtualMouse test5 = new VirtualMouse(testKeycodeSet, -15, 1, 2);

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
		testKeycodeSet.add(VirtualKey.LC.getKeycode());
		testKeycodeSet.add(VirtualKey.MC.getKeycode());

		VirtualMouse actual = new VirtualMouse(testKeycodeSet, 10, 3, 2);
		VirtualMouse test2 = actual.clone();

		assertEquals(actual, test2);
	}

	/**
	 * Test moveFrom method
	 */
	@Test
	void testMoveFrom() {
		VirtualMouse moveFrom = new VirtualMouse();
		VirtualMouse actual = new VirtualMouse();

		moveFrom.update(VirtualKey.LC.getKeycode(), true, 0, 0, 0);
		moveFrom.update(VirtualKey.MOUSEMOVED.getKeycode(), false, 120, 10, 20);

		VirtualMouse expected = moveFrom.clone();

		actual.update(VirtualKey.MBUTTON12.getKeycode(), true, 0,0,0);
		actual.update(VirtualKey.MOUSEMOVED.getKeycode(), true, -120, -10, -10);

		actual.moveFrom(null);
		
		actual.moveFrom(moveFrom);

		assertIterableEquals(expected.getPressedKeys(), actual.getPressedKeys());
		assertEquals(expected.getScrollWheel(), actual.getScrollWheel());
		assertEquals(expected.getCursorX(), actual.getCursorX());
		assertEquals(expected.getCursorY(), actual.getCursorY());

		assertTrue(moveFrom.getSubticks().isEmpty());
		assertEquals(0, moveFrom.getScrollWheel());
		assertEquals(0, moveFrom.getCursorX());
		assertEquals(0, moveFrom.getCursorY());
	}
	
	/**
	 * Test copyFrom method
	 */
	@Test
	void testCopyFrom() {
		VirtualMouse copyFrom = new VirtualMouse();
		VirtualMouse actual = new VirtualMouse();

		copyFrom.update(VirtualKey.LC.getKeycode(), true, 0, 0, 0);
		copyFrom.update(VirtualKey.MOUSEMOVED.getKeycode(), false, 120, 10, 20);

		VirtualMouse expected = copyFrom.clone();

		actual.update(VirtualKey.MBUTTON12.getKeycode(), true, 0,0,0);
		actual.update(VirtualKey.MOUSEMOVED.getKeycode(), true, -120, -10, -10);

		actual.copyFrom(null);
		
		actual.copyFrom(copyFrom);

		assertIterableEquals(expected.getPressedKeys(), actual.getPressedKeys());
		assertEquals(expected.getScrollWheel(), actual.getScrollWheel());
		assertEquals(expected.getCursorX(), actual.getCursorX());
		assertEquals(expected.getCursorY(), actual.getCursorY());

		assertFalse(copyFrom.getSubticks().isEmpty());
		assertEquals(120, copyFrom.getScrollWheel());
		assertEquals(10, copyFrom.getCursorX());
		assertEquals(20, copyFrom.getCursorY());
	}

	/**
	 * Test subtick list being filled via update
	 */
	@Test
	void testUpdate(){
		VirtualMouse actual = new VirtualMouse();
		actual.update(VirtualKey.LC.getKeycode(), true, -30, 118, 42);
		actual.update(VirtualKey.MOUSEMOVED.getKeycode(), false, 0, 23, 144);

		List<VirtualMouse> expected = new ArrayList<>();
		expected.add(new VirtualMouse(new HashSet<Integer>(Arrays.asList(VirtualKey.LC.getKeycode())), -30, 118, 42));
		expected.add(new VirtualMouse(new HashSet<Integer>(Arrays.asList(VirtualKey.LC.getKeycode())), 0, 23, 144));

		assertIterableEquals(expected, actual.getAll());
	}
	
    /**
     * Tests getDifference
     */
    @Test
    void testGetDifference(){
        VirtualMouse test = new VirtualMouse(new HashSet<>(Arrays.asList(VirtualKey.LC.getKeycode())), 15, 0, 0);
        VirtualMouse test2 = new VirtualMouse(new HashSet<>(Arrays.asList(VirtualKey.LC.getKeycode(), VirtualKey.RC.getKeycode())), 30, 1, 2);
        Queue<VirtualMouseEvent> actual = new ConcurrentLinkedQueue<>();
        test.getDifference(test2, actual);
        Queue<VirtualMouseEvent> expected = new ConcurrentLinkedQueue<>(Arrays.asList(new VirtualMouseEvent(VirtualKey.RC.getKeycode(), true, 30, 1, 2)));

        assertIterableEquals(expected, actual);
    }
    
    /**
     * Tests generating virtual events going from an unpressed mouse to a pressed mouse state
     */
    @Test
    void testGetVirtualEventsPress() {
    	VirtualMouse unpressed = new VirtualMouse();
    	
    	VirtualMouse pressed = new VirtualMouse();
    	pressed.update(VirtualKey.LC.getKeycode(), true, 15, 10, 12);
    	
    	// Load actual with the events
    	Queue<VirtualMouseEvent> actual = new ConcurrentLinkedQueue<>();
    	unpressed.getVirtualEvents(pressed, actual);
    	
    	// Load expected
    	List<VirtualMouseEvent> expected = Arrays.asList(new VirtualMouseEvent(VirtualKey.LC.getKeycode(), true, 15, 10, 12));
    	
    	assertIterableEquals(expected, actual);
    }
    
    /**
     * Tests generating virtual events going from a pressed mouse to an unpressed mouse state
     */
    @Test
    void testGetVirtualEventsUnpress() {
    	VirtualMouse unpressed = new VirtualMouse();
    	
    	VirtualMouse pressed = new VirtualMouse();
    	pressed.update(VirtualKey.LC.getKeycode(), true, 15, 10, 12);
    	
    	// Load actual with the events
    	Queue<VirtualMouseEvent> actual = new ConcurrentLinkedQueue<>();
    	pressed.getVirtualEvents(unpressed, actual);
    	
    	// Load expected
    	List<VirtualMouseEvent> expected = Arrays.asList(new VirtualMouseEvent(VirtualKey.LC.getKeycode(), false, 0, 0, 0));
    	
    	assertIterableEquals(expected, actual);
    }
    
    /**
     * Tests 2 updates having the same value and the scroll wheel is 0. Should return no additional button events
     */
    @Test
    void testSameUpdate() {
    	VirtualMouse unpressed = new VirtualMouse();
    	
    	VirtualMouse pressed = new VirtualMouse();
    	pressed.update(VirtualKey.LC.getKeycode(), true, 0, 10, 12);
    	pressed.update(VirtualKey.LC.getKeycode(), true, 0, 10, 12);
    	
    	// Load actual with the events
    	Queue<VirtualMouseEvent> actual = new ConcurrentLinkedQueue<>();
    	unpressed.getVirtualEvents(pressed, actual);
    	
    	// Load expected
    	List<VirtualMouseEvent> expected = Arrays.asList(
    			new VirtualMouseEvent(VirtualKey.LC.getKeycode(), true, 0, 10, 12) // Should only have one keyboard event
    			); 
    	
    	assertIterableEquals(expected, actual);
    }
    
    
    /**
     * Tests 2 updates having the same pressed keys, but scrollWheel != 0
     */
    @Test
    void testScrollWheelDifferent() {
    	VirtualMouse unpressed = new VirtualMouse();
    	
    	VirtualMouse pressed = new VirtualMouse();
    	pressed.update(VirtualKey.LC.getKeycode(), true, 15, 10, 12);
    	pressed.update(VirtualKey.LC.getKeycode(), true, 15, 10, 12);
    	
    	// Load actual with the events
    	Queue<VirtualMouseEvent> actual = new ConcurrentLinkedQueue<>();
    	unpressed.getVirtualEvents(pressed, actual);
    	
    	// Load expected
    	List<VirtualMouseEvent> expected = Arrays.asList(
    			new VirtualMouseEvent(VirtualKey.LC.getKeycode(), true, 15, 10, 12),
    			new VirtualMouseEvent(VirtualKey.MOUSEMOVED.getKeycode(), false, 15, 10, 12) // Adds an additional "MOUSEMOVED" event with the scroll wheel
    			);
    	
    	assertIterableEquals(expected, actual);
    }
    
    /**
     * Tests 2 updates having the same pressed keys, but scrollWheel is different
     */
    @Test
    void testCursorXDifferent() {
    	VirtualMouse unpressed = new VirtualMouse();
    	
    	VirtualMouse pressed = new VirtualMouse();
    	pressed.update(VirtualKey.LC.getKeycode(), true, 0, 10, 12);
    	pressed.update(VirtualKey.LC.getKeycode(), true, 0, 11, 12);
    	
    	// Load actual with the events
    	Queue<VirtualMouseEvent> actual = new ConcurrentLinkedQueue<>();
    	unpressed.getVirtualEvents(pressed, actual);
    	
    	// Load expected
    	List<VirtualMouseEvent> expected = Arrays.asList(
    			new VirtualMouseEvent(VirtualKey.LC.getKeycode(), true, 0, 10, 12),
    			new VirtualMouseEvent(VirtualKey.MOUSEMOVED.getKeycode(), false, 0, 11, 12) // Adds an additional "MOUSEMOVED" event with the cursorX
    			);
    	
    	assertIterableEquals(expected, actual);
    }
    
    /**
     * Tests 2 updates having the same pressed keys, but scrollWheel is different
     */
    @Test
    void testCursorYDifferent() {
    	VirtualMouse unpressed = new VirtualMouse();
    	
    	VirtualMouse pressed = new VirtualMouse();
    	pressed.update(VirtualKey.LC.getKeycode(), true, 0, 10, 12);
    	pressed.update(VirtualKey.LC.getKeycode(), true, 0, 10, 120);
    	
    	// Load actual with the events
    	Queue<VirtualMouseEvent> actual = new ConcurrentLinkedQueue<>();
    	unpressed.getVirtualEvents(pressed, actual);
    	
    	// Load expected
    	List<VirtualMouseEvent> expected = Arrays.asList(
    			new VirtualMouseEvent(VirtualKey.LC.getKeycode(), true, 0, 10, 12),
    			new VirtualMouseEvent(VirtualKey.MOUSEMOVED.getKeycode(), false, 0, 10, 120) // Adds an additional "MOUSEMOVED" event with the cursorY
    			);
    	
    	assertIterableEquals(expected, actual);
    }
}
