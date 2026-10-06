package com.minecrafttas.tasmod.savestates.storage.builtin.entity;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.minecrafttas.tasmod.savestates.storage.builtin.EntityStorage.EntitySubStorage;

import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntitySquid;
import net.minecraft.server.MinecraftServer;

/**
 * <p>Stores squid rotation in the savestates
 * <p>Did you know that the rotation of the squid can trigger an RNG call? Did you know Minecraft does not save this by default?<br>
 * Well now you know and it's annoying.
 * 
 * @author Scribble
 */
public class SquidRotationSubStorage implements EntitySubStorage {

	@Override
	public String getPropertyName() {
		return "squidRotationStorage";
	}

	@Override
	public Class<? extends Entity> getEntityClass() {
		return EntitySquid.class;
	}

	@Override
	public JsonObject onSavestate(MinecraftServer server, Entity entity, JsonObject dataToSave, Gson gsonInstance) {
		EntitySquid squid = (EntitySquid) entity;
		float rotation = squid.squidRotation;
		dataToSave.addProperty("rotation", Float.toString(rotation));
		return dataToSave;
	}

	@Override
	public Entity onLoadstatePost(MinecraftServer server, Entity entity, JsonObject loadedData, Gson gsonInstance) {
		EntitySquid squid = (EntitySquid) entity;
		JsonElement rotElement = loadedData.get("rotation");
		if (rotElement == null)
			return squid;
		float rotation = rotElement.getAsFloat();
		squid.squidRotation = rotation;
		return squid;
	}
}
