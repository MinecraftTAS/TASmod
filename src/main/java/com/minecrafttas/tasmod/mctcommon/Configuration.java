package com.minecrafttas.tasmod.mctcommon;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import com.minecrafttas.tasmod.mctcommon.ConfigurationRegistry.ConfigOptions;
import com.minecrafttas.tasmod.mctcommon.file.AbstractDataFile;

/**
 * A <i>very</i> simple configuration class
 * 
 * @author Scribble
 */

public class Configuration extends AbstractDataFile {

	private ConfigurationRegistry registry;

	public Configuration(String comment, Path configFile, ConfigurationRegistry registry) {
		super(configFile, "config", comment);
		this.registry = registry;
	}

	@Override
	public void load() {

		if (Files.exists(file)) {
			String in = null;
			try {
				in = readFile(file);
			} catch (IOException e) {
				MCTCommon.LOGGER.catching(e);
				return;
			}

			if (in.startsWith("<?xml")) {
				MCTCommon.LOGGER.warn("Converting xml config to json");
				loadFromXML();
				return;
			}

			loadFromJson(file);
		}

		if (properties == null || !Files.exists(file)) {
			properties = generateDefault();
			saveToJson();
		}
	}

	@Override
	public void save() {
		super.saveToJson();
	}

	/**
	 * Generates the default property list from the values provided in {@link #registry}
	 * @return The default property list
	 */
	public Properties generateDefault() {
		Properties newProperties = new Properties();
		registry.getConfigRegistry().forEach((configOption) -> {
			newProperties.put(configOption.getConfigKey(), configOption.getDefaultValue());
		});
		return newProperties;
	}

	public String get(ConfigOptions configOption) {
		return properties.getProperty(configOption.getConfigKey(), configOption.getDefaultValue());
	}

	public int getInt(ConfigOptions configOption) {
		// TODO Add config exception or something... NumberFormatExceptions all around...
		return Integer.parseInt(get(configOption));
	}

	public boolean getBoolean(ConfigOptions configOption) {
		return Boolean.parseBoolean(get(configOption));
	}

	public boolean has(ConfigOptions configOption) {
		return properties.contains(configOption.getConfigKey());
	}

	public void set(ConfigOptions configOption, String value) {
		if (properties == null) {
			throw new NullPointerException("Config needs to be loaded first, before trying to set a value");
		}
		properties.setProperty(configOption.getConfigKey(), value);
		save();
	}

	public void set(ConfigOptions configOption, int value) {
		String val = Integer.toString(value);
		set(configOption, val);
	}

	public void set(ConfigOptions configOption, boolean value) {
		String val = Boolean.toString(value);
		set(configOption, val);
	}

	public void reset(ConfigOptions configOption) {
		set(configOption, configOption.getDefaultValue());
	}

	public void delete(ConfigOptions configOption) {
		properties.remove(configOption);
		saveToJson();
	}
}
