package com.minecrafttas.common;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;

import com.minecrafttas.common.events.client.EventClientGameLoop;
import com.minecrafttas.tasmod.virtual.VirtualKeybindings;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;

/**
 * Keybind manager
 */
public class KeybindManager implements EventClientGameLoop  {

	public static class Keybind {
		
		private KeyBinding keyBinding;
		private String category;
		private Runnable onKeyDown;

		/**
		 * Initialize keybind
		 * @param name Name of keybind
		 * @param category Category of keybind
		 * @param defaultKey Default key of keybind
		 * @param onKeyDown Will be run when the keybind is pressed
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
			if (VirtualKeybindings.isKeyDownExceptTextfield(keybind.keyBinding))
				keybind.onKeyDown.run();
	}

	/**
	 * Register new keybind
	 * @param keybind Keybind
	 */
	public void registerKeybind(Keybind keybind) {
		this.keybindings.add(keybind);
		KeyBinding keyBinding = keybind.keyBinding;
		
		// block keybinding from virtual keybindings
		VirtualKeybindings.registerBlockedKeyBinding(keyBinding);

		// add category
		GameSettings options = Minecraft.getMinecraft().gameSettings;
		if (!KeyBinding.CATEGORY_ORDER.containsKey(keybind.category))
			KeyBinding.CATEGORY_ORDER.put(keybind.category, KeyBinding.CATEGORY_ORDER.size() + 1);
		
		// add keybinding
		options.keyBindings = ArrayUtils.add(options.keyBindings, keyBinding);
	}
	
}