package com.minecrafttas.tasmod.savestates.gui;

import com.minecrafttas.tasmod.TASmodClient;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * Stub GuiSavestateRename - GUI system needs complete rewrite for modern GuiGraphics API
 */
public class GuiSavestateRename extends GuiSavestate {

	public GuiSavestateRename(Component msg, int index) {
		super(msg);
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}
}