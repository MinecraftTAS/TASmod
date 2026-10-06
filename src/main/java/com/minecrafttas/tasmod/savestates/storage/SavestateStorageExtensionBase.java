package com.minecrafttas.tasmod.savestates.storage;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.minecrafttas.mctcommon.registry.Registerable;
import com.minecrafttas.tasmod.TASmod;
import com.minecrafttas.tasmod.util.JsonUtils;

import net.minecraft.server.MinecraftServer;

public abstract class SavestateStorageExtensionBase implements Registerable {
	protected final Logger logger = TASmod.LOGGER;
	protected final Gson gsonInstance;

	public final Path fileName;

	public SavestateStorageExtensionBase(String filename) {
		this(filename, JsonUtils.getGsonInstance());
	}

	public SavestateStorageExtensionBase(String fileName, Gson gson) {
		this.fileName = Paths.get(fileName);
		this.gsonInstance = gson;
	}

	public abstract JsonObject onSavestate(MinecraftServer server, JsonObject dataToSave);

	public void onLoadstatePre(MinecraftServer server, JsonObject loadedData) {
	}

	public void onLoadstatePost(MinecraftServer server, JsonObject loadedData) {
	}

	public void onLoadstateComplete(MinecraftServer server, JsonObject loadedData) {
	}
}
