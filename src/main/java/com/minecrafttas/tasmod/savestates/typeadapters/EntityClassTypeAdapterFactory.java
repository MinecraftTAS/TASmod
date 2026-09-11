package com.minecrafttas.tasmod.savestates.typeadapters;

import java.io.IOException;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import net.minecraft.entity.Entity;

public class EntityClassTypeAdapterFactory implements TypeAdapterFactory {

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {

		Class<T> rawType = (Class<T>) type.getRawType();
		if (!Class.class.equals(rawType))
			return null;

		return new TypeAdapter<T>() {

			@Override
			public void write(JsonWriter out, T value) throws IOException {
				Class<? extends Entity> clazz = (Class) value;
				out.value(clazz.getTypeName());
			}

			@Override
			public T read(JsonReader in) throws IOException {
				if (!in.hasNext())
					return null;
				String jsonString = in.nextString();
				Class out = null;
				try {
					out = Class.forName(jsonString, false, getClass().getClassLoader()).asSubclass(Entity.class);
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
				return (T) out;
			}
		};
	}
}
