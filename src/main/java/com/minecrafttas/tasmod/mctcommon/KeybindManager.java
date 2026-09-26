package com.minecrafttas.tasmod.mctcommon;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;

import com.minecrafttas.tasmod.mctcommon.events.EventClient.EventClientGameLoop;
import com.minecrafttas.tasmod.mctcommon.mixin.AccessorKeyBinding;

import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.KeyMapping;

/**
 * Keybind manager
 * 
 * @author Pancake
 */
public class KeybindManager implements EventClientGameLoop {

	private final IsKeyDownFunc defaultFunction;

	private Map<KeybindID, Keybind> keybindings;

	/**
	 * Initialize keybind manage
	 * 
	 * @param defaultFunction The default function used to determine if a keybind is
	 *                        down. Can be overridden when registering a new keybind
	 */
	public KeybindManager(IsKeyDownFunc defaultFunction) {
		this.defaultFunction = defaultFunction;
		this.keybindings = new HashMap<>();
	}

	/**
	 * Handle registered keybindings on game loop
	 */
	@Override
	public void onRunClientGameLoop(Minecraft mc) {
		for (Keybind keybind : this.keybindings.values()) {
			IsKeyDownFunc keyDown = keybind.isKeyDownFunc != null ? keybind.isKeyDownFunc : defaultFunction;
			if (keyDown.isKeyDown(keybind.vanillaKeyBinding)) {
				keybind.onKeyDown.run();
			}
		}

	}

	public void registerKeybinds(Options options, Class<? extends KeybindID> keybindIDclass) {
		if (keybindIDclass.isEnum())
			registerKeybinds(options, keybindIDclass.getEnumConstants());
	}

	public void registerKeybinds(Options options, KeybindID... keybind) {
		for (KeybindID keybindEnum : keybind) {
			registerKeybind(options, keybindEnum, keybindEnum.getKeybind());
		}
	}

	/**
	 * Register a new keybind
	 * 
	 * @param keybindID The {@link KeybindID} to register this underI 
	 * @param keybind The {@link Keybind} to register
	 */
	public void registerKeybind(Options options, KeybindID keybindID, Keybind keybind) {
		this.keybindings.put(keybindID, keybind);
		// Note: In modern MC, keybindings are registered differently via Fabric's KeyBindingRegistry
		// This is a placeholder for the old API
	}

	public Keybind getKeybind(KeybindID id) {
		return keybindings.get(id);
	}

	@FunctionalInterface
	public static interface IsKeyDownFunc {

		public boolean isKeyDown(KeyMapping keybind);
	}

	public static interface KeybindID {
		public Keybind getKeybind();
	}

	public static class Keybind {

		public final KeyMapping vanillaKeyBinding;
		private final String category;
		private final Runnable onKeyDown;
		private final IsKeyDownFunc isKeyDownFunc;

		/**
		 * Initialize keybind
		 * 
		 * @param name       Name of keybind
		 * @param category   Category of keybind
		 * @param defaultKey Default key of keybind
		 * @param onKeyDown  Will be run when the keybind is pressed
		 */
		public Keybind(String name, String category, int defaultKey, Runnable onKeyDown) {
			this(name, category, defaultKey, onKeyDown, null);
		}

		/**
		 * Initialize keybind with a different "isKeyDown" method
		 * 
		 * @param name       Name of keybind
		 * @param category   Category of keybind
		 * @param defaultKey Default key of keybind
		 * @param onKeyDown  Will be run when the keybind is pressed
		 */
		public Keybind(String name, String category, int defaultKey, Runnable onKeyDown, IsKeyDownFunc func) {
			this.vanillaKeyBinding = new KeyMapping(name, defaultKey, KeyMapping.Category.MISC);
			this.category = category;
			this.onKeyDown = onKeyDown;
			this.isKeyDownFunc = func;
		}

		@Override
		public String toString() {
			return this.vanillaKeyBinding.getTranslatedKeyMessage().getString();
		}
	}
}
