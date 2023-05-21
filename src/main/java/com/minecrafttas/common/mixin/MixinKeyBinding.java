package com.minecrafttas.common.mixin;

import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import com.minecrafttas.common.KeybindRegistry.KeybindDuck;

import net.minecraft.client.settings.KeyBinding;

/**
 * Mixin for registering keybinding categories
 * @author Scribble
 *
 */
@Mixin(KeyBinding.class)
public class MixinKeyBinding implements KeybindDuck {

	@Shadow
	private static Map<String, Integer> CATEGORY_ORDER;
	
	@Override
	public void registerKeyCategory() {
		KeyBinding keybind = (KeyBinding)(Object)this;
		if(!CATEGORY_ORDER.containsKey(keybind.getKeyCategory())) {
			CATEGORY_ORDER.put(keybind.getKeyCategory(), CATEGORY_ORDER.size()+1);
		}
	}

}
