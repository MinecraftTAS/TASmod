package de.scribble.lp.tasmod.virtual;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.lwjgl.input.Keyboard;

import com.google.common.collect.Maps;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.settings.KeyBinding;

/**
 * Transforms certain Minecraft keybindings to keybindings checked by LWJGL's isKeyDown method. <br>
 * Keybinds with LWJGL work during guiscreens and don't get recognised by the InputPlayback, meaning you can't accidentally savestate while playing back a file
 * @author ScribbleLP
 *
 */
public class VirtualKeybindings {
	private static Minecraft mc= Minecraft.getMinecraft();
	private static final long standardCooldown=20;
	private static HashMap<KeyBinding, Long> cooldownHashMap=Maps.<KeyBinding, Long>newHashMap();
	private static List<KeyBinding> blockedKeys=new ArrayList<>();
	private static List<KeyBinding> blockedDuringRecordingKeys=new ArrayList<>();
	private static long cooldowntimer=0;
	public static boolean focused = false;
	
	public static void increaseCooldowntimer() {
		cooldowntimer++;
	}
	/**
	 * Checks whether the key is down, but doesn't execute in chat
	 * @param keybind
	 * @return
	 */
	public static boolean isKeyDownExceptTextfield(KeyBinding keybind) {
		if(mc.currentScreen instanceof GuiChat || focused) {
			return false;
		}
		boolean down=Keyboard.isKeyDown(keybind.getKeyCode());
		if(down) {
			if(cooldownHashMap.containsKey(keybind)) {
				if(standardCooldown<=cooldowntimer-(long)cooldownHashMap.get(keybind)) {
					cooldownHashMap.put(keybind, cooldowntimer);
					return true;
				}
				return false;
			}else {
				cooldownHashMap.put(keybind, cooldowntimer);
				return true;
			}
		}
		return false;
	}
	public static boolean isKeyDown(KeyBinding keybind) {
		boolean down=Keyboard.isKeyDown(keybind.getKeyCode());
		if(down) {
			if(cooldownHashMap.containsKey(keybind)) {
				if(standardCooldown<=cooldowntimer-(long)cooldownHashMap.get(keybind)) {
					cooldownHashMap.put(keybind, cooldowntimer);
					return true;
				}
				return false;
			}else {
				cooldownHashMap.put(keybind, cooldowntimer);
				return true;
			}
		}
		return false;
	}
	public static void registerBlockedKeyBinding(KeyBinding keybind) {
		blockedKeys.add(keybind);
	}
	public static void registerBlockedDuringRecordingKeyBinding(KeyBinding keybind) {
		blockedDuringRecordingKeys.add(keybind);
	}
	public static boolean isKeyCodeAlwaysBlocked(int keycode) {
		for(KeyBinding keybind:blockedKeys) {
			if(keycode==keybind.getKeyCode()) return true;
		}
		return false;
	}
	public static boolean isKeyCodeBlockedDuringRecording(int keycode) {
		for(KeyBinding keybind:blockedDuringRecordingKeys) {
			if(keycode==keybind.getKeyCode()) {
				blockedDuringRecordingKeys.remove(keybind);
				return true;
			}
		}
		return false;
	}
}
