package com.minecrafttas.tasmod.mctcommon;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import com.minecrafttas.tasmod.mctcommon.ConfigurationRegistry.ConfigOptions;
import com.minecrafttas.tasmod.mctcommon.registry.AbstractRegistry;
import com.minecrafttas.tasmod.mctcommon.registry.Registerable;

public class ConfigurationRegistry extends AbstractRegistry<ConfigOptions> {

	protected final List<ConfigOptions> configRegistry = new ArrayList<>();

	public ConfigurationRegistry() {
		super("Configuration", new LinkedHashMap<>());
	}

	@Override
	public void register(ConfigOptions registryObject) {
		if (registryObject == null) {
			return;
		}

		if (configRegistry.contains(registryObject)) {
			return;
		}

		configRegistry.add(registryObject);
	}

	@Override
	public void unregister(ConfigOptions registryObject) {
		if (registryObject == null) {
			return;
		}

		if (!configRegistry.contains(registryObject)) {
			return;
		}

		configRegistry.remove(registryObject);
	}

	public List<ConfigOptions> getConfigRegistry() {
		return configRegistry;
	}

	/**
	 * <p>Interface for registering your own options in the TASmod config
	 * 
	 * @see com.minecrafttas.tasmod.registries.TASmodConfig TASmodConfig 
	 * @author Scribble
	 */
	public interface ConfigOptions extends Registerable {
		/**
		 * @return The config key name that is stored in the file
		 */
		public String getConfigKey();

		/**
		 * @return The default value that is used if the config key doesn't exist yet
		 */
		public String getDefaultValue();
	}
}
