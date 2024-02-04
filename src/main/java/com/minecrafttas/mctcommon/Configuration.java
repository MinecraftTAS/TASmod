package com.minecrafttas.mctcommon;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.InvalidPropertiesFormatException;
import java.util.Map;
import java.util.Properties;


/**
 * A <i>very</i> simple configuration class
 * @author Scribble
 *
 */

public class Configuration {
	
	private File file;
	
	private Properties properties;

	private String comment;
	
	public Configuration(String comment, File configFile) {
		file = configFile;
		this.comment = comment;
		
		if(file.exists()) {
			properties = load();
		}
		if(properties == null || !file.exists()) {
			properties = generateDefault();
			save();
		}
	}
	
	public Properties load() {
		FileInputStream fis;
		Properties newProp = new Properties();
		try {
			fis = new FileInputStream(file);
			newProp.loadFromXML(fis);
			fis.close();
		} catch (InvalidPropertiesFormatException e) {
			MCTCommon.LOGGER.error("The config file could not be read", e);
			return null;
		} catch (FileNotFoundException e) {
			MCTCommon.LOGGER.warn("No config file found: {}", file);
			return null;
		} catch (IOException e) {
			MCTCommon.LOGGER.error("An error occured while reading the config", e);
			return null;
		}
		return newProp;
	}
	
	public void save() {
		save(file);
	}
	
	public void save(File file) {
		try {
			FileOutputStream fos = new FileOutputStream(file);
			properties.storeToXML(fos, comment, "UTF-8");
			fos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public Properties generateDefault() {
		Properties newProperties = new Properties();
		newProperties.putAll(ConfigOptions.getDefaultValues());
		return newProperties;
	}
	
	public String get(ConfigOptions configOption) {
		return properties.getProperty(configOption.configKey);
	}
	
	public int getInt(ConfigOptions configOption) {
		return Integer.parseInt(get(configOption));
	}
	
	public boolean getBoolean(ConfigOptions configOption) {
		return Boolean.parseBoolean(get(configOption));
	}
	
	public boolean has(ConfigOptions configOption) {
		return properties.contains(configOption.configKey);
	}
	
	public void set(ConfigOptions configOption, String value) {
		properties.setProperty(configOption.configKey, value);
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
		set(configOption, configOption.defaultValue);
	}
	
	public void delete(ConfigOptions configOption) {
		properties.remove(configOption);
		save();
	}
	
	public static enum ConfigOptions{
		FileToOpen("fileToOpen", ""),
		ServerConnection("serverConnection", "");
		
		private String configKey;
		private String defaultValue;
		
		private ConfigOptions(String configKey, String defaultValue) {
			this.configKey = configKey;
			this.defaultValue = defaultValue;
		}
		
		public static Map<String, String> getDefaultValues() {
			Map<String, String> out = new HashMap<>();
			for (ConfigOptions configthing : values()) {
				if(configthing.defaultValue!=null) {
					out.put(configthing.configKey, configthing.defaultValue);
				}
			}
			return out;
		}
	}
}
