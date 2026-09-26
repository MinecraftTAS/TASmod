package com.minecrafttas.tasmod.virtual;

import com.minecrafttas.tasmod.TASmodClient;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * Stub SubtickGuiScreen - GUI system needs complete rewrite for modern GuiGraphics API
 */
public class SubtickGuiScreen extends Screen {

	public SubtickGuiScreen() {
		super(Component.literal("SubtickGuiScreen"));
	}

	@Override
	protected void init() {
		TASmodClient.virtual.setUseVanillaIsKeyDown(true);
	}

	@Override
	public void onClose() {
		TASmodClient.virtual.setUseVanillaIsKeyDown(false);
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}
}