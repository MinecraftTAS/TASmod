package com.minecrafttas.tasmod.savestates.storage.builtin.entity;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.minecrafttas.tasmod.savestates.storage.builtin.EntityStorage.EntitySubStorage;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAITasks;
import net.minecraft.server.MinecraftServer;

/**
 * Stores the {@link EntityAITasks#tickCount} in a savestate.
 * 
 * AI tasks are only updated every third tick and the tickCount keeps track of that.
 * 
 * @author Scribble
 */
public class LivingTickTimersSubStorage implements EntitySubStorage {

	@Override
	public String getPropertyName() {
		return "tickTimers";
	}

	@Override
	public Class<? extends Entity> getEntityClass() {
		return EntityLiving.class;
	}

	@Override
	public JsonObject onSavestate(MinecraftServer server, Entity entity, JsonObject dataToSave, Gson gsonInstance) {
		EntityLiving entityLiving = (EntityLiving) entity;
		EntityLivingBase entityLivingBase = (EntityLivingBase) entity;
		EntityAITasks tasks = entityLiving.tasks;
		EntityAITasks targetTasks = entityLiving.targetTasks;
		int tickCount = tasks.tickCount;
		int tickCountTarget = targetTasks.tickCount;
		int tickCountSound = entityLiving.livingSoundTime;
		int tickIdle = entityLivingBase.idleTime;
		dataToSave.addProperty("aitasks", tickCount);
		dataToSave.addProperty("aitargetTasks", tickCountTarget);
		dataToSave.addProperty("livingSoundTime", tickCountSound);
		dataToSave.addProperty("idleTime", tickIdle);
		return dataToSave;
	}

	@Override
	public Entity onLoadstatePost(MinecraftServer server, Entity entity, JsonObject loadedData, Gson gsonInstance) {

		JsonElement tickCount = loadedData.get("aitasks");
		JsonElement tickCountTarget = loadedData.get("aitargetTasks");
		JsonElement tickCountSound = loadedData.get("livingSoundTime");
		JsonElement tickCountIdle = loadedData.get("idleTime");

		EntityLiving entityLiving = (EntityLiving) entity;
		EntityLivingBase entityLivingBase = (EntityLivingBase) entity;
		EntityAITasks tasks = entityLiving.tasks;
		EntityAITasks targetTasks = entityLiving.targetTasks;
		if (tickCount != null)
			tasks.tickCount = tickCount.getAsInt();
		if (tickCountTarget != null)
			targetTasks.tickCount = tickCountTarget.getAsInt();
		if (tickCountSound != null)
			entityLiving.livingSoundTime = tickCountSound.getAsInt();
		if (tickCountIdle != null)
			entityLivingBase.idleTime = tickCountIdle.getAsInt();

		return entityLiving;
	}
}
