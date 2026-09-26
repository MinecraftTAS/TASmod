package com.minecrafttas.tasmod.savestates.gui;

import com.minecrafttas.tasmod.TASmodClient;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * Stub GuiResourcepackWarn - GUI system needs complete rewrite for modern GuiGraphics API
 */
public class GuiResourcepackWarn extends Screen {

	public GuiResourcepackWarn() {
		super(Component.literal("GuiResourcepackWarn"));
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}
}