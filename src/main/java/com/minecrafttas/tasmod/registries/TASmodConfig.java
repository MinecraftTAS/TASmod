package com.minecrafttas.tasmod.registries;

import com.minecrafttas.tasmod.mctcommon.ConfigurationRegistry.ConfigOptions;

/**
 * The config options that will be stored in .minecraft/config/tasmod.cfg
 * 
 * @author Scribble
 */
public enum TASmodConfig implements ConfigOptions {
	FileToOpen("fileToOpen", ""),
	ServerConnection("serverConnection", ""),
	EnabledFileCommands("enabledFileCommands", "tasmod_desyncMonitor@v1, tasmod_label@v1, tasmod_options@v1"),
	UnpauseWarn("unpauseWarn", "true");

	private String configKey;
	private String defaultValue;

	private TASmodConfig(String configKey, String defaultValue) {
		this.configKey = configKey;
		this.defaultValue = defaultValue;
	}

	@Override
	public String getDefaultValue() {
		return defaultValue;
	}

	@Override
	public String getConfigKey() {
		return configKey;
	}

	@Override
	public String getExtensionName() {
		return "TASmodConfig";
	}
}

