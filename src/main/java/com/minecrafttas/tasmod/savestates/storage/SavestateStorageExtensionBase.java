package com.minecrafttas.tasmod.savestates.storage;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.minecrafttas.tasmod.mctcommon.registry.Registerable;
import com.minecrafttas.tasmod.TASmod;
import com.minecrafttas.tasmod.util.JsonUtils;

import net.minecraft.server.MinecraftServer;

public abstract class SavestateStorageExtensionBase implements Registerable {
	protected final Logger logger = TASmod.LOGGER;
	protected final Gson json = JsonUtils.getJsonInstance();

	public final Path fileName;

	public SavestateStorageExtensionBase(String fileName) {
		this.fileName = Paths.get(fileName);
	}

	public abstract JsonObject onSavestate(MinecraftServer server, JsonObject dataToSave);

	public abstract void onLoadstate(MinecraftServer server, JsonObject loadedData);
}

