package com.minecrafttas.tasmod.virtual;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
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

	// Cached reflection fields/methods for performance
	private static Method keyMappingGetKey;
	private static Method keyGetValue;
	private static Field minecraftScreen;
	private static Method screenGetFocused;

	static {
		try {
			// KeyMapping.getKey() - returns InputConstants.Key
			keyMappingGetKey = KeyMapping.class.getDeclaredMethod("getKey");
			keyMappingGetKey.setAccessible(true);
			
			// InputConstants.Key.getValue() - returns GLFW keycode
			Class<?> keyClass = Class.forName("net.minecraft.util.InputConstants$Key");
			keyGetValue = keyClass.getDeclaredMethod("getValue");
			keyGetValue.setAccessible(true);
		} catch (Exception e) {
			// Fallback if API differs
		}
		
		try {
			// Minecraft.screen field
			minecraftScreen = Minecraft.class.getDeclaredField("screen");
			minecraftScreen.setAccessible(true);
			
			// Screen.getFocused()
			screenGetFocused = Screen.class.getDeclaredMethod("getFocused");
			screenGetFocused.setAccessible(true);
		} catch (Exception e) {
			// Fallback
		}
	}

	/**
	 * Checks if a key is currently down
	 * Uses Minecraft's key handler which processes GLFW events
	 */
	public static boolean isKeyDown(KeyMapping keybind) {
		if (keybind == null) return false;
		
		// Check if we're using virtual input (TAS playback)
		if (!TASmodClient.virtual.isUseVanillaIsKeyDown()) {
			// Use virtual keyboard state
			return TASmodClient.virtual.KEYBOARD.willKeyBeDown(getKeyCode(keybind));
		}
		
		// Use vanilla key handler
		return keybind.isDown();
	}

	/**
	 * Gets the GLFW keycode from a KeyMapping using reflection
	 */
	private static int getKeyCode(KeyMapping keybind) {
		if (keybind == null) return -1;
		
		try {
			if (keyMappingGetKey != null && keyGetValue != null) {
				Object key = keyMappingGetKey.invoke(keybind);
				if (key != null) {
					return (int) keyGetValue.invoke(key);
				}
			}
		} catch (Exception e) {
			// Ignore and fallback
		}
		
		// Fallback: try to get key code from keybind directly if available
		try {
			Field keyField = KeyMapping.class.getDeclaredField("key");
			keyField.setAccessible(true);
			Object key = keyField.get(keybind);
			if (key != null) {
				Method getValue = key.getClass().getMethod("getValue");
				return (int) getValue.invoke(key);
			}
		} catch (Exception ignored) {}
		
		return -1;
	}

	/**
	 * Checks if a key is down, but returns false if a text field is focused
	 * Used for keybinds that shouldn't trigger while typing
	 */
	public static boolean isKeyDownExceptTextfield(KeyMapping keybind) {
		Minecraft mc = Minecraft.getInstance();
		Screen currentScreen = getScreen(mc);
		
		// Check if a text field is focused
		if (currentScreen != null && getFocused(currentScreen) != null) {
			return false;
		}
		
		return isKeyDown(keybind);
	}

	private static Screen getScreen(Minecraft mc) {
		try {
			if (minecraftScreen != null) {
				return (Screen) minecraftScreen.get(mc);
			}
		} catch (Exception ignored) {}
		return null;
	}

	private static Object getFocused(Screen screen) {
		try {
			if (screenGetFocused != null) {
				return screenGetFocused.invoke(screen);
			}
		} catch (Exception ignored) {}
		return null;
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
			if (getKeyCode(keybind) == keycode) {
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