package tasmod.virtual;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.minecrafttas.tasmod.virtual.VirtualCameraAngle;

public class VirtualCameraAngleTest {

	/**
	 * Test the empty constructor
	 */
	@Test
	void testEmptyConstructor() {
		VirtualCameraAngle actual = new VirtualCameraAngle();
		assertEquals(null, actual.getPitch());
		assertEquals(null, actual.getYaw());
		assertTrue(actual.isParent());
	}

	/**
	 * Test subtick constructor with premade pitch and value
	 */
	@Test
	void testSubtickConstructor() {
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
	void testUpdate() {
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
	 * Test updating a camera but pitch and yaw are null. The update should fail and
	 * pitch and yaw should still be null
	 */
	@Test
	void testUpdateWithNull() {
		float x = 1f;
		float y = 2f;

		VirtualCameraAngle actual = new VirtualCameraAngle();

		actual.update(x, y);

		assertEquals(null, actual.getPitch());
		assertEquals(null, actual.getYaw());

		VirtualCameraAngle actual2 = new VirtualCameraAngle(1f, null);
		actual2.update(x, y);

		assertEquals(null, actual.getPitch());
		assertEquals(null, actual.getYaw());
	}

	/**
	 * Test setting the camera
	 */
	@Test
	void testSet() {
		VirtualCameraAngle actual = new VirtualCameraAngle();
		actual.set(1f, 2f);

		actual.update(1f, 2f);

		assertEquals(2f, actual.getPitch());
		assertEquals(4f, actual.getYaw());
	}

	/**
	 * Test getting all states
	 */
	@Test
	void testGetStates() {
		VirtualCameraAngle test = new VirtualCameraAngle();
		test.set(0f, 0f);
		test.update(1f, 1f);
		test.update(1f, 1f);
		test.update(1f, 1f);
		
		List<VirtualCameraAngle> actual = new ArrayList<>();
		
		// Test get states on a subtick, should result in an empty array
		VirtualCameraAngle test2 = new VirtualCameraAngle(0f, 0f);
		
		test2.getStates(actual);
		
		assertTrue(actual.isEmpty());
		
		actual.clear();
		
		test.getStates(actual);
		
		List<VirtualCameraAngle> expected = new ArrayList<>();
		
		expected.add(new VirtualCameraAngle(1f, 1f));
		expected.add(new VirtualCameraAngle(2f, 2f));
		expected.add(new VirtualCameraAngle(3f, 3f));
		
		assertIterableEquals(expected, actual);
	}

	/**
	 * Test copyfrom method
	 */
	@Test
	void copyFrom() {
		VirtualCameraAngle expected = new VirtualCameraAngle(0f, 0f, true);
		expected.update(1f, 2f);
		expected.update(3f, 4f);
		
		VirtualCameraAngle actual = new VirtualCameraAngle(0f, 0f, true);
		
		actual.copyFrom(expected);
		
		// Test pitch and yaw
		assertEquals(expected.getPitch(), actual.getPitch());
		assertEquals(expected.getYaw(), actual.getYaw());
		
		// Test subticks
		List<VirtualCameraAngle> expected2 = new ArrayList<>();
		expected2.add(new VirtualCameraAngle(1f, 2f));
		expected2.add(new VirtualCameraAngle(4f, 6f));
		
		assertIterableEquals(expected2, actual.getAll());
		// Test expected subticks being cleared
		
		assertTrue(expected.getSubticks().isEmpty());
	}
	
	/**
	 * Test clearing the camera angle
	 */
	@Test
	void testClear() {
		VirtualCameraAngle actual = new VirtualCameraAngle();
		actual.set(0f, 0f);
		actual.update(1f, 1f);
		actual.update(1f, 1f);
		actual.update(1f, 1f);
		
		actual.clear();
		
		assertNull(actual.getPitch());
		assertNull(actual.getYaw());
		assertTrue(actual.getSubticks().isEmpty());
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
	void testClone() {
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
	void testEquals() {
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
	void testNotEquals() {

		// Test pitch being different
		VirtualCameraAngle test = new VirtualCameraAngle(1f, 4f);
		VirtualCameraAngle test2 = new VirtualCameraAngle(3f, 4f);
		assertNotEquals(test, test2);

		// Test yaw being different
		test = new VirtualCameraAngle(1f, 2f);
		test2 = new VirtualCameraAngle(1f, 3f);
		assertNotEquals(test, test2);

		// Test pitch being null
		test = new VirtualCameraAngle(null, 2f);
		test2 = new VirtualCameraAngle(1f, 2f);
		assertNotEquals(test, test2);

		// Test yaw being null
		test = new VirtualCameraAngle(1f, null);
		test2 = new VirtualCameraAngle(1f, 2f);
		assertNotEquals(test, test2);

		// Test mismatched types
		assertNotEquals(test, new Object());
	}
}
