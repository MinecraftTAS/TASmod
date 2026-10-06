package com.minecrafttas.tasmod.savestates.storage.builtin.entity;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.minecrafttas.tasmod.savestates.storage.builtin.EntityStorage.EntitySubStorage;

import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.server.MinecraftServer;

public class CreeperDetonateSubStorage implements EntitySubStorage {

	@Override
	public String getPropertyName() {
		return "creeperDetonate";
	}

	@Override
	public Class<? extends Entity> getEntityClass() {
		return EntityCreeper.class;
	}

	@Override
	public JsonObject onSavestate(MinecraftServer server, Entity entity, JsonObject dataToSave, Gson gsonInstance) {
		EntityCreeper creeper = (EntityCreeper) entity;
		dataToSave.addProperty("timeSinceIgnited", creeper.timeSinceIgnited);
		dataToSave.addProperty("droppedSkulls", creeper.droppedSkulls);
		dataToSave.addProperty("exploding", creeper.getCreeperState());
		return dataToSave;
	}

	@Override
	public Entity onLoadstatePost(MinecraftServer server, Entity entity, JsonObject loadedData, Gson gsonInstance) {
		EntityCreeper creeper = (EntityCreeper) entity;
		JsonElement boomTime = loadedData.get("timeSinceIgnited");
		JsonElement dropped = loadedData.get("droppedSkulls");
		JsonElement exploding = loadedData.get("exploding");

		if (boomTime != null)
			creeper.timeSinceIgnited = boomTime.getAsInt();

		if (dropped != null)
			creeper.droppedSkulls = dropped.getAsInt();

		if (exploding != null)
			creeper.setCreeperState(exploding.getAsInt());

		return entity;
	}

}
