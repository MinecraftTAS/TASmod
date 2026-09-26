package com.minecrafttas.tasmod.mctcommon.registry;

/**
 * Interface for declaring that a class can be registered by a class of type {@link AbstractRegistry}
 * 
 * @author Scribble
 */
public interface Registerable {

	/**
	 * @return The name of the extension that is registered
	 */
	public String getExtensionName();
}
