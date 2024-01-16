package com.minecrafttas.mctcommon;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;

import com.minecrafttas.mctcommon.events.EventClient.EventClientGameLoop;
import com.minecrafttas.mctcommon.mixin.AccessorKeyBinding;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;

/**
 * Keybind manager
 * @author Pancake
 */
public abstract class KeybindManager implements EventClientGameLoop {

	public static class Keybind {

		private KeyBinding keyBinding;
		private String category;
		private Runnable onKeyDown;

		/**
		 * Initialize keybind
		 * 
		 * @param name       Name of keybind
		 * @param category   Category of keybind
		 * @param defaultKey Default key of keybind
		 * @param onKeyDown  Will be run when the keybind is pressed
		 */
		public Keybind(String name, String category, int defaultKey, Runnable onKeyDown) {
			this.keyBinding = new KeyBinding(name, defaultKey, category);
			this.category = category;
			this.onKeyDown = onKeyDown;
		}

	}

	private List<Keybind> keybindings;

	/**
	 * Initialize keybind manager
	 */
	public KeybindManager() {
		this.keybindings = new ArrayList<>();
	}

	/**
	 * Handle registered keybindings on game loop
	 */
	@Override
	public void onRunClientGameLoop(Minecraft mc) {
		for (Keybind keybind : this.keybindings)
			if (this.isKeyDown(keybind.keyBinding))
				keybind.onKeyDown.run();
	}

	protected abstract boolean isKeyDown(KeyBinding i);

	/**
	 * Register new keybind
	 * 
	 * @param keybind Keybind to register
	 */
	public KeyBinding registerKeybind(Keybind keybind) {
		this.keybindings.add(keybind);
		KeyBinding keyBinding = keybind.keyBinding;

		// add category
		GameSettings options = Minecraft.getMinecraft().gameSettings;
		if (!AccessorKeyBinding.getCategoryOrder().containsKey(keybind.category))
			AccessorKeyBinding.getCategoryOrder().put(keybind.category, AccessorKeyBinding.getCategoryOrder().size() + 1);

		// add keybinding
		options.keyBindings = ArrayUtils.add(options.keyBindings, keyBinding);
		return keyBinding;
	}

}