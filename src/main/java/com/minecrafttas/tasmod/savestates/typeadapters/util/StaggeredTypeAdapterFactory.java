package com.minecrafttas.tasmod.savestates.typeadapters.util;

import java.io.IOException;
import java.lang.reflect.Field;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

public class StaggeredTypeAdapterFactory implements TypeAdapterFactory {

	Class<?> clazz;
	OnWrite writeBehaviour;
	OnRead readBehaviour;

	public StaggeredTypeAdapterFactory(Class<?> clazz, OnWrite writeBehaviour, OnRead readBehaviour) {
		this.clazz = clazz;
		this.writeBehaviour = writeBehaviour;
		this.readBehaviour = readBehaviour;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
		Class<T> rawType = (Class<T>) type.getRawType();
		if (!clazz.isAssignableFrom(rawType))
			return null;
		return new TypeAdapter<T>() {

			@Override
			public void write(JsonWriter out, T value) throws IOException {

			}

			@Override
			public T read(JsonReader in) throws IOException {
				return null;
			}

		};
	}

	@FunctionalInterface
	public interface OnWrite {

		public void apply(JsonWriter out, Field value, Gson gson);
	}

	@FunctionalInterface
	public static interface OnRead {

		public <T> T apply(JsonReader in, T value, Gson gson);
	}
}
