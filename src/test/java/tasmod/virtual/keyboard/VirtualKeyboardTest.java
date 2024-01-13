package tasmod.virtual.keyboard;

import com.minecrafttas.tasmod.virtual.VirtualKey2;
import com.minecrafttas.tasmod.virtual.VirtualKeyboard2;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class VirtualKeyboardTest {

    @Test
    void testEmptyConstructor(){
        VirtualKeyboard2 test = new VirtualKeyboard2();
        assertTrue(test.getPressedKeys().isEmpty());
        assertTrue(test.getCharList().isEmpty());
        assertTrue(test.isParent());
    }

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

    @Test
    void testSetPressedByKeycode(){
        VirtualKeyboard2 test = new VirtualKeyboard2();
        test.setPressed(VirtualKey2.W.getKeycode(), true);

        assertIterableEquals(Arrays.asList(VirtualKey2.W.getKeycode()), test.getPressedKeys());
        assertTrue(test.isParent());
    }

    @Test
    void testSetPressedByKeyname(){
        VirtualKeyboard2 test = new VirtualKeyboard2();
        test.setPressed("W", true);

        assertIterableEquals(Arrays.asList(VirtualKey2.W.getKeycode()), test.getPressedKeys());
        assertTrue(test.isParent());
    }

    @Test
    void testSetUnPressedByKeycode(){
        Set<Integer> testKeycodeSet = new HashSet<>();
        testKeycodeSet.add(VirtualKey2.W.getKeycode());
        testKeycodeSet.add(VirtualKey2.S.getKeycode());
        VirtualKeyboard2 test = new VirtualKeyboard2(testKeycodeSet, new ArrayList<>(), null);
        test.setPressed(VirtualKey2.W.getKeycode(), false);

        assertIterableEquals(Arrays.asList(VirtualKey2.S.getKeycode()), test.getPressedKeys());
    }

    @Test
    void testSetUnPressedByKeyname(){
        Set<Integer> testKeycodeSet = new HashSet<>();
        testKeycodeSet.add(VirtualKey2.W.getKeycode());
        testKeycodeSet.add(VirtualKey2.S.getKeycode());
        VirtualKeyboard2 test = new VirtualKeyboard2(testKeycodeSet, new ArrayList<>(), null);
        test.setPressed("S", false);

        assertIterableEquals(Arrays.asList(VirtualKey2.W.getKeycode()), test.getPressedKeys());
    }

    @Test
    void addCharacter(){
        VirtualKeyboard2 test = new VirtualKeyboard2();
        test.addChar('w');

        assertIterableEquals(Arrays.asList('w'), test.getCharList());
    }

    @Test
    void clearCharacters(){
        List<Character> testCharList = new ArrayList<>();
        testCharList.add('w');
        testCharList.add('s');
        VirtualKeyboard2 test = new VirtualKeyboard2(new HashSet<>(), testCharList);
        test.clearCharList();

        assertTrue(test.getCharList().isEmpty());
    }

    @Test
    void testToString(){
        Set<Integer> testKeycodeSet = new HashSet<>();
        testKeycodeSet.add(VirtualKey2.W.getKeycode());
        testKeycodeSet.add(VirtualKey2.S.getKeycode());

        List<Character> testCharList = new ArrayList<>();
        testCharList.add('w');
        testCharList.add('s');

        VirtualKeyboard2 test = new VirtualKeyboard2(testKeycodeSet, testCharList, null);

        assertEquals("W,S;ws", test.toString());
    }
}
