package de.scribble.lp.tasmod.virtual;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.lwjgl.input.Keyboard;

import net.minecraft.client.settings.KeyBinding;

/**
 * Transforms certain Minecraft keybindings to keybindings checked by LWJGL's isKeyDown method. <br>
 * Keybinds with LWJGL work during guiscreens and don't get recognised by the InputPlayback, meaning you can't accidentally savestate while playing back a file
 * @author ScribbleLP
 *
 */
public class VirtualKeybindings {
	private static final int standardCooldown=10;
	List<KeyCooldown> keyList=new ArrayList<KeyCooldown>();
	
	private void add(KeyBinding keybinding) {
		keyList.add(new KeyCooldown(keybinding, standardCooldown));
	}
	
	public void decreaseCooldowns() {
		Stack<Integer> index=new Stack<Integer>();
		for(KeyCooldown key: keyList) {
			if(key.getCoooldown()==0) {
				index.add(keyList.indexOf(key));
			}else {
				key.cooldown--;
			}
		}
		for (int i = 0; i < index.size(); i++) {
			keyList.remove((int)index.pop());
		}
	}
	
	public boolean isKeyDown(KeyBinding key) {
		boolean down=Keyboard.isKeyDown(key.getKeyCode());
		if(down) {
			for(KeyCooldown keys: keyList) {
				if(keys.getKeyName().contentEquals(key.getKeyDescription())) {
					return false;
				}
			}
			keyList.add(new KeyCooldown(key, standardCooldown));
		}
		return down;
	}
	
	class KeyCooldown{
		private KeyBinding key;
		private int standardCooldown;
		public int cooldown=0;
		
		public KeyCooldown(KeyBinding key, int standardCooldown) {
			this.key=key;
			this.standardCooldown=standardCooldown;
			this.cooldown=standardCooldown;
		}
		
		public int getCoooldown() {
			return cooldown;
		}
		
		public void setPressed() {
			cooldown=standardCooldown;
		}
		
		public int getKeycode() {
			return key.getKeyCode();
		}
		
		public void decreaseCooldown() {
			if(cooldown>0) {
				cooldown--;
			}
		}
		public String getKeyName() {
			return key.getKeyDescription();
		}
	}
}
