package com.minecrafttas.tasmod.savestates.gui;

import com.minecrafttas.tasmod.virtual.SubtickGuiScreen;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

/**
 * Savestate GUI screen
 */
public class GuiSavestate extends SubtickGuiScreen {

	private final Component msg;

	public GuiSavestate(Component msg) {
		super();
		this.msg = msg;
	}

	@Override
	public boolean shouldCloseOnEsc() {
		return true;
	}

	@Override
	public boolean isPauseScreen() {
		return true;
	}
}

