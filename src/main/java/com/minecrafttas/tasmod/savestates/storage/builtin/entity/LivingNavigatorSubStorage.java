package com.minecrafttas.tasmod.savestates.storage.builtin.entity;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.minecrafttas.tasmod.savestates.storage.builtin.EntityStorage.EntitySubStorage;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.server.MinecraftServer;

public class LivingNavigatorSubStorage implements EntitySubStorage {

	@Override
	public String getPropertyName() {
		return "livingNavigator";
	}

	@Override
	public Class<? extends Entity> getEntityClass() {
		return EntityLiving.class;
	}

	@Override
	public JsonObject onSavestate(MinecraftServer server, Entity entity, JsonObject dataToSave, Gson gsonInstance) {
		EntityLiving entityLiving = (EntityLiving) entity;
		dataToSave.add("navigator", gsonInstance.toJsonTree(entityLiving.navigator));
		return dataToSave;
	}

	@Override
	public Entity onLoadstatePost(MinecraftServer server, Entity entity, JsonObject loadedData, Gson gsonInstance) {
		EntityLiving entityLiving = (EntityLiving) entity;
		JsonElement navigatorJson = loadedData.get("navigator");
		if (navigatorJson != null) {
			PathNavigate dummyNavigator = entityLiving.createNavigator(entityLiving.world);
			PathNavigate navigator = entityLiving.navigator;
			navigator = gsonInstance.fromJson(navigatorJson, dummyNavigator.getClass());
			navigator.entity = dummyNavigator.entity;
			navigator.world = dummyNavigator.world;
			navigator.pathSearchRange = dummyNavigator.pathSearchRange;
			navigator.pathFinder = dummyNavigator.pathFinder;
			navigator.nodeProcessor = dummyNavigator.nodeProcessor;

			entityLiving.navigator = navigator;
		}

		return entityLiving;
	}

}
