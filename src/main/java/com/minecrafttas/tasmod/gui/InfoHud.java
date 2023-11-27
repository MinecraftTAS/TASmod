package com.minecrafttas.tasmod.gui;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Callable;

import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;

import com.minecrafttas.mctcommon.events.EventClient.EventClientTick;
import com.minecrafttas.tasmod.TASmod;
import com.minecrafttas.tasmod.TASmodClient;
import com.minecrafttas.tasmod.events.EventClient.EventDrawHotbar;
import com.minecrafttas.tasmod.handlers.InterpolationHandler;
import com.minecrafttas.tasmod.monitoring.DesyncMonitoring;
import com.minecrafttas.tasmod.playback.ControlByteHandler;
import com.minecrafttas.tasmod.playback.PlaybackControllerClient.TASstate;
import com.minecrafttas.tasmod.util.TrajectoriesCalculator;
import com.mojang.realmsclient.gui.ChatFormatting;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.math.Vec3d;

/**
 * The info hud is a hud that is always being rendered ontop of the screen, it can show some stuff such as coordinates, etc.,
 * any everything can be customized
 * @author Pancake
 */
public class InfoHud extends GuiScreen implements EventClientTick, EventDrawHotbar{
	
	public static class InfoLabel {
		public String displayName;
		public int x;
		public int y;
		public boolean visible;
		public boolean renderRect;
		public String renderText;
		private Callable<String> text;
		
		
		public InfoLabel(String displayName, int x, int y, boolean visible, boolean renderRect, Callable<String> text) {
			this.displayName = displayName;
			this.visible = visible;
			this.x = x;
			this.y = y;
			this.renderRect = renderRect;
			this.text = text;
		}
		
		public void tick() {
			try {
				renderText = text.call();
			} catch (Exception e) {
				e.printStackTrace();
				// Lots of NPEs
			}
		}
	}
	
	/** -1, or the current index in {@link InfoHud#lists} that is being dragged by the mouse */
	private int currentlyDraggedIndex = -1;
	private int xOffset; // drag offsets
	private int yOffset;
	
	private int gridSizeX=14;
	private int gridSizeY=14;
	
	public Properties configuration;
	private boolean resetLayout;
	public static List<InfoLabel> lists = new ArrayList<>();
	
	private void setDefaults(String string, int y) {
		setDefaults(string, y, false);
	}
	
	private void setDefaults(String string, int y, boolean enabled) {
		configuration.setProperty(string + "_x", "0");
		configuration.setProperty(string + "_y", y + "");
		configuration.setProperty(string + "_visible", enabled?"true":"false");
		configuration.setProperty(string + "_rect", "false");
		saveConfig();
	}
	
	/**
	 * Returns the object below the mouse
	 */
	public void identify(int mouseX, int mouseY) {
		int index = 0;
		for (InfoLabel label : lists) {
			int x=0;
			int y=0;
			try {
				x = Integer.parseInt(configuration.getProperty(label.displayName + "_x"));
				y = Integer.parseInt(configuration.getProperty(label.displayName + "_y"));
				
				Pair<Integer, Integer> newPos = getScreenOffset(x, y, label);
				
				x = newPos.getLeft();
				y = newPos.getRight();
			} catch (NumberFormatException e) {
				configuration.setProperty(label.displayName + "_x", "0");
				configuration.setProperty(label.displayName + "_y", "0");
				saveConfig();
			}
			int w = x + Minecraft.getMinecraft().fontRenderer.getStringWidth(label.renderText);
			int h = y + 15;
			
			if (mouseX >= x && mouseX <= w && mouseY >= y && mouseY <= h) {
				currentlyDraggedIndex = index;
				xOffset = mouseX - x;
				yOffset = mouseY - y;
				return;
			}
			index++;
		}
		currentlyDraggedIndex = -1;
		xOffset = -1;
		yOffset = -1;
	}
	
	@Override protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
		if (mouseButton == 2) {
			identify(mouseX, mouseY);
			if (currentlyDraggedIndex != -1) {
				String id = lists.get(currentlyDraggedIndex).displayName;
				lists.get(currentlyDraggedIndex).renderRect = !lists.get(currentlyDraggedIndex).renderRect;
				configuration.setProperty(id + "_rect", configuration.getProperty(id + "_rect").equalsIgnoreCase("true") ? "false" : "true");
				saveConfig();
				currentlyDraggedIndex = -1;
			}
			return;
		} else if (mouseButton == 1) {
			identify(mouseX, mouseY);
			if (currentlyDraggedIndex != -1) {
				String id = lists.get(currentlyDraggedIndex).displayName;
				lists.get(currentlyDraggedIndex).visible = !lists.get(currentlyDraggedIndex).visible;
				configuration.setProperty(id + "_visible", configuration.getProperty(id + "_visible").equalsIgnoreCase("true") ? "false" : "true");
				saveConfig();
				currentlyDraggedIndex = -1;
			}
			return;
		}
		identify(mouseX, mouseY);
		super.mouseClicked(mouseX, mouseY, mouseButton);
	}
	
	@Override
	protected void mouseReleased(int p_mouseReleased_1_, int p_mouseReleased_2_, int p_mouseReleased_3_) {
		currentlyDraggedIndex = -1;
		saveConfig();
		super.mouseReleased(p_mouseReleased_1_, p_mouseReleased_2_, p_mouseReleased_3_);
	}
	
	@Override
	protected void mouseClickMove(int mouseX, int mouseY, int k, long millis) {
		if (currentlyDraggedIndex != -1) {
			String dragging = lists.get(currentlyDraggedIndex).displayName;
			
			int mousePosX=mouseX - xOffset;
			int mousePosY=mouseY - yOffset;
			
			if(TASmodClient.virtual.isKeyDown(42)) {
				mousePosX=snapToGridX(mousePosX);
				mousePosY=snapToGridY(mousePosY);
			}
			
			lists.get(currentlyDraggedIndex).x = mousePosX;
			lists.get(currentlyDraggedIndex).y = mousePosY;
			
			configuration.setProperty(dragging + "_x", lists.get(currentlyDraggedIndex).x + "");
			configuration.setProperty(dragging + "_y", lists.get(currentlyDraggedIndex).y + "");
		}
		super.mouseClickMove(mouseX, mouseY, k, millis);
	}
	
	private int snapToGridX(int x) {
		return Math.round(x / gridSizeX) * gridSizeX;
	}
	
	private int snapToGridY(int y) {
		return Math.round(y / gridSizeY) * gridSizeY;
	}
	
	/**
	 * Saves the Configuration
	 */
	private void saveConfig() {
		if(!(Minecraft.getMinecraft().currentScreen instanceof InfoHud) || configuration == null) {
			return;
		}
		try {
			File tasmodDir = new File(Minecraft.getMinecraft().mcDataDir, "tasmod");
			tasmodDir.mkdir();
			File configFile = new File(tasmodDir, "infogui2.cfg");
			if (!configFile.exists()) configFile.createNewFile();
			configuration.store(new FileOutputStream(configFile, false), "DO NOT EDIT MANUALLY");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Updates every tick
	 */
	@Override
	public void onClientTick(Minecraft mc) {
		if(mc.player!=null) {
			if (checkInit()) return;
		}
	}
	
	public boolean checkInit() {
		if (configuration != null) return false;
		/* Check whether already rendered before */
		try {
			configuration = new Properties();
			if (!resetLayout) {
				File tasmodDir = new File(Minecraft.getMinecraft().mcDataDir, "tasmod");
				tasmodDir.mkdir();
				File configFile = new File(tasmodDir, "infogui2.cfg");
				if (!configFile.exists()) configFile.createNewFile();
				configuration.load(new FileReader(configFile));
			}else {
				resetLayout = false;
			}
			lists = new ArrayList<InfoLabel>();
			/* ====================== */
			String title = "tickrate";
			int y = 0;
			if (configuration.getProperty(title + "_x", "err").equals("err")) setDefaults(title, y);
			lists.add(new InfoLabel(title, Integer.parseInt(configuration.getProperty(title + "_x")), Integer.parseInt(configuration.getProperty(title + "_y")), Boolean.parseBoolean(configuration.getProperty(title + "_visible")), Boolean.parseBoolean(configuration.getProperty(title + "_rect")), () -> {
				if (Minecraft.getMinecraft().currentScreen == this) return "Tickrate";
				return String.format("Tickrate: %s", TASmodClient.tickratechanger.ticksPerSecond);
			}));
			
			title = "position";
			y += 14;
			if (configuration.getProperty(title + "_x", "err").equals("err")) setDefaults(title, y);
			lists.add(new InfoLabel(title, Integer.parseInt(configuration.getProperty(title + "_x")), Integer.parseInt(configuration.getProperty(title + "_y")), Boolean.parseBoolean(configuration.getProperty(title + "_visible")), Boolean.parseBoolean(configuration.getProperty(title + "_rect")), () -> {
				if (Minecraft.getMinecraft().currentScreen == this) return "XYZ";
				return String.format("%.2f %.2f %.2f", Minecraft.getMinecraft().player.posX, Minecraft.getMinecraft().player.posY, Minecraft.getMinecraft().player.posZ);
			}));
			
			title = "position2";
			y += 14;
			if (configuration.getProperty(title + "_x", "err").equals("err")) setDefaults(title, y);
			lists.add(new InfoLabel(title, Integer.parseInt(configuration.getProperty(title + "_x")), Integer.parseInt(configuration.getProperty(title + "_y")), Boolean.parseBoolean(configuration.getProperty(title + "_visible")), Boolean.parseBoolean(configuration.getProperty(title + "_rect")), () -> {
				if (Minecraft.getMinecraft().currentScreen == this) return "Precise XYZ";
				return String.format("%f %f %f", Minecraft.getMinecraft().player.posX, Minecraft.getMinecraft().player.posY, Minecraft.getMinecraft().player.posZ);
			}));
			
			title = "chunkpos";
			y += 14;
			if (configuration.getProperty(title + "_x", "err").equals("err")) setDefaults(title, y);
			lists.add(new InfoLabel(title, Integer.parseInt(configuration.getProperty(title + "_x")), Integer.parseInt(configuration.getProperty(title + "_y")), Boolean.parseBoolean(configuration.getProperty(title + "_visible")), Boolean.parseBoolean(configuration.getProperty(title + "_rect")), () -> {
				if (Minecraft.getMinecraft().currentScreen == this) return "Chunk Position";
				return String.format("%d %d", Minecraft.getMinecraft().player.chunkCoordX, Minecraft.getMinecraft().player.chunkCoordZ);
			}));
			
			title = "worldseed";
			y += 14;
			if (configuration.getProperty(title + "_x", "err").equals("err")) setDefaults(title, y);
			lists.add(new InfoLabel(title, Integer.parseInt(configuration.getProperty(title + "_x")), Integer.parseInt(configuration.getProperty(title + "_y")), Boolean.parseBoolean(configuration.getProperty(title + "_visible")), Boolean.parseBoolean(configuration.getProperty(title + "_rect")), () -> {
				if (Minecraft.getMinecraft().currentScreen == this) return "Worldseed";
				return "World Seed: " + Minecraft.getMinecraft().world.getWorldInfo().getSeed();
			}));
			
			y += 14;
			
			if(TASmod.ktrngHandler.isLoaded()) {
				title = "ktrng_randomseed";
				if (configuration.getProperty(title + "_x", "err").equals("err")) setDefaults(title, y);
				lists.add(new InfoLabel(title, Integer.parseInt(configuration.getProperty(title + "_x")), Integer.parseInt(configuration.getProperty(title + "_y")), Boolean.parseBoolean(configuration.getProperty(title + "_visible")), Boolean.parseBoolean(configuration.getProperty(title + "_rect")), () -> {
					if (Minecraft.getMinecraft().currentScreen == this) return "KTRNG";
					return "RandomSeed: " + TASmod.ktrngHandler.getGlobalSeedClient();
				}));
			}
			
			title = "facing";
			y += 14;
			if (configuration.getProperty(title + "_x", "err").equals("err")) setDefaults(title, y);
			lists.add(new InfoLabel(title, Integer.parseInt(configuration.getProperty(title + "_x")), Integer.parseInt(configuration.getProperty(title + "_y")), Boolean.parseBoolean(configuration.getProperty(title + "_visible")), Boolean.parseBoolean(configuration.getProperty(title + "_rect")), () -> {
				if (Minecraft.getMinecraft().currentScreen == this) return "Facing";
				return String.format("%.2f %.2f", InterpolationHandler.rotationYaw, InterpolationHandler.rotationPitch);
			}));
			
			title = "cticks";
			y += 14;
//			if (configuration.getProperty(title + "_x", "err").equals("err")) setDefaults(title, y);
//			lists.add(new InfoLabel(title, Integer.parseInt(configuration.getProperty(title + "_x")), Integer.parseInt(configuration.getProperty(title + "_y")), Boolean.parseBoolean(configuration.getProperty(title + "_visible")), Boolean.parseBoolean(configuration.getProperty(title + "_rect")), () -> {
//				if (Minecraft.getMinecraft().currentScreen == this) return "Client Ticks";
//				return "Client Ticks: " + ClientProxy.ticksyncClient.getClienttickcounter();
//			}));
			
			title = "sticks";
			y += 14;
//			if (configuration.getProperty(title + "_x", "err").equals("err")) setDefaults(title, y);
//			lists.add(new InfoLabel(title, Integer.parseInt(configuration.getProperty(title + "_x")), Integer.parseInt(configuration.getProperty(title + "_y")), Boolean.parseBoolean(configuration.getProperty(title + "_visible")), Boolean.parseBoolean(configuration.getProperty(title + "_rect")), () -> {
//				if (Minecraft.getMinecraft().currentScreen == this) return "Server Ticks";
//				return "Server Ticks: " + ClientProxy.ticksyncClient.getServertickcounter();
//			}));
			
			title = "nextxyz";
			y += 14;
//			if (configuration.getProperty(title + "_x", "err").equals("err")) setDefaults(title, y);
//			lists.add(new InfoLabel(title, Integer.parseInt(configuration.getProperty(title + "_x")), Integer.parseInt(configuration.getProperty(title + "_y")), Boolean.parseBoolean(configuration.getProperty(title + "_visible")), Boolean.parseBoolean(configuration.getProperty(title + "_rect")), () -> {
//				if (Minecraft.getMinecraft().currentScreen == this) return "Predicted Position";
//				return String.format("%f %f %f", PlayerPositionCalculator.xNew, PlayerPositionCalculator.yNew, PlayerPositionCalculator.zNew);
//			}));
			
			title = "state";
			y += 14;
			if (configuration.getProperty(title + "_x", "err").equals("err")) setDefaults(title, y);
			lists.add(new InfoLabel(title, Integer.parseInt(configuration.getProperty(title + "_x")), Integer.parseInt(configuration.getProperty(title + "_y")), Boolean.parseBoolean(configuration.getProperty(title + "_visible")), Boolean.parseBoolean(configuration.getProperty(title + "_rect")), () -> {
				if (Minecraft.getMinecraft().currentScreen == this) {
					return "State";
				} else {
					TASstate state = TASmodClient.virtual.getContainer().getState();
					ChatFormatting format = ChatFormatting.WHITE;
					String out = "";
					if (state == TASstate.PLAYBACK) {
						out = "Playback";
						format = ChatFormatting.GREEN;
					} else if (state == TASstate.RECORDING) {
						out = "Recording";
						format = ChatFormatting.RED;
					} else if (state == TASstate.PAUSED) {
						out = "Paused";
						format = ChatFormatting.YELLOW;
					} else if (state == TASstate.NONE) {
						out = "";
					}
					return String.format("%s%s", format, out);
				}
			}));
			
			title = "cursor";
			y += 14;
			if (configuration.getProperty(title + "_x", "err").equals("err")) setDefaults(title, y);
			lists.add(new InfoLabel(title, Integer.parseInt(configuration.getProperty(title + "_x")), Integer.parseInt(configuration.getProperty(title + "_y")), Boolean.parseBoolean(configuration.getProperty(title + "_visible")), Boolean.parseBoolean(configuration.getProperty(title + "_rect")), () -> {
				if (Minecraft.getMinecraft().currentScreen == this) return "Mouse Position";
				return String.format("Mouse Cursor: " + TASmodClient.virtual.getNextMouse().getPath().get(0).cursorX + " " + TASmodClient.virtual.getNextMouse().getPath().get(0).cursorY);
			}));
			
			title = "trajectories";
			y += 14;
			if (configuration.getProperty(title + "_x", "err").equals("err")) setDefaults(title, y);
			lists.add(new InfoLabel(title, Integer.parseInt(configuration.getProperty(title + "_x")), Integer.parseInt(configuration.getProperty(title + "_y")), Boolean.parseBoolean(configuration.getProperty(title + "_visible")), Boolean.parseBoolean(configuration.getProperty(title + "_rect")), () -> {
				if (Minecraft.getMinecraft().currentScreen == this) return "Trajectories";
				String message = "Invalid Item";
				Vec3d vec = TrajectoriesCalculator.calculate();
				if (vec != null) {
					message = String.format("%.3f %.3f %.3f", vec.x, vec.y, vec.z);
				}
				return String.format("Trajectories: " + message);
			}));
			
			title = "velocity";
			y += 14;
			if (configuration.getProperty(title + "_x", "err").equals("err")) setDefaults(title, y);
			lists.add(new InfoLabel(title, Integer.parseInt(configuration.getProperty(title + "_x")), Integer.parseInt(configuration.getProperty(title + "_y")), Boolean.parseBoolean(configuration.getProperty(title + "_visible")), Boolean.parseBoolean(configuration.getProperty(title + "_rect")), () -> {
				if (Minecraft.getMinecraft().currentScreen == this) return "Velocity";
				return "Velocity: " + Minecraft.getMinecraft().player.motionX + " " + Minecraft.getMinecraft().player.motionY + " " + Minecraft.getMinecraft().player.motionZ;
			}));
			
			title = "desyncstatus";
			y += 14;
			if (configuration.getProperty(title + "_x", "err").equals("err")) setDefaults(title, y);
			lists.add(new InfoLabel(title, Integer.parseInt(configuration.getProperty(title + "_x")), Integer.parseInt(configuration.getProperty(title + "_y")), Boolean.parseBoolean(configuration.getProperty(title + "_visible")), Boolean.parseBoolean(configuration.getProperty(title + "_rect")), () -> {
				if (Minecraft.getMinecraft().currentScreen == this) return "Desync";
				DesyncMonitoring dMonitor=TASmodClient.virtual.getContainer().desyncMonitor;
				return dMonitor.getStatus(Minecraft.getMinecraft().player);
			}));
			
			title = "desyncstatusMotion";
			y += 14;
			if (configuration.getProperty(title + "_x", "err").equals("err")) setDefaults(title, y);
			lists.add(new InfoLabel(title, Integer.parseInt(configuration.getProperty(title + "_x")), Integer.parseInt(configuration.getProperty(title + "_y")), Boolean.parseBoolean(configuration.getProperty(title + "_visible")), Boolean.parseBoolean(configuration.getProperty(title + "_rect")), () -> {
				if (Minecraft.getMinecraft().currentScreen == this) return "Desync Motion";
				DesyncMonitoring dMonitor=TASmodClient.virtual.getContainer().desyncMonitor;
				return dMonitor.getMotion();
			}));
			
			title = "desyncstatusPos";
			y += 14;
			if (configuration.getProperty(title + "_x", "err").equals("err")) setDefaults(title, y);
			lists.add(new InfoLabel(title, Integer.parseInt(configuration.getProperty(title + "_x")), Integer.parseInt(configuration.getProperty(title + "_y")), Boolean.parseBoolean(configuration.getProperty(title + "_visible")), Boolean.parseBoolean(configuration.getProperty(title + "_rect")), () -> {
				if (Minecraft.getMinecraft().currentScreen == this) return "Desync Position";
				DesyncMonitoring dMonitor=TASmodClient.virtual.getContainer().desyncMonitor;
				return dMonitor.getPos();
			}));
			
			y += 14;
			title = "ktrng_desync";
			if (configuration.getProperty(title + "_x", "err").equals("err")) setDefaults(title, y);
			lists.add(new InfoLabel(title, Integer.parseInt(configuration.getProperty(title + "_x")), Integer.parseInt(configuration.getProperty(title + "_y")), Boolean.parseBoolean(configuration.getProperty(title + "_visible")), Boolean.parseBoolean(configuration.getProperty(title + "_rect")), () -> {
				if (Minecraft.getMinecraft().currentScreen == this) return "Desync KTRNG";
				DesyncMonitoring dMonitor=TASmodClient.virtual.getContainer().desyncMonitor;
				return dMonitor.getSeed();
			}));

			
			y = height - 28;
			title = "playback_index";
			if (configuration.getProperty(title + "_x", "err").equals("err")) setDefaults(title, y, true);
			lists.add(new InfoLabel(title, Integer.parseInt(configuration.getProperty(title + "_x")), Integer.parseInt(configuration.getProperty(title + "_y")), Boolean.parseBoolean(configuration.getProperty(title + "_visible")), Boolean.parseBoolean(configuration.getProperty(title + "_rect")), () -> {
				if (Minecraft.getMinecraft().currentScreen == this) return "PlaybackIndex";
				return Integer.toString(TASmodClient.virtual.getContainer().index());
			}));
			
			y = height - 14;
			title = "keystrokes";
			if (configuration.getProperty(title + "_x", "err").equals("err")) setDefaults(title, y, true);
			lists.add(new InfoLabel(title, Integer.parseInt(configuration.getProperty(title + "_x")), Integer.parseInt(configuration.getProperty(title + "_y")), Boolean.parseBoolean(configuration.getProperty(title + "_visible")), Boolean.parseBoolean(configuration.getProperty(title + "_rect")), () -> {
				if (Minecraft.getMinecraft().currentScreen == this) return "Keystrokes";
				return keystrokes();
			}));
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return true;
	}
	
	/**
	 * Render the Info Hud only
	 */
	@Override
	public void onDrawHotbar() {
		// render custom info box if control byte is set
		if (!ControlByteHandler.hideInfoBox && TASmodClient.virtual.getContainer().isPlayingback())
			drawRectWithText(ControlByteHandler.text, 10, 10, true);
		// skip rendering of control byte is set
		if (!ControlByteHandler.shouldRenderHud && TASmodClient.virtual.getContainer().isPlayingback())
			return;
		int xpos=40;
		int ypos=190;
		for (InfoLabel label : lists) {
			label.tick();
			
			int lx = label.x;
			int ly = label.y;

			Pair<Integer, Integer> newPos = getScreenOffset(lx, ly, label);
			
			lx = newPos.getLeft();
			ly = newPos.getRight();
			
			if (label.visible) {
				drawRectWithText(label.renderText, lx, ly, label.renderRect);
			} else if (Minecraft.getMinecraft().currentScreen != null) {
				if (Minecraft.getMinecraft().currentScreen.getClass().getSimpleName().contains("InfoHud")) {
		         	Minecraft.getMinecraft().fontRenderer.drawStringWithShadow(label.renderText, label.x + 2, label.y + 3, 0x60FFFFFF);
				}
			}
			if(Minecraft.getMinecraft().currentScreen instanceof InfoHud) {
				Minecraft.getMinecraft().fontRenderer.drawStringWithShadow("Leftclick to move", width-ypos, xpos- 30, 0x60FF00);
				Minecraft.getMinecraft().fontRenderer.drawStringWithShadow("Rightclick to enable", width-ypos, xpos-20, 0x60FF00);
				Minecraft.getMinecraft().fontRenderer.drawStringWithShadow("Middleclick to add black background", width-ypos, xpos-10, 0x60FF00);
				Minecraft.getMinecraft().fontRenderer.drawStringWithShadow("Hold Shift to snap to grid", width-ypos, xpos, 0x60FF00);
				Minecraft.getMinecraft().fontRenderer.drawStringWithShadow("CTRL+Shift+R to reset the layout", width - ypos, xpos + 10, 0xEE8100);
				
				if (isCtrlKeyDown() && isShiftKeyDown() && TASmodClient.virtual.isKeyDown(Keyboard.KEY_R)) {
					resetLayout = true;
					configuration = null;
				}
			}
		}
		ScaledResolution scaled = new ScaledResolution(Minecraft.getMinecraft());
		drawCenteredString(Minecraft.getMinecraft().fontRenderer, "TASmod is still in development! Major issues may arise!", scaled.getScaledWidth() / 2, scaled.getScaledHeight() - 50, 0xFF8400);
//		drawCenteredString(Minecraft.getMinecraft().fontRenderer, Float.toString(TASmod.tickratechanger.ticksPerSecond), scaled.getScaledWidth() / 2, scaled.getScaledHeight() - 36, 0xFFFFFF);
	}
	
	/**
	 * Renders a Box with Text in it
	 */
	private void drawRectWithText(String text, int x, int y, boolean rect) {
		if (rect) drawRect(x, y, x + Minecraft.getMinecraft().fontRenderer.getStringWidth(text) + 4, y + 14, 0x60000000);
		Minecraft.getMinecraft().fontRenderer.drawStringWithShadow(text, x + 2, y + 3, 0xFFFFFF);
		GL11.glEnable(3042 /*GL_BLEND*/);
	}
	
	private String keystrokes() {
		if (Display.isActive()) {
			String out1 = ""+ChatFormatting.WHITE;
			for (String mouse : TASmodClient.virtual.getCurrentMousePresses()) {
				out1 = out1.concat(mouse + " ");
			}
			out1=out1.concat(""+ChatFormatting.GREEN);
			for (String mouse : TASmodClient.virtual.getNextMousePresses()) {
				out1 = out1.concat(mouse + " ");
			}
			
			String out2 = ""+ChatFormatting.WHITE;
			for (String key : TASmodClient.virtual.getCurrentKeyboardPresses()) {
				out2 = out2.concat(key + " ");
			}
			out2=out2.concat(""+ChatFormatting.GREEN);
			for (String key : TASmodClient.virtual.getNextKeyboardPresses()) {
				out2 = out2.concat(key + " ");
			}
			return out1+out2;
		}
		return "";
	}
	
	private Pair<Integer, Integer> getScreenOffset(int x, int y, InfoLabel label){
		ScaledResolution scaled = new ScaledResolution(Minecraft.getMinecraft());
		
		int marginX = 5;
		int marginY = 5;
		
		if (getBBRight(x, label.renderText) > scaled.getScaledWidth()) {
			int offset = getBBRight(x, label.renderText);
			x = x - (offset - scaled.getScaledWidth()) - marginX;
		}

		if (getBBDown(y) > scaled.getScaledHeight()) {
			int offset = getBBDown(y);
			y = y - (offset - scaled.getScaledHeight()) - marginY;
		}
		
		return Pair.of(x, y);
	}

	private int getBBRight(int x, String text) {
		return x + Minecraft.getMinecraft().fontRenderer.getStringWidth(text);
	}

	private int getBBDown(int y) {
		return y + 14;
	}
	
}