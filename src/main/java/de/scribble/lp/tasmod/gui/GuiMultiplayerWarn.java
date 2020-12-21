package de.scribble.lp.tasmod.gui;

import java.io.IOException;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.resources.I18n;

public class GuiMultiplayerWarn extends GuiScreen{
	private GuiScreen previous;
	public GuiMultiplayerWarn(GuiScreen screen) {
		previous=screen;
	}
	@Override
	public void initGui() {
		this.buttonList.add(new GuiButton(0, width / 2 -100, height / 2 + 70, "Continue"));
		super.initGui();
	}
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		this.drawDefaultBackground();
		
		ScaledResolution scaled = new ScaledResolution(Minecraft.getMinecraft());
		int width = scaled.getScaledWidth();
		int height = scaled.getScaledHeight();
		
		drawCenteredString(fontRenderer,I18n.format("WARNING"), width / 2, height / 4 + 50 + -16, 0xCE0000);
		drawCenteredString(fontRenderer,I18n.format("Do NOT join a server that has not installed the TASmod (e.g. Hypixel)."), width / 2, height / 4 + 50 + -6, 0xFFFFFF);
		drawCenteredString(fontRenderer,I18n.format("You will softlock your game for a few seconds then disconnect!"), width / 2, height / 4 + 50 + 4, 0xFFFFFF);
		drawCenteredString(fontRenderer,I18n.format("This mod only works together with a server."), width / 2, height / 4 + 50 + 14, 0xFFFFFF);
		
		super.drawScreen(mouseX, mouseY, partialTicks);
	}
	@Override
	protected void actionPerformed(GuiButton button) throws IOException {
		if(button.id==0) {
			Minecraft.getMinecraft().displayGuiScreen(new GuiMultiplayer(previous));
		}
	}
}
