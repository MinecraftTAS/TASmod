package com.minecrafttas.mctcommon.json;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

/**
 * A fine grained gson replacement
 * 
 * @author Scribble
 */
public class FineGson {

	private Map<Class<?>, FineTypeAdapter> typeAdapters = new HashMap<>();

	private final Gson gsonInstance;

	public FineGson(Gson gsonInstance) {
		this.gsonInstance = gsonInstance;
	}

	public void registerTypeAdapter(Class<?> type, FineTypeAdapter adapter) {
		typeAdapters.put(type, adapter);
	}

	public JsonElement serialize(Object obj) {
		Class<?> type = obj.getClass();
		if (!typeAdapters.containsKey(type))
			return gsonInstance.toJsonTree(obj);

		FineTypeAdapter adapter = typeAdapters.get(type);
		return adapter.serialize(obj, this);
	}

	public Object deserialize(JsonElement element, Class<?> type) {
		if (!typeAdapters.containsKey(type))
			return gsonInstance.fromJson(element, type);

		FineTypeAdapter adapter = typeAdapters.get(type);

		return adapter.deserialize(element, type, this);
	}

	public Gson getGsonInstance() {
		return gsonInstance;
	}
}
