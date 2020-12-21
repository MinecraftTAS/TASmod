package de.scribble.lp.tasmod.gui;

import java.io.IOException;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.resources.I18n;

public class GuiMultiplayerTimeOut extends GuiScreen{
	private GuiScreen previous;
	
	public GuiMultiplayerTimeOut() {
		previous=new GuiMainMenu();
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
		
		drawCenteredString(fontRenderer,I18n.format("TASmod: Timed out"), width / 2, height / 4 + 50 + -16, 0xFFFFFF);
		drawCenteredString(fontRenderer,I18n.format("Lost or could not make a connection to the TASmod on the server side"), width / 2, height / 4 + 50 + -6, 0xFFFFFF);
		drawCenteredString(fontRenderer,I18n.format("Possible Cause:"), width / 2, height / 4 + 50 + 14, 0xFFFFFF);
		drawCenteredString(fontRenderer,I18n.format("The server has no TASmod installed or the server lagged too much."), width / 2, height / 4 + 50 + 24, 0xFFFFFF);
		drawCenteredString(fontRenderer,I18n.format("It's also possible to get this message in singleplayer if the integrated server stopped responding."), width / 2, height / 4 + 50 + 34, 0xFFFFFF);
		super.drawScreen(mouseX, mouseY, partialTicks);
	}
	@Override
	protected void actionPerformed(GuiButton button) throws IOException {
		if(button.id==0) {
			Minecraft.getMinecraft().displayGuiScreen(new GuiMultiplayer(previous));
		}
	}
}
