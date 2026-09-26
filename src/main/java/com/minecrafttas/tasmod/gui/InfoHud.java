package com.minecrafttas.tasmod.gui;

import com.minecrafttas.tasmod.mctcommon.events.EventClient.EventClientTick;
import com.minecrafttas.tasmod.events.EventClient.EventDrawHotbar;
import com.minecrafttas.tasmod.TASmodClient;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * Stub InfoHud - GUI system needs complete rewrite for modern GuiGraphics API
 */
public class InfoHud extends Screen implements EventClientTick, EventDrawHotbar {

	public InfoHud() {
		super(Component.literal("InfoHud"));
	}

	@Override
	public void onClientTick(Minecraft mc) {
		// Stub - no functionality
	}

	@Override
	public void onDrawHotbar() {
		// Stub - no functionality
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}
}