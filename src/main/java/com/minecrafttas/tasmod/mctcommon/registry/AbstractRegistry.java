package com.minecrafttas.tasmod.mctcommon.registry;

import java.util.Arrays;
import java.util.Map;

import com.minecrafttas.tasmod.mctcommon.MCTCommon;

public abstract class AbstractRegistry<V extends Registerable> {
	protected final Map<String, V> REGISTRY;
	protected final String name;

	public AbstractRegistry(String name, Map<String, V> map) {
		this.REGISTRY = map;
		this.name = name;
	}

	public void register(V registryObject) {
		if (registryObject == null) {
			throw new NullPointerException("Tried to register an object to " + name + " with value null");
		}

		if (containsClass(registryObject)) {
			MCTCommon.LOGGER.warn("Trying to register an object in {}, but another instance of this class is already registered: {}", name, registryObject.getClass().getName());
			return;
		}

		if (REGISTRY.containsKey(registryObject.getExtensionName())) {
			MCTCommon.LOGGER.warn("Trying to register the an object in {}, but an extension with the same name is already registered: {}", registryObject.getExtensionName());
			return;
		}

		REGISTRY.put(registryObject.getExtensionName(), registryObject);
	}

	@SafeVarargs
	public final void register(V... registryObjects) {
		this.register(Arrays.asList(registryObjects));
	}

	public final void register(Iterable<V> registryObjects) {
		for (V registryObject : registryObjects) {
			this.register(registryObject);
		}
	}

	public void unregister(V registryObject) {
		if (registryObject == null) {
			throw new NullPointerException("Tried to unregister an object from " + name + " with value null");
		}
		if (REGISTRY.containsKey(registryObject.getExtensionName())) {
			REGISTRY.remove(registryObject.getExtensionName());
		} else {
			MCTCommon.LOGGER.warn("Trying to unregister an object from {}, but it was not registered: {}", name, registryObject.getClass().getName());
		}
	}

	@SafeVarargs
	public final void unregister(V... registryObjects) {
		this.unregister(Arrays.asList(registryObjects));
	}

	public final void unregister(Iterable<V> registryObjects) {
		for (V registryObject : registryObjects) {
			this.unregister(registryObject);
		}
	}

	public void clear() {
		REGISTRY.clear();
	}

	protected boolean containsClass(V newExtension) {
		return containsClazz(newExtension, REGISTRY.values());
	}

	public static <W> boolean containsClazz(W newExtension, Iterable<W> iterable) {
		for (W extension : iterable) {
			if (extension.getClass().equals(newExtension.getClass())) {
				return true;
			}
		}
		return false;
	}
}
