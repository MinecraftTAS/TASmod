package tasmod.virtual.keyboard;

import com.minecrafttas.tasmod.virtual.VirtualKey2;
import com.minecrafttas.tasmod.virtual.VirtualKeyboard2;
import com.minecrafttas.tasmod.virtual.VirtualKeyboardEvent;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Array;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

import static org.junit.jupiter.api.Assertions.*;

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
        VirtualKeyboard2 test = new VirtualKeyboard2(testKeycodeSet, new ArrayList<>(), null);
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
        VirtualKeyboard2 test = new VirtualKeyboard2(testKeycodeSet, new ArrayList<>(), null);
        test.setPressed("S", false);

        assertIterableEquals(Arrays.asList(VirtualKey2.W.getKeycode()), test.getPressedKeys());
    }

    /**
     * Test adding a character to the keyboard
     */
    @Test
    void addCharacter(){
        VirtualKeyboard2 test = new VirtualKeyboard2();
        test.addChar('w');

        assertIterableEquals(Arrays.asList('w'), test.getCharList());
    }

    /**
     * Test clearing all characters
     */
    @Test
    void clearCharacters(){
        List<Character> testCharList = new ArrayList<>();
        testCharList.add('w');
        testCharList.add('s');
        VirtualKeyboard2 test = new VirtualKeyboard2(new HashSet<>(), testCharList);
        test.clearCharList();

        assertTrue(test.getCharList().isEmpty());
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

        VirtualKeyboard2 test = new VirtualKeyboard2(testKeycodeSet, testCharList, null);
        VirtualKeyboard2 test2 = new VirtualKeyboard2(testKeycodeSet, new ArrayList<>(), null);

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

        VirtualKeyboard2 test = new VirtualKeyboard2(testKeycodeSet, testCharList, null);
        VirtualKeyboard2 test2 = new VirtualKeyboard2(testKeycodeSet, testCharList, null);
        
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

        VirtualKeyboard2 test = new VirtualKeyboard2(testKeycodeSet, testCharList, null);
        VirtualKeyboard2 test2 = new VirtualKeyboard2(testKeycodeSet, testCharList2, null);
        VirtualKeyboard2 test3 = new VirtualKeyboard2(testKeycodeSet, testCharList3, null);
        
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

        VirtualKeyboard2 test = new VirtualKeyboard2(testKeycodeSet, testCharList, null);
        VirtualKeyboard2 test2 = test.clone();
        
        assertEquals(test, test2);
    }

    /**
     * Test copy from method
     */
    @Test
    void testCopyFrom(){
        Set<Integer> testKeycodeSet = new HashSet<>();
        testKeycodeSet.add(VirtualKey2.W.getKeycode());
        testKeycodeSet.add(VirtualKey2.S.getKeycode());

        List<Character> testCharList = new ArrayList<>();
        testCharList.add('w');
        testCharList.add('s');
        VirtualKeyboard2 copyFrom = new VirtualKeyboard2(testKeycodeSet, testCharList);

        Set<Integer> testKeycodeSet2 = new HashSet<>();
        testKeycodeSet.add(VirtualKey2.A.getKeycode());
        testKeycodeSet.add(VirtualKey2.D.getKeycode());

        List<Character> testCharList2 = new ArrayList<>();
        testCharList.add('a');
        testCharList.add('d');
        VirtualKeyboard2 test = new VirtualKeyboard2(testKeycodeSet2, testCharList2);

        test.copyFrom(copyFrom);

        assertIterableEquals(test.getPressedKeys(), copyFrom.getPressedKeys());
        assertIterableEquals(test.getCharList(), copyFrom.getCharList());
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
        expected.add(new VirtualKeyboard2(Set.of(VirtualKey2.W.getKeycode()), List.of('w')));
        expected.add(new VirtualKeyboard2(Set.of(VirtualKey2.W.getKeycode(), VirtualKey2.A.getKeycode()), List.of('w', 'A')));

        assertIterableEquals(expected, test.getSubticks());
    }

    /**
     * Tests getDifference
     */
    @Test
    void testGetDifference(){
        VirtualKeyboard2 test = new VirtualKeyboard2(Set.of(VirtualKey2.W.getKeycode()), List.of('w'));
        VirtualKeyboard2 test2 = new VirtualKeyboard2(Set.of(VirtualKey2.W.getKeycode(), VirtualKey2.S.getKeycode()), List.of('S'));

        Queue<VirtualKeyboardEvent> actual = test.getDifference(test2);
        Queue<VirtualKeyboardEvent> expected = new ConcurrentLinkedQueue<>(List.of(new VirtualKeyboardEvent(VirtualKey2.S.getKeycode(), true, 'S')));

        assertIterableEquals(expected, actual);
    }
}
