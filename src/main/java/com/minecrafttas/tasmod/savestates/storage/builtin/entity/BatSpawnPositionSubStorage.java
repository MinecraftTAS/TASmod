package com.minecrafttas.tasmod.savestates.storage.builtin.entity;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.minecrafttas.tasmod.savestates.storage.builtin.EntityStorage.EntitySubStorage;

import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityBat;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

public class BatSpawnPositionSubStorage implements EntitySubStorage {

	@Override
	public String getPropertyName() {
		return "batSpawnPositionStorage";
	}

	@Override
	public Class<? extends Entity> getEntityClass() {
		return EntityBat.class;
	}

	@Override
	public JsonObject onSavestate(MinecraftServer server, Entity entity, JsonObject dataToSave, Gson gsonInstance) {
		EntityBat bat = (EntityBat) entity;
		BlockPos spawnPos = bat.spawnPosition;
		if (spawnPos == null)
			return dataToSave;

		dataToSave.addProperty("spawnPos", Long.toString(bat.spawnPosition.toLong()));
		return dataToSave;
	}

	@Override
	public Entity onLoadstatePost(MinecraftServer server, Entity entity, JsonObject loadedData, Gson gsonInstance) {
		EntityBat bat = (EntityBat) entity;
		JsonElement element = loadedData.get("spawnPos");
		if (element == null)
			return bat;
		BlockPos spawnPos = BlockPos.fromLong(element.getAsLong());
		bat.spawnPosition = spawnPos;
		return bat;
	}
}
