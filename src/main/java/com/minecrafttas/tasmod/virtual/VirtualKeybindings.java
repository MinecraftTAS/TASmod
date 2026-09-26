package com.minecrafttas.tasmod.virtual;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.minecrafttas.tasmod.TASmodClient;
import com.minecrafttas.tasmod.virtual.event.VirtualKeyboardEvent;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.util.Util;

/**
 * VirtualKeybindings - Handles key state checking for TAS playback
 * Uses modern GLFW input system (1.22+)
 */
public class VirtualKeybindings {

	public static boolean focused = false;

	private static List<KeyMapping> blockedKeys = new ArrayList<>();

	private static HashMap<KeyMapping, Long> cooldownHashMap = new HashMap<>();

	private static long cooldown = 50;

	/**
	 * Checks if a key is currently down
	 * Uses Minecraft's key handler which processes GLFW events
	 */
	public static boolean isKeyDown(KeyMapping keybind) {
		if (keybind == null) return false;
		
		// Check if we're using virtual input (TAS playback)
		if (!TASmodClient.virtual.useVanillaIsKeyDown) {
			// Use virtual keyboard state
			return TASmodClient.virtual.KEYBOARD.willKeyBeDown(keybind.getKey().getValue());
		}
		
		// Use vanilla key handler
		return keybind.isDown();
	}

	/**
	 * Checks if a key is down, but returns false if a text field is focused
	 * Used for keybinds that shouldn't trigger while typing
	 */
	public static boolean isKeyDownExceptTextfield(KeyMapping keybind) {
		Minecraft mc = Minecraft.getInstance();
		Screen currentScreen = mc.screen;
		
		// Check if a text field is focused
		if (currentScreen != null && currentScreen.getFocused() != null) {
			return false;
		}
		
		return isKeyDown(keybind);
	}

	/**
	 * Registers a key mapping as blocked (won't trigger vanilla actions during playback)
	 */
	public static void registerBlockedKeyMapping(KeyMapping keybind) {
		if (!blockedKeys.contains(keybind)) {
			blockedKeys.add(keybind);
		}
	}

	/**
	 * Checks if a keycode is always blocked (used during TAS playback)
	 */
	public static boolean isKeyCodeAlwaysBlocked(int keycode) {
		for (KeyMapping keybind : blockedKeys) {
			if (keybind.getKey().getValue() == keycode) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Checks if a key was just pressed (not held)
	 * Uses cooldown to prevent rapid repeats
	 */
	public static boolean wasKeyPressed(KeyMapping keybind) {
		if (keybind == null) return false;
		
		long now = Util.getMillis();
		Long lastPress = cooldownHashMap.get(keybind);
		
		if (isKeyDown(keybind)) {
			if (lastPress == null || now - lastPress >= cooldown) {
				cooldownHashMap.put(keybind, now);
				return true;
			}
		}
		return false;
	}

	/**
	 * Clears all blocked keys
	 */
	public static void clearBlockedKeys() {
		blockedKeys.clear();
	}

	/**
	 * Clears cooldown map
	 */
	public static void clearCooldowns() {
		cooldownHashMap.clear();
	}
}