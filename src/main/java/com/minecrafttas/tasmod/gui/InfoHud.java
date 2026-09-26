package com.minecrafttas.tasmod.gui;

import com.minecrafttas.tasmod.mctcommon.events.EventClient.EventClientTick;
import com.minecrafttas.tasmod.events.EventClient.EventDrawHotbar;
import com.minecrafttas.tasmod.TASmodClient;
import com.minecrafttas.tasmod.playback.PlaybackControllerClient.TASstate;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * InfoHud - HUD overlay for displaying TAS information
 * Rendering implementation depends on version-specific API
 */
public class InfoHud extends Screen implements EventClientTick, EventDrawHotbar {

	public InfoHud() {
		super(Component.literal("InfoHud"));
	}

	@Override
	public void onClientTick(Minecraft mc) {
		// Update any dynamic HUD elements here
	}

	@Override
	public void onDrawHotbar() {
		// HUD rendering - implementation depends on version-specific rendering API
		// In 1.22+, this would use GuiGraphics
		// For now, this event is fired but rendering is handled elsewhere or stubbed
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}
}