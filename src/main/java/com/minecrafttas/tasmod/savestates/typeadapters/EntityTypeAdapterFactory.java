package com.minecrafttas.tasmod.savestates.typeadapters;

import java.io.IOException;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import com.minecrafttas.tasmod.TASmod;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.WorldServer;

public class EntityTypeAdapterFactory implements TypeAdapterFactory {

	@SuppressWarnings("unchecked")
	public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
		Class<T> rawType = (Class<T>) type.getRawType();
		if (!Entity.class.isAssignableFrom(rawType))
			return null;

		return new TypeAdapter<T>() {

			public void write(JsonWriter out, T value) throws IOException {
				if (value == null) {
					out.nullValue();
				} else {
					Entity entity = (Entity) rawType.cast(value);

					/*
					 * Instead of storing the players UUID, we will store the index of the playerlist.
					 * This is because when sharing savestates, the players UUID will be different and thus crash Minecraft.
					 * 
					 * TODO Make an option to map "Player0, Player1" etc. to specific playernames, so that this will be deterministic
					 */
					if (entity instanceof EntityPlayer) {
						MinecraftServer server = TASmod.getServerInstance();
						int counter = 0;
						for (EntityPlayerMP player : server.getPlayerList().getPlayers()) {
							if (player.getUniqueID().equals(entity.getUniqueID())) {
								out.value("Player" + counter);
								return;
							}
						}
					}

					out.value(entity.getUniqueID().toString());
				}
			}

			public T read(JsonReader reader) throws IOException {
				if (reader.peek() == JsonToken.NULL) {
					reader.nextNull();
					return null;
				} else {
					if (TASmod.getServerInstance() == null)
						return null;
					MinecraftServer server = TASmod.getServerInstance();
					String entityString = reader.nextString();
					Entity entity = null;

					/*
					 * Instead of storing the players UUID, we will store the index of the playerlist.
					 * This is because when sharing savestates, the players UUID will be different and thus crash Minecraft.
					 */
					Matcher matcher = Pattern.compile("Player(\\d+)").matcher(entityString);
					if (matcher.find()) {
						int counter = Integer.parseInt(matcher.group(1));
						entity = server.getPlayerList().getPlayers().get(counter);
					} else {
						UUID uuid = UUID.fromString(entityString);
						for (WorldServer world : server.worlds) {
							entity = world.getEntityFromUuid(uuid);
							if (entity != null)
								break;
						}
					}

					return (T) entity;
				}
			}
		};
	}
}
