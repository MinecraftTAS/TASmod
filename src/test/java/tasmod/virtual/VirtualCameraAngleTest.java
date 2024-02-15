package tasmod.virtual;

import com.minecrafttas.tasmod.virtual.VirtualCameraAngle;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
public class VirtualCameraAngleTest {

    /**
     * Test the empty constructor
     */
    @Test
    void testEmptyConstructor(){
        VirtualCameraAngle actual = new VirtualCameraAngle();
        assertEquals(null, actual.getPitch());
        assertEquals(null, actual.getYaw());
        assertTrue(actual.isParent());
    }

    /**
     * Test subtick constructor with premade pitch and value
     */
    @Test
    void testSubtickConstructor(){
        float x = 1f;
        float y = 2f;

        VirtualCameraAngle actual = new VirtualCameraAngle(x, y);
        assertEquals(1f, actual.getPitch());
        assertEquals(2f, actual.getYaw());
        assertFalse(actual.isParent());
    }

    /**
     * Testing update function
     */
    @Test
    void testUpdate(){
        float x = 1f;
        float y = 2f;

        VirtualCameraAngle actual = new VirtualCameraAngle(0f, 0f, true);

        actual.update(x, y);

        assertEquals(1f, actual.getPitch());
        assertEquals(2f, actual.getYaw());
    }
    
    /**
     * Testing update function, but with a pitch higher/lower than 90/-90
     */
    @Test
    void testUpdateWithBadPitch() {
        VirtualCameraAngle actual = new VirtualCameraAngle(0f, 0f, true);

        actual.update(-100f, 0f);

        assertEquals(-90f, actual.getPitch());
        
        actual.update(360f, 0f);
        
        assertEquals(90f, actual.getPitch());
    }

    /**
     * Test copyfrom method
     */
    @Test
    void copyFrom() {
    	VirtualCameraAngle actual = new VirtualCameraAngle();
    	 actual.update(1f, 2f);
    	 actual.update(3f, 4f);
    }
    
    /**
     * Test the toString method <em>without</em> subticks
     */
    @Test
    void testToString() {
        float x = 1f;
        float y = 2f;

        VirtualCameraAngle actual = new VirtualCameraAngle(x, y);

        assertEquals("1.0;2.0", actual.toString());
    }
    
    /**
     * Test the toString method <em>with</em> subticks
     */
    @Test
    void testToStringSubticks() {

        VirtualCameraAngle actual = new VirtualCameraAngle(0f, 0f, true);
        actual.update(1f, 2f);
        actual.update(3f, 4f);
        actual.update(5f, 6f);
        
        assertEquals("1.0;2.0\n4.0;6.0\n9.0;12.0", actual.toString());
    }
    
    /**
     * Test cloning the camera angle
     */
    @Test
    void testClone(){
        float x = 1f;
        float y = 2f;

        VirtualCameraAngle test = new VirtualCameraAngle(x, y);

        VirtualCameraAngle actual = test.clone();

        assertEquals(1f, actual.getPitch());
        assertEquals(2f, actual.getYaw());
    }

    /**
     * Test equals
     */
    @Test
    void testEquals(){
        float x = 1f;
        float y = 2f;

        VirtualCameraAngle test = new VirtualCameraAngle(x, y);
        VirtualCameraAngle test2 = new VirtualCameraAngle(x, y);

        assertEquals(test, test2);
    }
    
    /**
     * Test where equals will fail
     */
    @Test
    void testNotEquals(){

        VirtualCameraAngle test = new VirtualCameraAngle(1f, 2f);
        VirtualCameraAngle test2 = new VirtualCameraAngle(3f, 4f);

        assertNotEquals(test, test2);
    }
}
