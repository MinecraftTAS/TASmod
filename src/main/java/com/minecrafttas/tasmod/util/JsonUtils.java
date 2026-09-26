package com.minecrafttas.tasmod.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

public class JsonUtils {

	public static Gson getJsonInstance() {
		return new GsonBuilder().setPrettyPrinting().create();
	}

	public static void saveJson(Path savePath, JsonObject data) throws IOException {
		saveJson(savePath, data, getJsonInstance());
	}

	public static void saveJson(Path savePath, JsonObject data, Gson jsonInstance) throws IOException {
		String out = jsonInstance.toJson(data);
		Files.write(savePath, out.getBytes());
	}

	public static JsonObject loadJson(Path loadPath) throws IOException {
		return loadJson(loadPath, getJsonInstance());
	}

	public static JsonObject loadJson(Path loadPath, Gson jsonInstance) throws IOException {
		return jsonInstance.fromJson(new String(Files.readAllBytes(loadPath)), JsonObject.class);
	}
}

