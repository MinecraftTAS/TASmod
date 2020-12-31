package de.scribble.lp.tasmod.virtual;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

import org.lwjgl.input.Keyboard;

import com.google.common.collect.Maps;

import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.IntHashMap;

/**
 * Transforms certain Minecraft keybindings to keybindings checked by LWJGL's isKeyDown method. <br>
 * Keybinds with LWJGL work during guiscreens and don't get recognised by the InputPlayback, meaning you can't accidentally savestate while playing back a file
 * @author ScribbleLP
 *
 */
public class VirtualKeybindings {
	private static final int standardCooldown=10;
	private static HashMap<KeyBinding, Long> cooldownHashMap=Maps.<KeyBinding, Long>newHashMap();
	private static long cooldowntimer=0;
	
	public static void increaseCooldowntimer() {
		cooldowntimer++;
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
	
}
