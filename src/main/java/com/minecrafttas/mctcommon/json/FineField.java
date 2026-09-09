package com.minecrafttas.mctcommon.json;

import com.google.gson.JsonElement;

public class FineField {

	private final String name;
	private final FineMode mode;
	private final FineSerializer serializer;
	private final FineDeserializer deserializer;

	public FineField(String name, FineMode mode) {
		this.name = name;
		this.mode = mode;
		this.serializer = null;
		this.deserializer = null;
	}

	public FineField(String name, FineSerializer serializer, FineDeserializer deserializer) {
		this.name = name;
		this.mode = FineMode.CUSTOM;
		this.serializer = serializer;
		this.deserializer = deserializer;
	}

	public static enum FineMode {
		FINE,
		EXCLUDED,
		GSON,
		CUSTOM;
	}

	public String getName() {
		return name;
	}

	public FineMode getMode() {
		return mode;
	}

	public JsonElement serialize(Object obj) {
		return serializer.serialize(obj);
	}

	public Object deserialize(JsonElement element) {
		return deserializer.deserialize(element);
	}

	@FunctionalInterface
	public static interface FineSerializer {
		public JsonElement serialize(Object obj);
	}

	@FunctionalInterface
	public static interface FineDeserializer {
		public Object deserialize(JsonElement element);
	}
}
