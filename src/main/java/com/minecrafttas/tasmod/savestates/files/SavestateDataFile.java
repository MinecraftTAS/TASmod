package com.minecrafttas.tasmod.savestates.files;

import java.nio.file.Path;

import com.minecrafttas.tasmod.mctcommon.file.AbstractDataFile;

@Deprecated
public class SavestateDataFile extends AbstractDataFile {

	public SavestateDataFile(Path file) {
		super(file, "savestatedata", "Data for this savestate from TASmod");
	}

	public enum DataValues {
		INDEX("currentIndex"),
		NAME("savestateName"),
		SEED("ktrngSeed");

		private String configname;

		private DataValues(String configname) {
			this.configname = configname;
		}

		public String getConfigName() {
			return configname;
		}
	}

	public void set(DataValues key, String val) {
		properties.setProperty(key.getConfigName(), val);
	}

	public String get(DataValues key) {
		return properties.getProperty(key.getConfigName());
	}
}

