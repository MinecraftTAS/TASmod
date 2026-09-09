package mctcommon.json;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.minecrafttas.mctcommon.json.FineField.FineMode;
import com.minecrafttas.mctcommon.json.FineGson;
import com.minecrafttas.mctcommon.json.FineTypeAdapter;

class FineGsonTest {

	private class TestClass extends TestSuperClass {
		private int index = 7;
		private String lol = "Laughing out loud";
		private TestSubClass subClass = new TestSubClass();
		private boolean excluded = false;

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof TestClass) {
				TestClass other = (TestClass) obj;
				return index == other.index && lol.equals(other.lol) && subClass.equals(other.subClass) && excluded == other.excluded && bot == other.bot && evil.equals(evil);
			}
			return super.equals(obj);
		}
	}

	private class TestSubClass {
		private boolean isWater = true;

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof TestSubClass) {
				TestSubClass other = (TestSubClass) obj;
				return isWater == other.isWater;
			}
			return super.equals(obj);
		}
	}

	private static class TestSuperClass {
		protected long bot = 8999;
		protected String evil = "Squids are evil!";
	}

	private static class TestClassTypeAdapter extends FineTypeAdapter {
		public TestClassTypeAdapter() {
			register("index", FineMode.FINE);
			register("lol", FineMode.GSON);
			register("subClass", FineMode.FINE);
			register("excluded", FineMode.EXCLUDED);
			register("bot", FineMode.FINE);
			register("evil", obj -> new JsonPrimitive(((String) obj).toUpperCase()), element -> element.toString());
		}
	}

	private static class TestSubClassAdapter extends FineTypeAdapter {
		public TestSubClassAdapter() {
			register("isWater", FineMode.FINE);
		}
	}

	static FineGson fgson;

	@BeforeAll
	static void beforeAll() {
		fgson = new FineGson(new GsonBuilder().setPrettyPrinting().create());
		fgson.registerTypeAdapter(TestClass.class, new TestClassTypeAdapter());
		fgson.registerTypeAdapter(TestSubClass.class, new TestSubClassAdapter());
	}

	@Test
	void testSerialization() {
		TestClass test = new TestClass();
		JsonObject actual = fgson.serialize(test).getAsJsonObject();

		JsonObject expected = new JsonObject();
		expected.addProperty("index", 7);
		expected.addProperty("lol", "Laughing out loud");
		JsonObject expectedSubClass = new JsonObject();
		expectedSubClass.addProperty("isWater", true);
		expected.add("subClass", expectedSubClass);
		expected.addProperty("bot", 8999);
		expected.addProperty("evil", "SQUIDS ARE EVIL!");

		assertEquals(expected, actual);
	}

	@Test
	void testDeserialization() {
		JsonObject testObj = new JsonObject();
		testObj.addProperty("index", 5);
		testObj.addProperty("lol", "Laughing quietly");
		JsonObject testObjSubClass = new JsonObject();
		testObjSubClass.addProperty("isWater", false);
		testObj.add("subClass", testObjSubClass);
		testObj.addProperty("bot", 9000);
		testObj.addProperty("evil", "SQUIDS ARE STILL EVIL!");

		TestClass actual = (TestClass) fgson.deserialize(testObj, TestClass.class);

		TestClass expected = new TestClass();
		expected.index = 5;
		expected.lol = "Laughing quietly";
		expected.subClass.isWater = false;
		expected.bot = 9000;
		expected.evil = "SQUIDS ARE STILL EVIL!";

		assertEquals(expected, actual);
	}
}
