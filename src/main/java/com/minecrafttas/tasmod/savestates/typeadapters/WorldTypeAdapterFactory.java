package com.minecrafttas.tasmod.savestates.typeadapters;

import java.io.IOException;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import com.minecrafttas.tasmod.TASmod;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

public class WorldTypeAdapterFactory implements TypeAdapterFactory {

	@SuppressWarnings("unchecked")
	@Override
	public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
		Class<T> rawType = (Class<T>) type.getRawType();
		if (!World.class.isAssignableFrom(rawType))
			return null;

		return new TypeAdapter<T>() {

			public void write(JsonWriter out, T value) throws IOException {
				if (value == null) {
					out.nullValue();
				} else {
					if (TASmod.getServerInstance() == null)
						return;
					MinecraftServer server = TASmod.getServerInstance();
					WorldServer world = (WorldServer) rawType.cast(value);

					Integer index = findIndex(server.worlds, world);
					if (index == null) {
						out.nullValue();
						return;
					}
					out.value(index);
				}
			}

			private Integer findIndex(WorldServer[] worlds, WorldServer world) {
				int index = 0;
				for (World otherWorld : worlds) {
					if (otherWorld.equals(world))
						return index;
					index++;
				}
				return null;
			}

			public T read(JsonReader reader) throws IOException {
				if (reader.peek() == JsonToken.NULL) {
					reader.nextNull();
					return null;
				} else {
					if (TASmod.getServerInstance() == null)
						return null;
					int worldId = Integer.parseInt(reader.nextString());
					MinecraftServer server = TASmod.getServerInstance();

					return (T) server.worlds[worldId];
				}
			}
		};
	}
}
