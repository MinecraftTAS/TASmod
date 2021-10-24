package de.scribble.lp.tasmod.virtual;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import com.google.common.collect.Maps;

import de.scribble.lp.tasmod.ClientProxy;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiControls;
import net.minecraft.client.gui.inventory.GuiEditSign;
import net.minecraft.client.settings.KeyBinding;

/**
 * Applies special rules to vanilla keybindings. <br>
 * <br>
 * Using {@link #isKeyDown(KeyBinding)}, the registered keybindings will work
 * inside of gui screens <br>
 * <br>
 * {@link #isKeyDownExceptTextfield(KeyBinding)} does the same, but excludes
 * textfields, certain guiscreens, and the keybinding options<br>
 * <br>
 * Keybindings registered with {@link #registerBlockedKeyBinding(KeyBinding)}
 * will not be recorded during a recording or pressed in a playback
 * 
 * @author ScribbleLP
 *
 */
public class VirtualKeybindings {
	private static Minecraft mc = Minecraft.getMinecraft();
	private static long cooldown = 50*5;
	private static HashMap<KeyBinding, Long> cooldownHashMap = Maps.<KeyBinding, Long>newHashMap();
	private static List<KeyBinding> blockedKeys = new ArrayList<>();
	public static boolean focused = false;


	/**
	 * Checks whether the keycode is pressed, regardless of any gui screens
	 * 
	 * @param keybind
	 * @return
	 */
	public static boolean isKeyDown(KeyBinding keybind) {

		int keycode = keybind.getKeyCode();

		boolean down = false;

		if(mc.currentScreen instanceof GuiControls) {
			return false;
		}
		
		if (isKeyCodeAlwaysBlocked(keycode)) {
			down = keycode >= 0 ? Keyboard.isKeyDown(keycode) : Mouse.isButtonDown(keycode + 100);
		} else {
			down = ClientProxy.virtual.willKeyBeDown(keycode);
		}

		if (down) {
			if (cooldownHashMap.containsKey(keybind)) {
				if (cooldown <= Minecraft.getSystemTime() - (long) cooldownHashMap.get(keybind)) {
					cooldownHashMap.put(keybind, Minecraft.getSystemTime());
					return true;
				}
				return false;
			} else {
				cooldownHashMap.put(keybind, Minecraft.getSystemTime());
				return true;
			}
		}
		return false;
	}

	/**
	 * Checks whether the key is down, but stops when certain conditions apply
	 * 
	 * @param keybind
	 * @return
	 */
	public static boolean isKeyDownExceptTextfield(KeyBinding keybind) {
		if (mc.currentScreen instanceof GuiChat || mc.currentScreen instanceof GuiEditSign || (focused && mc.currentScreen != null)) {
			return false;
		}
		return isKeyDown(keybind);
	}

	/**
	 * Registers keybindings that should not be recorded or played back in a TAS
	 * 
	 * @param keybind
	 */
	public static void registerBlockedKeyBinding(KeyBinding keybind) {
		blockedKeys.add(keybind);
	}

	/**
	 * Checks whether the keycode should not be recorded or played back in a TAS
	 * 
	 * @param keycode to block
	 * @return Whether it should be blocked
	 */
	public static boolean isKeyCodeAlwaysBlocked(int keycode) {
		for (KeyBinding keybind : blockedKeys) {
			if (keycode == keybind.getKeyCode())
				return true;
		}
		return false;
	}

}
