package com.minecrafttas.mctcommon.json;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FineTypeAdapterGenerator {

	protected final String className;

	protected final String packagge;

	protected final List<String> importLines = new ArrayList<>();
	protected final Set<Class<?>> classList = new LinkedHashSet<>();
	protected final Map<String, Integer> duplicateClassNameList = new HashMap<>();

	public FineTypeAdapterGenerator(String className, String packagge) {
		this.className = className;
		this.packagge = packagge;
		fillDefaultImports();
	}

	protected void fillDefaultImports() {
		importLines.add("import com.minecrafttas.mctcommon.json.FineField.FineMode;");
		importLines.add("import com.minecrafttas.mctcommon.json.FineMultiTarget;");
		importLines.add("import com.minecrafttas.mctcommon.json.FineTarget;");
		importLines.add("import com.minecrafttas.mctcommon.json.FineTypeAdapter;");
		importLines.add("");
	}

	public List<String> generate() {
		List<String> out = new ArrayList<>();
		out.add(String.format("package %s;", packagge));
		out.add("");
		out.addAll(importLines);
		for (Class<?> clazz : classList) {
			out.addAll(buildClass(clazz, false));
		}
		return out;
	}

	public List<String> generateMulti() {
		List<String> out = new ArrayList<>();
		out.add(String.format("package %s;", packagge));
		out.add("");
		out.addAll(importLines);
		out.add("@FineMultiTarget");
		out.add(String.format("public class %s {", className));
		out.add("");
		for (Class<?> clazz : classList) {
			List<String> classLines = buildClass(clazz, true);
			classLines.forEach(e1 -> out.add("\t" + e1));
		}
		out.add("}");
		return out;
	}

	public void addClass(Class<?>... classes) {
		for (Class<?> clazz : classes) {
			addClass(clazz);
		}
	}

	public <T> void addClass(T object) {
		Class<?> clazz = object.getClass();
		addClass(clazz);
	}

	public void addClass(Class<?> clazz) {
		if (classList.contains(clazz))
			return;

		if (clazz.isAnonymousClass()) {
			return;
		}

		Class<?> superclazz = clazz.getSuperclass();
		if (superclazz != Object.class)
			addClass(superclazz);

		classList.add(clazz);
	}

	private List<String> buildClass(Class<?> clazz, boolean isStatic) {
		List<String> out = new ArrayList<>();

		String clazzName = clazz.getSimpleName();
		List<Field> fields = FineTypeAdapter.getFieldList(clazz);

		Integer duplicateCount = duplicateClassNameList.getOrDefault(clazzName, 0);
		duplicateCount++;
		duplicateClassNameList.put(clazzName, duplicateCount);

		String typeAdapterName = duplicateCount == 1 ? clazzName : clazzName + duplicateCount;

		out.addAll(splitNewLine(String.format(""
				+ "@FineTarget(%s.class)\n"
				+ "public %sclass %sTypeAdapter extends FineTypeAdapter {\n"
				+ "\n"
				+ "\tpublic %sTypeAdapter() {", clazz.getName().replace("$", "."), isStatic ? "static " : "", typeAdapterName, typeAdapterName)));

		for (Field field : fields) {
			out.add(String.format("\t\tregister(\"%s\", FineMode.FINE);", field.getName()));
		}

		out.add("\t}");
		out.add("}");
		out.add("");
		return out;
	}

	protected List<String> splitNewLine(String text) {
		return new ArrayList<>(Arrays.asList(text.split("\n")));
	}
}
