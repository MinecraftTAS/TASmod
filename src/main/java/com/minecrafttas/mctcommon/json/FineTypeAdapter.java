package com.minecrafttas.mctcommon.json;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.minecrafttas.mctcommon.json.FineField.FineDeserializer;
import com.minecrafttas.mctcommon.json.FineField.FineMode;
import com.minecrafttas.mctcommon.json.FineField.FineSerializer;
import com.minecrafttas.tasmod.util.JsonUtils;

public abstract class FineTypeAdapter {

	private final List<FineField> fineFields = new ArrayList<>();

	private final List<FineField> additionalFields = new ArrayList<>();

	public void register(String name, FineMode mode) {
		register(new FineField(name, mode));
	}

	public void register(String name, FineSerializer customSerializer, FineDeserializer customDeserializer) {
		register(new FineField(name, customSerializer, customDeserializer));
	}

	public void register(FineField field) {
		this.fineFields.add(field);
	}

	public void registerAdditional(String name, FineSerializer customSerializer, FineDeserializer customDeserializer) {
		this.additionalFields.add(new FineField(name, customSerializer, customDeserializer));
	}

	public JsonElement serialize(Object obj, FineGson fineJson) throws RuntimeException {
		JsonObject out = new JsonObject();

		Class<?> clazz = obj.getClass();
		List<Field> clazzFields = getFieldList(clazz);

		if (clazzFields.size() != fineFields.size()) {
			throw new RuntimeException(String.format("Can't serialize %s: The field count differs: %s!=%s", clazz.getSimpleName(), clazzFields.size(), fineFields.size()));
		}

		for (int i = 0; i < clazzFields.size(); i++) {
			Field field = clazzFields.get(i);
			FineField fineField = fineFields.get(i);

			Object fieldValue = null;
			try {
				field.setAccessible(true);
				fieldValue = field.get(obj);
			} catch (IllegalArgumentException | IllegalAccessException e) {
				e.printStackTrace();
				continue;
			}

			String fieldName = fineField.getName();
			System.out.println(String.format("[%s] Serializing field %s (%s)", obj.getClass(), field.getName(), fieldName));
			switch (fineField.getMode()) {
				case CUSTOM:
					out.add(fieldName, fineField.serialize(fieldValue));
					break;
				case FINE:
					out.add(fieldName, fineJson.serialize(fieldValue));
					break;
				case GSON:
					out.add(fieldName, fineJson.getGsonInstance().toJsonTree(fieldValue));
					break;

				case EXCLUDED:
				default:
					break;
			}
		}

		for (FineField fineField : additionalFields) {
			out.add(fineField.getName(), fineField.serialize(null));
		}
		return out;
	}

	public Object deserialize(JsonElement element, Class<?> clazz, FineGson fineJson) {
		List<Field> clazzFields = getFieldList(clazz);

		if (clazzFields.size() != fineFields.size()) {
			throw new RuntimeException(String.format("Can't deserialize %s: The field count differs: %s!=%s", clazz.getSimpleName(), clazzFields.size(), fineFields.size()));
		}

		JsonObject elementObj = element.getAsJsonObject();

		Object out = constructNew(clazz);

		for (int i = 0; i < clazzFields.size(); i++) {
			Field field = clazzFields.get(i);
			field.setAccessible(true);
			FineField fineField = fineFields.get(i);
			String fieldName = fineField.getName();
			JsonElement fieldElement = elementObj.get(fieldName);

			Object fieldValue = null;
			switch (fineField.getMode()) {
				case CUSTOM:
					fieldValue = fineField.deserialize(fieldElement);
					break;
				case FINE:
					fieldValue = fineJson.deserialize(fieldElement, field.getType());
					break;
				case GSON:
					fieldValue = fineJson.getGsonInstance().fromJson(fieldElement, field.getType());
					break;

				case EXCLUDED:
				default:
					break;
			}
			if (fieldValue != null) {
				try {
					field.set(out, fieldValue);
				} catch (IllegalArgumentException | IllegalAccessException e) {
					e.printStackTrace();
				}
			}
		}
		return out;
	}

	protected Object constructNew(Class<?> clazz) {
		return JsonUtils.constructNew(clazz);
	}

	public static List<Field> getFieldList(Class<?> clazz) {
		List<Field> fields = new ArrayList<>();
		for (Field field : clazz.getDeclaredFields()) {
			if (!field.getName().startsWith("this"))
				fields.add(field);
		}

		Class<?> superclazz = clazz.getSuperclass();
		if (superclazz != Object.class)
			fields.addAll(getFieldList(superclazz));

		return fields;
	}
}
