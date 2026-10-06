package com.minecrafttas.mctcommon.json;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.internal.ConstructorConstructor;
import com.google.gson.reflect.TypeToken;

/**
 * A fine grained gson replacement
 * 
 * @author Scribble
 */
public class FineGson {

	private TypeAdapterMap typeAdapters = new TypeAdapterMap();

	private final Gson gsonInstance;

	public FineGson(Gson gsonInstance) {
		this.gsonInstance = gsonInstance;
	}

	public void registerTypeAdapter(Class<?> type) {
		if (type.isAnnotationPresent(FineMultiTarget.class)) {
			List<Class<?>> classes = Arrays.asList(type.getDeclaredClasses());
			for (Class<?> clazz : classes) {
				registerTypeAdapter(clazz);
			}
		}
		if (type.isAnnotationPresent(FineTarget.class)) {
			if (!FineTypeAdapter.class.isAssignableFrom(type)) {
				throw new RuntimeException(String.format("Trying to register type adapter %s, but it doesn't extend FineTypeAdapter", type.getName()));
			}
			FineTarget fineTargetAnnotation = type.getAnnotation(FineTarget.class);
			Class<?> targetType = fineTargetAnnotation.value();
			FineTypeAdapter adapter;

			try {
				adapter = (FineTypeAdapter) type.newInstance();
			} catch (InstantiationException | IllegalAccessException e) {
				throw new RuntimeException(String.format("Failed to instantiate type adapter %s", type.getName()), e);
			}
			registerTypeAdapter(targetType, adapter);
		}
	}

	public void registerTypeAdapter(Class<?> type, FineTypeAdapter adapter) {
		typeAdapters.put(type, adapter);
	}

	public JsonElement serialize(Object obj) {
		if (obj == null)
			return JsonNull.INSTANCE;
		Class<?> type = obj.getClass();
		return serialize(obj, type);
	}

	public JsonElement serialize(Object obj, Class<?> type) {
		if (!typeAdapters.containsKey(type))
			return gsonInstance.toJsonTree(obj);

		FineTypeAdapter adapter = typeAdapters.get(type);
		JsonElement out = adapter.serialize(obj, this, type);

		Class<?> superclass = type.getSuperclass();
		if (superclass != Object.class) {
			JsonObject superObject = serialize(obj, superclass).getAsJsonObject();
			JsonObject jsonObj = out.getAsJsonObject();
			for (Entry<String, JsonElement> objects : superObject.entrySet()) {
				jsonObj.add(objects.getKey(), objects.getValue());
			}
			out = jsonObj;
		}

		return out;
	}

	public Object deserialize(JsonElement element, Class<?> type) {
		if (!typeAdapters.containsKey(type))
			return gsonInstance.fromJson(element, type);
		Object obj = constructNew(type);
		return deserialize(element, type, obj);
	}

	public Object deserialize(JsonElement element, Class<?> type, Object existing) {
		if (!typeAdapters.containsKey(type))
			return gsonInstance.fromJson(element, type);

		FineTypeAdapter adapter = typeAdapters.get(type);
		Object out = adapter.deserialize(element, type, this, existing);

		Class<?> superclass = type.getSuperclass();
		if (superclass != Object.class) {
			FineTypeAdapter superadapter = typeAdapters.get(superclass);
			out = superadapter.deserialize(element, superclass, this, out);
		}
		return out;
	}

	public Gson getGsonInstance() {
		return gsonInstance;
	}

	public Map<Class<?>, FineTypeAdapter> getTypeAdapters() {
		return typeAdapters;
	}

	private class TypeAdapterMap extends HashMap<Class<?>, FineTypeAdapter> {

		@Override
		public boolean containsKey(Object key) {
			return get(key) != null;
		}

		@Override
		public FineTypeAdapter get(Object key) {
			Class<?> classKey = (Class<?>) key;
			if (classKey.isAnonymousClass()) {
				for (FineTypeAdapter adapter : values()) {
					FineTarget fineTargetAnnotation = adapter.getClass().getAnnotation(FineTarget.class);
					if (fineTargetAnnotation == null)
						continue;
					Class<?> enclosingType = fineTargetAnnotation.enclosingclazz();
					Class<?> superType = fineTargetAnnotation.superclazz();
					if (classKey.getEnclosingClass() == enclosingType && classKey.getSuperclass() == superType)
						return adapter;
				}
				return null;
			}
			return super.get(key);
		}
	}

	public static <T> T constructNew(Class<T> clazz) {
		ConstructorConstructor constructor = new ConstructorConstructor(Collections.emptyMap(), true, Collections.emptyList());
		TypeToken<T> token = TypeToken.get(clazz);
		return constructor.get(token, true).construct();
	}
}
