package mctcommon.json;

import static org.junit.jupiter.api.Assertions.assertIterableEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.minecrafttas.mctcommon.json.FineTypeAdapterGenerator;

class FineTypeAdapterGeneratorTest {

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

	@Test
	void testGenerateMulti() {
		FineTypeAdapterGenerator generator = new FineTypeAdapterGenerator("GeneratedTypeAdapterTest", "typeadapter");
		generator.addClass(TestClass.class);
		List<String> actual = generator.generateMulti();
		List<String> expected = Arrays.asList(String.format("package typeadapter;\n"
				+ "\n"
				+ "import com.minecrafttas.mctcommon.json.FineField.FineMode;\n"
				+ "import com.minecrafttas.mctcommon.json.FineMultiTarget;\n"
				+ "import com.minecrafttas.mctcommon.json.FineTarget;\n"
				+ "import com.minecrafttas.mctcommon.json.FineTypeAdapter;\n"
				+ "\n"
				+ "@FineMultiTarget\n"
				+ "public class GeneratedTypeAdapterTest {\n"
				+ "\n"
				+ "	@FineTarget(mctcommon.json.FineTypeAdapterGeneratorTest.TestSuperClass.class)\n"
				+ "	public static class TestSuperClassTypeAdapter extends FineTypeAdapter {\n"
				+ "	\n"
				+ "		public TestSuperClassTypeAdapter() {\n"
				+ "			register(\"bot\", FineMode.FINE);\n"
				+ "			register(\"evil\", FineMode.FINE);\n"
				+ "		}\n"
				+ "	}\n"
				+ "	\n"
				+ "	@FineTarget(mctcommon.json.FineTypeAdapterGeneratorTest.TestClass.class)\n"
				+ "	public static class TestClassTypeAdapter extends FineTypeAdapter {\n"
				+ "	\n"
				+ "		public TestClassTypeAdapter() {\n"
				+ "			register(\"index\", FineMode.FINE);\n"
				+ "			register(\"lol\", FineMode.FINE);\n"
				+ "			register(\"subClass\", FineMode.FINE);\n"
				+ "			register(\"excluded\", FineMode.FINE);\n"
				+ "		}\n"
				+ "	}\n"
				+ "	\n"
				+ "}").split("\n"));
		assertIterableEquals(expected, actual);
	}

	@Test
	void testGenerateSingle() {
		FineTypeAdapterGenerator generator = new FineTypeAdapterGenerator("GeneratedTypeAdapterTest", "typeadapter");
		generator.addClass(TestSubClass.class);
		List<String> actual = generator.generate();
		System.out.println(String.join("\n", actual));
		List<String> expected = new ArrayList<>(Arrays.asList(String.format("package typeadapter;\n"
				+ "\n"
				+ "import com.minecrafttas.mctcommon.json.FineField.FineMode;\n"
				+ "import com.minecrafttas.mctcommon.json.FineMultiTarget;\n"
				+ "import com.minecrafttas.mctcommon.json.FineTarget;\n"
				+ "import com.minecrafttas.mctcommon.json.FineTypeAdapter;\n"
				+ "\n"
				+ "@FineTarget(mctcommon.json.FineTypeAdapterGeneratorTest.TestSubClass.class)\n"
				+ "public class TestSubClassTypeAdapter extends FineTypeAdapter {\n"
				+ "\n"
				+ "	public TestSubClassTypeAdapter() {\n"
				+ "		register(\"isWater\", FineMode.FINE);\n"
				+ "	}\n"
				+ "}\n"
				+ "\n").split("\n")));
		expected.add("");
		assertIterableEquals(expected, actual);
	}
}
