package com.minecrafttas.tasmod.util;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public class I18n {

	public static String format(String string, Object... args) {
		return Component.translatable(string, args).getString();
	}

	public static boolean hasKey(String key) {
		// In modern MC, we can't easily check without the Language instance
		return true;
	}
}

