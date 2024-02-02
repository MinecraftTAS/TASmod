package tasmod.virtual.keyboard;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.junit.jupiter.api.Test;

import com.minecrafttas.tasmod.virtual.VirtualKey2;
import com.minecrafttas.tasmod.virtual.VirtualKeyboard2;
import com.minecrafttas.tasmod.virtual.VirtualKeyboardEvent;

public class VirtualKeyboardTest {

    /**
     * Test the empty constructor
     */
    @Test
    void testEmptyConstructor(){
        VirtualKeyboard2 test = new VirtualKeyboard2();
        assertTrue(test.getPressedKeys().isEmpty());
        assertTrue(test.getCharList().isEmpty());
        assertTrue(test.isParent());
    }

    /**
     * Test constructor with premade keycode sets
     */
    @Test
    void testSubtickConstructor(){
        Set<Integer> testKeycodeSet = new HashSet<>();
        testKeycodeSet.add(VirtualKey2.W.getKeycode());
        testKeycodeSet.add(VirtualKey2.S.getKeycode());

        List<Character> testCharList = new ArrayList<>();
        testCharList.add('w');
        testCharList.add('s');

        VirtualKeyboard2 test = new VirtualKeyboard2(testKeycodeSet, testCharList);

        assertIterableEquals(testKeycodeSet, test.getPressedKeys());
        assertIterableEquals(testCharList, test.getCharList());
        assertFalse(test.isParent());
    }

    /**
     * Test setting the keycodes via setPressed to "pressed"
     */
    @Test
    void testSetPressedByKeycode(){
        VirtualKeyboard2 test = new VirtualKeyboard2();
        test.setPressed(VirtualKey2.W.getKeycode(), true);

        assertIterableEquals(Arrays.asList(VirtualKey2.W.getKeycode()), test.getPressedKeys());
        assertTrue(test.isParent());
    }

    /**
     * Test setting the keynames via setPressed to "pressed"
     */
    @Test
    void testSetPressedByKeyname(){
        VirtualKeyboard2 test = new VirtualKeyboard2();
        test.setPressed("W", true);

        assertIterableEquals(Arrays.asList(VirtualKey2.W.getKeycode()), test.getPressedKeys());
        assertTrue(test.isParent());
    }

    /**
     * Test setting the keycodes via setPressed to "unpressed"
     */
    @Test
    void testSetUnPressedByKeycode(){
        Set<Integer> testKeycodeSet = new HashSet<>();
        testKeycodeSet.add(VirtualKey2.W.getKeycode());
        testKeycodeSet.add(VirtualKey2.S.getKeycode());
        VirtualKeyboard2 test = new VirtualKeyboard2(testKeycodeSet, new ArrayList<>());
        test.setPressed(VirtualKey2.W.getKeycode(), false);

        assertIterableEquals(Arrays.asList(VirtualKey2.S.getKeycode()), test.getPressedKeys());
    }

    /**
     * Test setting the keynames via setPressed to "unpressed"
     */
    @Test
    void testSetUnPressedByKeyname(){
        Set<Integer> testKeycodeSet = new HashSet<>();
        testKeycodeSet.add(VirtualKey2.W.getKeycode());
        testKeycodeSet.add(VirtualKey2.S.getKeycode());
        VirtualKeyboard2 test = new VirtualKeyboard2(testKeycodeSet, new ArrayList<>());
        test.setPressed("S", false);

        assertIterableEquals(Arrays.asList(VirtualKey2.W.getKeycode()), test.getPressedKeys());
    }

    /**
     * Test adding a character to the keyboard
     */
    @Test
    void testAddCharacter(){
        VirtualKeyboard2 test = new VirtualKeyboard2();
        test.addChar('w');

        assertIterableEquals(Arrays.asList('w'), test.getCharList());
    }

    /**
     * Test the toString method <em>without</em> subticks
     */
    @Test
    void testToString(){
        Set<Integer> testKeycodeSet = new HashSet<>();
        testKeycodeSet.add(VirtualKey2.W.getKeycode());
        testKeycodeSet.add(VirtualKey2.S.getKeycode());

        List<Character> testCharList = new ArrayList<>();
        testCharList.add('w');
        testCharList.add('s');

        VirtualKeyboard2 test = new VirtualKeyboard2(testKeycodeSet, testCharList);
        VirtualKeyboard2 test2 = new VirtualKeyboard2(testKeycodeSet, new ArrayList<>());

        assertEquals("W,S;ws", test.toString());
        assertEquals("W,S;", test2.toString());
    }

    /**
     * Test equals method
     */
    @Test
    void testEquals() {
        Set<Integer> testKeycodeSet = new HashSet<>();
        testKeycodeSet.add(VirtualKey2.W.getKeycode());
        testKeycodeSet.add(VirtualKey2.S.getKeycode());

        List<Character> testCharList = new ArrayList<>();
        testCharList.add('w');
        testCharList.add('s');

        VirtualKeyboard2 test = new VirtualKeyboard2(testKeycodeSet, testCharList);
        VirtualKeyboard2 test2 = new VirtualKeyboard2(testKeycodeSet, testCharList);
        
        assertEquals(test, test2);
    }

    /**
     * Test where equals will fail
     */
    @Test
    void testNotEquals() {
        Set<Integer> testKeycodeSet = new HashSet<>();
        testKeycodeSet.add(VirtualKey2.W.getKeycode());
        testKeycodeSet.add(VirtualKey2.S.getKeycode());

        List<Character> testCharList = new ArrayList<>();
        testCharList.add('w');
        testCharList.add('s');
        
        List<Character> testCharList2 = new ArrayList<>();
        testCharList2.add('w');
        testCharList2.add('S');
        
        List<Character> testCharList3 = new ArrayList<>();
        testCharList3.add('w');

        VirtualKeyboard2 test = new VirtualKeyboard2(testKeycodeSet, testCharList);
        VirtualKeyboard2 test2 = new VirtualKeyboard2(testKeycodeSet, testCharList2);
        VirtualKeyboard2 test3 = new VirtualKeyboard2(testKeycodeSet, testCharList3);
        
        assertNotEquals(test, test2);
        assertNotEquals(test, test3);
        assertNotEquals(test, null);
    }

    /**
     * Test cloning the keyboard
     */
    @Test
    void testClone() {
        Set<Integer> testKeycodeSet = new HashSet<>();
        testKeycodeSet.add(VirtualKey2.W.getKeycode());
        testKeycodeSet.add(VirtualKey2.S.getKeycode());

        List<Character> testCharList = new ArrayList<>();
        testCharList.add('w');
        testCharList.add('s');

        VirtualKeyboard2 test = new VirtualKeyboard2(testKeycodeSet, testCharList);
        VirtualKeyboard2 test2 = test.clone();
        
        assertEquals(test, test2);
    }

    /**
     * Test move from method
     */
    @Test
    void testMoveFrom(){
    	VirtualKeyboard2 copyFrom = new VirtualKeyboard2();
    	VirtualKeyboard2 actual = new VirtualKeyboard2();
    	
    	copyFrom.update(VirtualKey2.W.getKeycode(), true, 'w');
    	copyFrom.update(VirtualKey2.A.getKeycode(), true, 'a');
    	
    	VirtualKeyboard2 expected = copyFrom.clone();
    	
    	actual.update(VirtualKey2.S.getKeycode(), true, 's');
    	actual.update(VirtualKey2.D.getKeycode(), true, 'd');

        actual.copyFrom(copyFrom);

        assertIterableEquals(expected.getPressedKeys(), actual.getPressedKeys());
        assertIterableEquals(expected.getCharList(), actual.getCharList());

        assertFalse(copyFrom.getSubticks().isEmpty());
    }

    /**
     * Test subtick list being filled via update
     */
    @Test
    void testUpdate(){
        VirtualKeyboard2 test = new VirtualKeyboard2();
        test.update(VirtualKey2.W.getKeycode(), true, 'w');
        test.update(VirtualKey2.A.getKeycode(), true, 'A');

        List<VirtualKeyboard2> expected = new ArrayList<>();
        expected.add(new VirtualKeyboard2(new HashSet<Integer>(Arrays.asList(VirtualKey2.W.getKeycode())), Arrays.asList('w')));
        expected.add(new VirtualKeyboard2(new HashSet<Integer>(Arrays.asList(VirtualKey2.W.getKeycode(), VirtualKey2.A.getKeycode())), Arrays.asList('A')));

        assertIterableEquals(expected, test.getAll());
    }

    /**
     * Tests getDifference
     */
    @Test
    void testGetDifference(){
        VirtualKeyboard2 test = new VirtualKeyboard2(new HashSet<>(Arrays.asList(VirtualKey2.W.getKeycode())), Arrays.asList('w'));
        VirtualKeyboard2 test2 = new VirtualKeyboard2(new HashSet<>(Arrays.asList(VirtualKey2.W.getKeycode(), VirtualKey2.S.getKeycode())), Arrays.asList('S'));
        Queue<VirtualKeyboardEvent> actual = new ConcurrentLinkedQueue<>();
        test.getDifference(test2, actual);
        Queue<VirtualKeyboardEvent> expected = new ConcurrentLinkedQueue<>(Arrays.asList(new VirtualKeyboardEvent(VirtualKey2.S.getKeycode(), true, 'S')));

        assertIterableEquals(expected, actual);
    }
    
    /**
     * Tests generating virtual events going from an unpressed keyboard to a pressed keyboard state
     */
    @Test
    void testGetVirtualEventsPress() {
    	VirtualKeyboard2 unpressed = new VirtualKeyboard2();
    	
    	VirtualKeyboard2 pressed = new VirtualKeyboard2();
    	pressed.update(VirtualKey2.W.getKeycode(), true, 'w');
    	
    	// Load actual with the events
    	Queue<VirtualKeyboardEvent> actual = new ConcurrentLinkedQueue<>();
    	unpressed.getVirtualEvents(pressed, actual);
    	
    	// Load expected
    	List<VirtualKeyboardEvent> expected = Arrays.asList(new VirtualKeyboardEvent(VirtualKey2.W.getKeycode(), true, 'w'));
    	
    	assertIterableEquals(expected, actual);
    }
    
    /**
     * Tests generating virtual events going from a pressed keyboard to an unpressed keyboard state
     */
    @Test
    void testGetVirtualEventsUnpress() {
    	VirtualKeyboard2 unpressed = new VirtualKeyboard2();
    	
    	VirtualKeyboard2 pressed = new VirtualKeyboard2();
    	pressed.update(VirtualKey2.W.getKeycode(), true, 'w');
    	
    	// Load actual with the events
    	Queue<VirtualKeyboardEvent> actual = new ConcurrentLinkedQueue<>();
    	pressed.getVirtualEvents(unpressed, actual);
    	
    	// Load expected
    	List<VirtualKeyboardEvent> expected = Arrays.asList(new VirtualKeyboardEvent(VirtualKey2.W.getKeycode(), false, Character.MIN_VALUE));
    	
    	assertIterableEquals(expected, actual);
    }
}
