package com.minecrafttas.tasmod.savestates.storage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;

import com.google.gson.JsonObject;
import com.minecrafttas.tasmod.mctcommon.registry.AbstractRegistry;
import com.minecrafttas.tasmod.TASmod;
import com.minecrafttas.tasmod.events.EventSavestate.EventServerLoadstate;
import com.minecrafttas.tasmod.events.EventSavestate.EventServerSavestate;
import com.minecrafttas.tasmod.savestates.SavestateIndexer;
import com.minecrafttas.tasmod.savestates.SavestateIndexer.SavestatePaths;
import com.minecrafttas.tasmod.savestates.exceptions.LoadstateException;
import com.minecrafttas.tasmod.savestates.exceptions.SavestateException;
import com.minecrafttas.tasmod.util.JsonUtils;

import net.minecraft.server.MinecraftServer;

public class SavestateStorageExtensionRegistry extends AbstractRegistry<SavestateStorageExtensionBase> implements EventServerSavestate, EventServerLoadstate {

	public SavestateStorageExtensionRegistry() {
		super("SAVESTATESTORAGE_REGISTRY", new LinkedHashMap<>());
	}

	@Override
	public void onServerSavestate(MinecraftServer server, SavestatePaths paths) {
		Path storageDir = paths.getSourceFolder().resolve(SavestateIndexer.savestateDataDir);
		if (!Files.exists(storageDir)) {
			try {
				Files.createDirectory(storageDir);
			} catch (IOException e) {
				throw new SavestateException(e, "Can't create directory for savestate storage in savestate %s", paths.getSourceFolder().getFileName());
			}
		}

		for (SavestateStorageExtensionBase storage : REGISTRY.values()) {
			Path dataPath = storageDir.resolve(storage.fileName);
			JsonObject dataToSave = storage.onSavestate(server, new JsonObject());

			try {
				JsonUtils.saveJson(dataPath, dataToSave);
			} catch (IOException e) {
				throw new SavestateException(e, "Can't save %s from %s extension", storage.fileName, storage.getExtensionName());
			}
		}
	}

	@Override
	public void onServerLoadstate(MinecraftServer server, SavestatePaths paths) {
		Path storageDir = paths.getTargetFolder().resolve(SavestateIndexer.savestateDataDir);
		if (!Files.exists(storageDir)) {
			try {
				Files.createDirectory(storageDir);
			} catch (IOException e) {
				throw new LoadstateException(e, "Can't create directory for savestate storage in savestate %s", paths.getTargetFolder().getFileName());
			}
		}

		for (SavestateStorageExtensionBase storage : REGISTRY.values()) {
			Path dataPath = storageDir.resolve(storage.fileName);

			if (!Files.exists(dataPath)) {
				TASmod.LOGGER.warn("Could not load {} in {} extension", storage.fileName, storage.getExtensionName());
				return;
			}

			JsonObject loadedData;
			try {
				loadedData = JsonUtils.loadJson(dataPath);
			} catch (IOException e) {
				throw new LoadstateException(e, "Can't load %s in %s extension", storage.fileName, storage.getExtensionName());
			}

			storage.onLoadstate(server, loadedData);
		}
	}
}

