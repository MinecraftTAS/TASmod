package com.minecrafttas.tasmod.savestates.storage.builtin;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import org.spongepowered.asm.mixin.Unique;

import com.google.common.base.Predicate;
import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.minecrafttas.tasmod.savestates.exceptions.SavestateException;
import com.minecrafttas.tasmod.savestates.storage.SavestateStorageExtensionBase;
import com.minecrafttas.tasmod.savestates.typeadapters.BlockPosTypeAdapterFactory;
import com.minecrafttas.tasmod.savestates.typeadapters.EntityClassTypeAdapterFactory;
import com.minecrafttas.tasmod.savestates.typeadapters.EntityLivingTypeAdapter;
import com.minecrafttas.tasmod.savestates.typeadapters.EntityTypeAdapterFactory;
import com.minecrafttas.tasmod.savestates.typeadapters.ItemTypeAdapter;
import com.minecrafttas.tasmod.savestates.typeadapters.PathNavigateTypeAdapterFactory;
import com.minecrafttas.tasmod.savestates.typeadapters.WorldTypeAdapterFactory;
import com.minecrafttas.tasmod.savestates.typeadapters.util.ClassExclusionStrategy;
import com.minecrafttas.tasmod.util.JsonUtils;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAITasks;
import net.minecraft.entity.ai.EntityAITasks.EntityAITaskEntry;
import net.minecraft.item.Item;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.pathfinding.PathNavigateClimber;
import net.minecraft.pathfinding.PathNavigateFlying;
import net.minecraft.pathfinding.PathNavigateGround;
import net.minecraft.pathfinding.PathNavigateSwimmer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.WorldServer;

public class EntityAiTaskStorage extends SavestateStorageExtensionBase {

	public EntityAiTaskStorage() {
		//@formatter:off
		super("entityAi.json", 
				new GsonBuilder()
				.setPrettyPrinting()
				.registerTypeAdapterFactory(
						new EntityTypeAdapterFactory()
						)
				.registerTypeAdapterFactory(
						new WorldTypeAdapterFactory()
						)
				.registerTypeAdapterFactory(
						new EntityClassTypeAdapterFactory()
						)
//				.registerTypeAdapter(EntityAINearestAttackableTarget.class, new EntityAINearestAttackableTargetTypeAdapter())
				.registerTypeAdapterFactory(
						new BlockPosTypeAdapterFactory()
						)
				.registerTypeAdapter(Item.class, new ItemTypeAdapter())
				.registerTypeAdapter(EntityLivingTypeAdapter.class, new EntityLivingTypeAdapter())
				.registerTypeAdapter(PathNavigateGround.class, new PathNavigateTypeAdapterFactory())
				.registerTypeAdapter(PathNavigateClimber.class, new PathNavigateTypeAdapterFactory())
				.registerTypeAdapter(PathNavigateFlying.class, new PathNavigateTypeAdapterFactory())
				.registerTypeAdapter(PathNavigateSwimmer.class, new PathNavigateTypeAdapterFactory())
				.setExclusionStrategies(new ExclusionStrategy() {
					
					@Override
					public boolean shouldSkipField(FieldAttributes f) {
						return f.getAnnotation(Unique.class) != null;
					}
					
					@Override
					public boolean shouldSkipClass(Class<?> c) {
						return false;
					}
				}, 
				new ClassExclusionStrategy(Predicate.class),
				new ClassExclusionStrategy(PathNavigate.class)
//				new ClassExclusionStrategy(EntityLivingBase.class, EntityAINearestAttackableTarget.class)
				)
				.create());
		//@formatter:on
	}

	@Override
	public String getExtensionName() {
		return "EntityAI";
	}

	@Override
	public JsonObject onSavestate(MinecraftServer server, JsonObject dataToSave) {
		for (WorldServer worldServer : server.worlds) {
			for (Entity entity : worldServer.loadedEntityList) {
				if (!(entity instanceof EntityLiving))
					continue;

				JsonObject mainObject = new JsonObject();
				EntityLiving entityLiving = (EntityLiving) entity;

				mainObject.addProperty("type", entity.getClass().getSimpleName());

				mainObject.addProperty("ticksExisted", entityLiving.ticksExisted);
				mainObject.add("revengeTarget", gsonInstance.toJsonTree(entityLiving.revengeTarget));
				mainObject.addProperty("revengeTimer", entityLiving.revengeTimer);
				mainObject.add("lastAttackedEntity", gsonInstance.toJsonTree(entityLiving.lastAttackedEntity));
				mainObject.add("attackTarget", gsonInstance.toJsonTree(entityLiving.attackTarget));

				EntityAITasks tasks = entityLiving.tasks;
				mainObject.add("tasks", serialiseAITasks(tasks));

				EntityAITasks targetTasks = entityLiving.targetTasks;
				mainObject.add("targetTasks", serialiseAITasks(targetTasks));

				dataToSave.add(entity.getUniqueID().toString(), mainObject);
			}
		}
		return dataToSave;
	}

	private JsonObject serialiseAITasks(EntityAITasks tasks) {
		JsonObject out = new JsonObject();
		out.add("taskEntries", serialiseAIEntries(tasks.taskEntries));
		out.add("executingTaskEntries", serialiseAIEntries(tasks.executingTaskEntries));
		return out;
	}

	private JsonArray serialiseAIEntries(Set<EntityAITaskEntry> taskEntries) {
		JsonArray serialisedEntries = new JsonArray();
		for (EntityAITasks.EntityAITaskEntry entry : taskEntries) {
			JsonObject jsonAiTaskEntry = new JsonObject();
			Class<? extends EntityAIBase> clazz = entry.action.getClass();
			jsonAiTaskEntry.addProperty("priority", entry.priority);
			jsonAiTaskEntry.addProperty("using", entry.using);
			jsonAiTaskEntry.addProperty("class", clazz.getName());

			try {
				System.out.println(clazz.getName());
				jsonAiTaskEntry.add("action", serializeAction(entry.action, clazz));
			} catch (Exception e) {
				throw new SavestateException(e, "Could not serialise AI Task %s", entry.action.getClass().getName());
			}

			serialisedEntries.add(jsonAiTaskEntry);
		}
		return serialisedEntries;
	}

	private JsonObject serializeAction(EntityAIBase action, Class<? extends EntityAIBase> clazz) {
		@SuppressWarnings("unchecked")
		Class<? extends EntityAIBase> superclazz = (Class<? extends EntityAIBase>) clazz.getSuperclass();

		JsonObject out = new JsonObject();

		if (superclazz != EntityAIBase.class)
			out = serializeAction(action, superclazz);

		return JsonUtils.mergeJsonObjects(out, gsonInstance.toJsonTree(action, clazz).getAsJsonObject());
	}

	@Override
	public void onLoadstatePost(MinecraftServer server, JsonObject loadedData) {
		for (Entry<String, JsonElement> elements : loadedData.entrySet()) {
			for (WorldServer worldServer : server.worlds) {
				EntityLiving entityLiving = (EntityLiving) worldServer.getEntityFromUuid(UUID.fromString(elements.getKey()));
				if (entityLiving == null)
					continue;

				JsonObject mainObject = elements.getValue().getAsJsonObject();

				entityLiving.ticksExisted = mainObject.get("ticksExisted").getAsInt();
				entityLiving.revengeTarget = (EntityLivingBase) gsonInstance.fromJson(mainObject.get("revengeTarget"), Entity.class);
				entityLiving.revengeTimer = mainObject.get("revengeTimer").getAsInt();
				entityLiving.lastAttackedEntity = (EntityLivingBase) gsonInstance.fromJson(mainObject.get("lastAttackedEntity"), Entity.class);
				entityLiving.attackTarget = (EntityLivingBase) gsonInstance.fromJson(mainObject.get("attackTarget"), Entity.class);

				deserialiseAITasks(entityLiving.tasks, mainObject.get("tasks").getAsJsonObject());
				deserialiseAITasks(entityLiving.targetTasks, mainObject.get("targetTasks").getAsJsonObject());
			}
		}
	}

	private void deserialiseAITasks(EntityAITasks tasks, JsonObject jsonTasks) {
		tasks.taskEntries = deserialiseAIEntries(tasks.taskEntries, tasks, jsonTasks.get("taskEntries").getAsJsonArray());
		tasks.executingTaskEntries = deserialiseAIEntries(tasks.executingTaskEntries, tasks, jsonTasks.get("executingTaskEntries").getAsJsonArray());
	}

	private Set<EntityAITaskEntry> deserialiseAIEntries(Set<EntityAITaskEntry> taskEntries, EntityAITasks parent, JsonArray jsonEntries) {
		Set<EntityAITaskEntry> out = new LinkedHashSet<>();

		for (JsonElement jsonEntryElement : jsonEntries) {
			JsonObject jsonEntry = jsonEntryElement.getAsJsonObject();

			// Deserialise the type of the AI action
			System.out.println(jsonEntry.get("class").getAsString());
			Class<? extends EntityAIBase> clazz;
			try {
				clazz = Class.forName(jsonEntry.get("class").getAsString(), false, getClass().getClassLoader()).asSubclass(EntityAIBase.class);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
				return out;
			}

			boolean createNew = true;
			boolean using = jsonEntry.get("using").getAsBoolean();
			for (EntityAITaskEntry vanillaEntry : taskEntries) {
				EntityAIBase vanillaAction = vanillaEntry.action;
				if (vanillaAction.getClass() != clazz) {
					continue;
				}

				createNew = false;
				vanillaEntry.action = deserialiseAction(vanillaAction, jsonEntry.get("action"));
				vanillaEntry.using = using;
				out.add(vanillaEntry);
				break;
			}

			if (!createNew) {
				continue;
			}
			int priority = jsonEntry.get("priority").getAsInt();

			EntityAIBase action = JsonUtils.constructNew(clazz);
			try {
				action = deserialiseAction(action, jsonEntry.get("action"));
			} catch (Exception e) {
				e.printStackTrace();
				continue;
			}
			EntityAITaskEntry entry = parent.new EntityAITaskEntry(priority, action);
			entry.using = using;
			out.add(entry);
		}
		return out;
	}

	private EntityAIBase deserialiseAction(EntityAIBase action, JsonElement json) {
		JsonObject jsonObject = json.getAsJsonObject();
		List<Field> fields = getFieldList(action.getClass());

		for (Field field : fields) {
			field.setAccessible(true);
			String fieldname = field.getName();
			Class<?> fieldClass = field.getType();

			JsonElement value = jsonObject.get(fieldname);

			if (value == null)
				continue;

			Object deserialised = gsonInstance.fromJson(value, fieldClass);
			try {
				field.set(action, deserialised);
			} catch (IllegalArgumentException | IllegalAccessException e) {
				e.printStackTrace();
			}
		}

		return action;
	}

	private List<Field> getFieldList(Class<? extends EntityAIBase> clazz) {
		@SuppressWarnings("unchecked")
		Class<? extends EntityAIBase> superclazz = (Class<? extends EntityAIBase>) clazz.getSuperclass();
		List<Field> out = new ArrayList<>();

		if (superclazz != EntityAIBase.class) {
			out.addAll(getFieldList(superclazz));
		}

		out.addAll(Arrays.asList(clazz.getDeclaredFields()));

		return out;
	}
}
