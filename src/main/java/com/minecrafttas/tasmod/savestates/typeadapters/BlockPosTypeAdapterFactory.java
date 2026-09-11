package com.minecrafttas.tasmod.savestates.typeadapters;

import java.io.IOException;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import net.minecraft.util.math.BlockPos;

public class BlockPosTypeAdapterFactory implements TypeAdapterFactory {

	@SuppressWarnings("unchecked")
	@Override
	public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
		Class<T> rawType = (Class<T>) type.getRawType();
		if (!BlockPos.class.isAssignableFrom(rawType))
			return null;
		return new TypeAdapter<T>() {
			@Override
			public void write(JsonWriter out, T value) throws IOException {
				if (value == null) {
					out.nullValue();
				}

				BlockPos val = (BlockPos) value;
				out.value(val.toLong());
			}

			@Override
			public T read(JsonReader in) throws IOException {
				if (!in.hasNext())
					return null;
				return (T) BlockPos.fromLong(in.nextLong());
			}
		};
	}
}
