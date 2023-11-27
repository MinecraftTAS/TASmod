package com.minecrafttas.mctcommon.mixin;

import java.util.Map;

import org.apache.commons.lang3.NotImplementedException;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.settings.KeyBinding;

@Mixin(KeyBinding.class)
public interface AccessorKeyBinding{
	
	@Accessor("CATEGORY_ORDER")
	public static Map<String, Integer> getCategoryOrder() {
		throw new NotImplementedException("WEE WOO");
	}
}
