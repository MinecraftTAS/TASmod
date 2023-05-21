package com.minecrafttas.common;

import org.apache.commons.lang3.ArrayUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;

public class KeybindRegistry {

	public static void registerKeyBinding(KeyBinding keybind) {
		Minecraft mc = Minecraft.getMinecraft();
		GameSettings options = mc.gameSettings;
			if(options!=null) {
				((KeybindDuck)keybind).registerKeyCategory();
				options.keyBindings = ArrayUtils.add(options.keyBindings, keybind);
			}
	}
	
	
	public static interface KeybindDuck {
		public void registerKeyCategory();
	}
}