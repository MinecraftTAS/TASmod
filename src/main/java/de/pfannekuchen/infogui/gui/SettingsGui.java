package de.pfannekuchen.infogui.gui;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;

import de.pfannekuchen.killtherng.utils.EntityRandom;
import de.pfannekuchen.killtherng.utils.ItemRandom;
import de.pfannekuchen.tasmod.utils.PlayerPositionCalculator;
import de.pfannekuchen.tasmod.utils.TrajectoriesCalculator;
import de.scribble.lp.tasmod.ClientProxy;
import de.scribble.lp.tasmod.savestates.server.SavestateTrackerFile;
import de.scribble.lp.tasmod.tickratechanger.TickrateChangerClient;
import de.scribble.lp.tasmod.ticksync.TickSync;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.client.config.GuiCheckBox;

public class SettingsGui extends GuiScreen {
	
	public String dragging;
	public static HashMap<Settings, Integer> widths = new HashMap<>();
	
	static {
		widths.put(Settings.XYZ, 0);
		widths.put(Settings.XYZPRECISE, 0);
		widths.put(Settings.CXZ, 0);
		widths.put(Settings.WORLDSEED, 0);
		widths.put(Settings.RNGSEEDS, 0);
		widths.put(Settings.FACING, 0);
		widths.put(Settings.TICKS, 0);
		widths.put(Settings.TICKRATE, 0);
		widths.put(Settings.SAVESTATECOUNT, 0);
		widths.put(Settings.PREDICTEDXYZ, 0);
		widths.put(Settings.MOUSEPOS, 0);
		widths.put(Settings.VELOCITY, 0);
	}
	
	public static enum Settings {
		XYZ, XYZPRECISE, CXZ, WORLDSEED, RNGSEEDS, FACING, TICKS, TICKRATE, SAVESTATECOUNT, PREDICTEDXYZ, MOUSEPOS, TRAJECTORIES, VELOCITY;
	}
	
	public static Properties p;

	public String identify(int mouseX, int mouseY) {
		final AtomicReference<String> returnable = new AtomicReference<String>(null);
		widths.forEach((a, b) -> {
			int x = Integer.parseInt(p.getProperty(a.name() + "_x"));
			int y = Integer.parseInt(p.getProperty(a.name() + "_y"));
			int w = x + widths.get(a);
			int h = y + 25;
			
			if (mouseX >= x && mouseX <= w && mouseY >= y && mouseY <= h) {
				returnable.set(a.name());
				return;
			}
		});
		return returnable.get();
	}
	
	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		if (mouseButton == 1) {
			String id = identify(mouseX, mouseY);
			if (id != null) {
				p.setProperty(id + "_hideRect", p.getProperty(id + "_hideRect").equalsIgnoreCase("true") ? "false" : "true");
				try {
					save();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			return;
		}
		dragging = identify(mouseX, mouseY);
		super.mouseClicked(mouseX, mouseY, mouseButton);
	}
	
	@Override
	protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
		if (dragging != null) {
			p.setProperty(dragging + "_x", (mouseX - 10) + "");
			p.setProperty(dragging + "_y", (mouseY - 10)+ "");
		}
		super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
	}
	
	@Override
	public void initGui() {
		int y = height;
		int index = 0;
		for (Settings s : Settings.values()) {
			addButton(new GuiCheckBox(index, 1, y -= 13, s.name().toLowerCase(), Boolean.parseBoolean(p.getProperty(s.name() + "_visible"))));
			index++;
		}
		super.initGui();
	}
	
	@Override
	protected void actionPerformed(GuiButton button) throws IOException {
		super.actionPerformed(button);
		p.setProperty(Settings.values()[button.id].name() + "_visible", ((GuiCheckBox) button).isChecked() + "");
		try {
			save();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	protected void mouseReleased(int mouseX, int mouseY, int state) {
		if (dragging != null) {
			p.setProperty(dragging + "_x", (mouseX - 10) + "");
			p.setProperty(dragging + "_y", (mouseY - 10) 	 + "");
			try {
				save();
			} catch (IOException e) {
				e.printStackTrace();
			}
			dragging = null;
		}
		super.mouseReleased(mouseX, mouseY, state);
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		if (dragging == null) super.drawScreen(mouseX, mouseY, partialTicks);
	}
	
	public static void load() throws FileNotFoundException, IOException {
		p = new Properties();
		p.load(new FileInputStream(new File(Minecraft.getMinecraft().mcDataDir, "ingameGui.data")));
	}
	
	public static void drawOverlay() {
		if (Minecraft.getMinecraft().currentScreen instanceof SettingsGui) Minecraft.getMinecraft().currentScreen.drawDefaultBackground();
		boolean showXYZ = Boolean.parseBoolean(p.getProperty("XYZ_visible"));
		if (showXYZ) {
			int x = Integer.parseInt(p.getProperty("XYZ_x"));
			int y = Integer.parseInt(p.getProperty("XYZ_y"));
			widths.replace(Settings.XYZ, drawRectWithText(String.format("%.2f %.2f %.2f", Minecraft.getMinecraft().player.posX, Minecraft.getMinecraft().player.posY, Minecraft.getMinecraft().player.posZ), x, y, Boolean.parseBoolean(p.getProperty("XYZ_hideRect"))));
		}
		
		boolean showXYZPRECISE = Boolean.parseBoolean(p.getProperty("XYZPRECISE_visible"));
		if (showXYZPRECISE) {
			int x = Integer.parseInt(p.getProperty("XYZPRECISE_x"));
			int y = Integer.parseInt(p.getProperty("XYZPRECISE_y"));
			widths.replace(Settings.XYZPRECISE, drawRectWithText(String.format("%f %f %f", Minecraft.getMinecraft().player.posX, Minecraft.getMinecraft().player.posY, Minecraft.getMinecraft().player.posZ), x, y, Boolean.parseBoolean(p.getProperty("XYZPRECISE_hideRect"))));
		}
		
		boolean showCXZ = Boolean.parseBoolean(p.getProperty("CXZ_visible"));
		if (showCXZ) {
			int x = Integer.parseInt(p.getProperty("CXZ_x"));
			int y = Integer.parseInt(p.getProperty("CXZ_y"));
			widths.replace(Settings.CXZ, drawRectWithText(String.format("%d %d", Minecraft.getMinecraft().player.chunkCoordX, Minecraft.getMinecraft().player.chunkCoordZ), x, y, Boolean.parseBoolean(p.getProperty("CXZ_hideRect"))));
		}
		
		boolean showWORLDSEED = Boolean.parseBoolean(p.getProperty("WORLDSEED_visible"));
		if (showWORLDSEED) {
			int x = Integer.parseInt(p.getProperty("WORLDSEED_x"));
			int y = Integer.parseInt(p.getProperty("WORLDSEED_y"));
			widths.replace(Settings.WORLDSEED, drawRectWithText("World Seed: " + Minecraft.getMinecraft().world.worldInfo.getSeed(), x, y, Boolean.parseBoolean(p.getProperty("WORLDSEED_hideRect"))));
		}
		
		boolean showRNGSEEDS = Boolean.parseBoolean(p.getProperty("RNGSEEDS_visible"));
		if (showRNGSEEDS) {
			int x = Integer.parseInt(p.getProperty("RNGSEEDS_x"));
			int y = Integer.parseInt(p.getProperty("RNGSEEDS_y"));
			int i = widths.replace(Settings.RNGSEEDS, drawRectWithText("Entity Random Seed: " + EntityRandom.currentSeed, x, y, Boolean.parseBoolean(p.getProperty("RNGSEEDS_hideRect"))));
			drawRectWithTextFixedWidth("Item Random Seed: " + ItemRandom.currentSeed, x, y + 14, Boolean.parseBoolean(p.getProperty("RNGSEEDS_hideRect")), i);
		}
		
		boolean showFACING = Boolean.parseBoolean(p.getProperty("FACING_visible"));
		if (showFACING) {
			int x = Integer.parseInt(p.getProperty("FACING_x"));
			int y = Integer.parseInt(p.getProperty("FACING_y"));
			widths.replace(Settings.FACING, drawRectWithText(String.format("%.2f %.2f", Minecraft.getMinecraft().player.rotationYaw, Minecraft.getMinecraft().player.rotationPitch), x, y, Boolean.parseBoolean(p.getProperty("FACING_hideRect"))));
		}
		
		boolean showTICKS = Boolean.parseBoolean(p.getProperty("TICKS_visible"));
		if (showTICKS) {
			int x = Integer.parseInt(p.getProperty("TICKS_x"));
			int y = Integer.parseInt(p.getProperty("TICKS_y"));
			int i = widths.replace(Settings.TICKS, drawRectWithText("Server Ticks: " + TickSync.getServertickcounter(), x, y + 14, Boolean.parseBoolean(p.getProperty("TICKS_hideRect"))));
			drawRectWithTextFixedWidth("Client Ticks: " + TickSync.getClienttickcounter(), x, y, Boolean.parseBoolean(p.getProperty("TICKS_hideRect")), i);
		}
		
		boolean showTICKRATE= Boolean.parseBoolean(p.getProperty("TICKRATE_visible"));
		if (showTICKRATE) {
			int x = Integer.parseInt(p.getProperty("TICKRATE_x"));
			int y = Integer.parseInt(p.getProperty("TICKRATE_y"));
			widths.replace(Settings.TICKRATE, drawRectWithText("Tickrate: " + (int) (TickrateChangerClient.TICKS_PER_SECOND), x, y, Boolean.parseBoolean(p.getProperty("TICKRATE_hideRect"))));
		}
		
		boolean showSAVESTATECOUNT = Boolean.parseBoolean(p.getProperty("SAVESTATECOUNT_visible"));
		if (showSAVESTATECOUNT) {
			int x = Integer.parseInt(p.getProperty("SAVESTATECOUNT_x"));
			int y = Integer.parseInt(p.getProperty("SAVESTATECOUNT_y"));
			int i = widths.replace(Settings.SAVESTATECOUNT, drawRectWithText("Savestates: " + SavestateTrackerFile.savestatecount, x, y, Boolean.parseBoolean(p.getProperty("SAVESTATECOUNT_hideRect"))));
			drawRectWithTextFixedWidth("Loadstates: " + SavestateTrackerFile.loadstatecount, x, y + 14, Boolean.parseBoolean(p.getProperty("SAVESTATECOUNT_hideRect")), i);
		}
		
		boolean showPREDICTEDXYZ = Boolean.parseBoolean(p.getProperty("PREDICTEDXYZ_visible"));
		if (showPREDICTEDXYZ) {
			int x = Integer.parseInt(p.getProperty("PREDICTEDXYZ_x"));
			int y = Integer.parseInt(p.getProperty("PREDICTEDXYZ_y"));
			widths.replace(Settings.PREDICTEDXYZ, drawRectWithText(String.format("%f %f %f", PlayerPositionCalculator.xNew, PlayerPositionCalculator.yNew, PlayerPositionCalculator.zNew), x, y, Boolean.parseBoolean(p.getProperty("XYZPRECISE_hideRect"))));
		}
		
		boolean showMOUSEPOS = Boolean.parseBoolean(p.getProperty("MOUSEPOS_visible"));
		if (showMOUSEPOS) {
			int x = Integer.parseInt(p.getProperty("MOUSEPOS_x"));
			int y = Integer.parseInt(p.getProperty("MOUSEPOS_y"));
			widths.replace(Settings.MOUSEPOS, drawRectWithText("Mouse Cursor: " + ClientProxy.virtual.nextMouse.getPath().get(0).cursorX + " " + ClientProxy.virtual.nextMouse.getPath().get(0).cursorY, x, y, Boolean.parseBoolean(p.getProperty("MOUSEPOS_hideRect"))));
		}
		
		boolean showTRAJECTORIES = Boolean.parseBoolean(p.getProperty("TRAJECTORIES_visible"));
		if (showTRAJECTORIES) {
			int x = Integer.parseInt(p.getProperty("TRAJECTORIES_x"));
			int y = Integer.parseInt(p.getProperty("TRAJECTORIES_y"));
			String message = "Invalid Item";
			Vec3d vec = TrajectoriesCalculator.calculate();
			if (vec != null) {
				message = String.format("%.3f %.3f %.3f", vec.x, vec.y, vec.z);
			}
			widths.replace(Settings.TRAJECTORIES, drawRectWithText("Trajectories: " + message, x, y, Boolean.parseBoolean(p.getProperty("TRAJECTORIES_hideRect"))));
		}
		
		boolean showVELOCITY = Boolean.parseBoolean(p.getProperty("VELOCITY_visible"));
		if (showVELOCITY) {
			int x = Integer.parseInt(p.getProperty("VELOCITY_x"));
			int y = Integer.parseInt(p.getProperty("VELOCITY_y"));
			widths.replace(Settings.VELOCITY, drawRectWithText("Velocity: " + Minecraft.getMinecraft().player.motionX + " " + Minecraft.getMinecraft().player.motionY + " " + Minecraft.getMinecraft().player.motionZ, x, y, Boolean.parseBoolean(p.getProperty("VELOCITY_hideRect"))));
		}
	}
	
	private static int drawRectWithTextFixedWidth(String text, int x, int y, boolean rect, int w) {
		if (!rect) drawRect(x, y, x + w, y + 14, -2147483648);
		Minecraft.getMinecraft().fontRenderer.drawStringWithShadow(text, x + 2, y + 3, 0xFFFFFF);
		return Minecraft.getMinecraft().fontRenderer.getStringWidth(text) + 4;
	}
	
	private static int drawRectWithText(String text, int x, int y, boolean rect) {
		if (!rect) drawRect(x, y, x + Minecraft.getMinecraft().fontRenderer.getStringWidth(text) + 4, y + 14, -2147483648);
		Minecraft.getMinecraft().fontRenderer.drawStringWithShadow(text, x + 2, y + 3, 0xFFFFFF);
		return Minecraft.getMinecraft().fontRenderer.getStringWidth(text) + 4;
	}
	
	public static void save() throws IOException {
		p.store(new FileOutputStream(new File(Minecraft.getMinecraft().mcDataDir, "ingameGui.data"), false), "This file contains your Ingame Gui Layout");
	}
	
}
