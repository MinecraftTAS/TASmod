package com.minecrafttas.tasmod.playback.tasfile.flavor;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import com.minecrafttas.tasmod.mctcommon.registry.AbstractRegistry;

/**
 * Registry for registering custom PlaybackSerialisers.<br>
 * This is useful if you want to create your own syntax "flavor" for TASmod to read.<br>
 * 
 * @author Scribble
 */
public class SerialiserFlavorRegistry extends AbstractRegistry<SerialiserFlavorBase> {

	public SerialiserFlavorRegistry() {
		super("FLAVOR_REGISTRY", new LinkedHashMap<>());
	}

	public Set<String> getFlavorNames() {
		return REGISTRY.keySet();
	}

	public SerialiserFlavorBase getFlavor(String name) {
		SerialiserFlavorBase out = REGISTRY.get(name);
		return out == null ? null : out.clone();
	}

	public List<SerialiserFlavorBase> getFlavors() {
		return new ArrayList<>(REGISTRY.values());
	}
}

