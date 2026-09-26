package com.minecrafttas.tasmod.mctcommon.file;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.InvalidPropertiesFormatException;
import java.util.Map.Entry;
import java.util.Properties;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.minecrafttas.tasmod.mctcommon.MCTCommon;

public abstract class AbstractDataFile {

	/**
	 * The save location of this data file
	 */
	protected final Path file;

	/**
	 * The name of this data file, used in logging
	 */
	protected final String name;
	/**
	 * The comment stored in the data file, to help recognize the file
	 */
	protected final String comment;

	/**
	 * The properties of this data file.
	 */
	protected Properties properties;

	/**
	 * Creates an abstract data file and creates it's directory if it doesn't exist
	 * @param file The {@link #file save location} of the data file
	 * @param name The {@link #name} of the data file, used in logging
	 * @param comment The {@link #comment} in the data file
	 */
	protected AbstractDataFile(Path file, String name, String comment) {
		this.file = file;
		this.name = name;
		this.comment = comment;
		this.properties = new Properties();
	}

	/**
	 * Creates the directory for the file if it doesn't exist
	 * @param directory The file to create the directory for
	 */
	public static void createDirectory(Path directory) throws IOException {
		/*
		 *  Test if the directory is a file,
		 *  but named like the target directory.
		 *  
		 *  For example, naming a file "tasfiles" and
		 *  putting it in the saves folder will succeed the "Files.exists" check,
		 *  but fail everywhere, where a directory is required...
		 *  
		 *  If this is the case, delete the file and create a directory instead.
		 */
		if (Files.exists(directory) && !Files.isDirectory(directory)) {
			Files.delete(directory);
		}

		Files.createDirectories(directory);
	}

	public void save() {
		this.saveToProperties();
	}

	public void save(Path file) {
		this.saveToProperties(file);
	}

	protected void saveToProperties() {
		this.saveToProperties(file);
	}

	protected void saveToProperties(Path file) {
		try {
			createDirectory(file.getParent());
		} catch (IOException e) {
			MCTCommon.LOGGER.catching(e);
		}
		try {
			OutputStream fos = Files.newOutputStream(file);
			properties.store(fos, comment);
			fos.close();
		} catch (IOException e) {
			MCTCommon.LOGGER.catching(e);
		}
	}

	/**
	 * Saves the {@link #properties} to the {@link #file} location
	 */
	protected void saveToXML() {
		this.saveToXML(file);
	}

	/**
	 * Saves the {@link #properties} to a specified file
	 * @param file The file to save the {@link #properties} to
	 */
	protected void saveToXML(Path file) {
		try {
			createDirectory(file.getParent());
		} catch (IOException e) {
			MCTCommon.LOGGER.catching(e);
		}
		try {
			OutputStream fos = Files.newOutputStream(file);
			properties.storeToXML(fos, comment, "UTF-8");
			fos.close();
		} catch (IOException e) {
			MCTCommon.LOGGER.catching(e);
		}
	}

	protected void saveToJson() {
		saveToJson(file);
	}

	protected void saveToJson(Path file) {
		try {
			createDirectory(file.getParent());
		} catch (IOException e) {
			MCTCommon.LOGGER.catching(e);
		}
		//@formatter:off
		Gson json = new GsonBuilder()
				.registerTypeAdapter(Properties.class, new PropertiesSerializer())
				.setPrettyPrinting()
				.create();
		//@formatter:on
		try {
			String element = json.toJson(properties);
			element = String.format("// %s\n", comment) + element;
			Files.write(file, element.getBytes());
		} catch (IOException e) {
			MCTCommon.LOGGER.catching(e);
		}
	}

	public void load() {
		loadFromProperties();
	}

	public void load(Path file) {
		loadFromProperties(file);
	}

	protected void loadFromProperties() {
		if (Files.exists(file)) {
			loadFromProperties(file);
		}
	}

	protected void loadFromProperties(Path file) {
		InputStream fis;
		Properties newProp = new Properties();
		try {
			fis = Files.newInputStream(file);
			newProp.load(fis);
			fis.close();
		} catch (InvalidPropertiesFormatException e) {
			MCTCommon.LOGGER.error("The {} file could not be read", name, e);
			return;
		} catch (FileNotFoundException e) {
			MCTCommon.LOGGER.warn("No {} file found: {}", name, file);
			return;
		} catch (IOException e) {
			MCTCommon.LOGGER.error("An error occured while reading the {} file", file, e);
			return;
		}
		this.properties = newProp;
	}

	/**
	 * Loads the xml {@link #file} into {@link #properties} if it exists
	 */
	protected void loadFromXML() {
		if (Files.exists(file)) {
			loadFromXML(file);
		}
	}

	/**
	 * @param file The xml file to load into {@link #properties}
	 */
	protected void loadFromXML(Path file) {
		InputStream fis;
		Properties newProp = new Properties();
		try {
			fis = Files.newInputStream(file);
			newProp.loadFromXML(fis);
			fis.close();
		} catch (InvalidPropertiesFormatException e) {
			MCTCommon.LOGGER.error("The {} file could not be read", name, e);
			return;
		} catch (FileNotFoundException e) {
			MCTCommon.LOGGER.warn("No {} file found: {}", name, file);
			return;
		} catch (IOException e) {
			MCTCommon.LOGGER.error("An error occured while reading the {} file", file, e);
			return;
		}
		this.properties = newProp;
	}

	protected void loadFromJson() {
		loadFromJson(file);
	}

	protected void loadFromJson(Path file) {
		//@formatter:off
		Gson json = new GsonBuilder()
				.registerTypeAdapter(Properties.class, new PropertiesDeserializer())
				.create();
		//@formatter:on

		String in;
		try {
			in = readFile(file);
		} catch (IOException e) {
			MCTCommon.LOGGER.catching(e);
			return;
		}

		properties = json.fromJson(in, Properties.class);
	}

	protected String readFile(Path file) throws IOException {
		return new String(Files.readAllBytes(file));
	}

	private class PropertiesSerializer implements JsonSerializer<Properties> {

		@Override
		public JsonElement serialize(Properties src, Type typeOfSrc, JsonSerializationContext context) {
			JsonObject obj = new JsonObject();
			src.forEach((key, val) -> {
				obj.addProperty((String) key, (String) val);
			});
			return obj;
		}
	}

	private class PropertiesDeserializer implements JsonDeserializer<Properties> {

		@Override
		public Properties deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
			Properties properties = new Properties();
			JsonObject obj = json.getAsJsonObject();
			for (Entry<String, JsonElement> elem : obj.entrySet()) {
				String key = elem.getKey();
				String val = elem.getValue().getAsString();
				properties.put(key, val);
			}
			return properties;
		}
	}
}
