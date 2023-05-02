package com.minecrafttas.tasmod.savestates.server.files;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.InvalidPropertiesFormatException;
import java.util.Properties;

public class SavestateDataFile {
	
	public enum DataValues {
		INDEX("currentIndex"),
		Name("savestateName"),
		SEED("ktrngSeed");
		
		
		private String configname;
		
		private DataValues(String configname) {
			this.configname=configname;
		}
		
		public String getConfigName() {
			return configname;
		}
	}
	
	Properties properties = new Properties();
	
	public void set(DataValues key, String val) {
		properties.setProperty(key.getConfigName(), val);
	}
	
	public String get(DataValues key) {
		return properties.getProperty(key.getConfigName());
	}
	
	public void save(File file) {
		try {
			Writer writer = new FileWriter(file);
			properties.store(writer, "Data for this savestate from TASmod");
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void load(File file) {
		try {
			Reader reader = new FileReader(file);
			properties.load(reader);
			reader.close();
		} catch (InvalidPropertiesFormatException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
