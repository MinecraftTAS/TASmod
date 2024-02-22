package com.minecrafttas.tasmod.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.client.gui.GuiWorldSelection;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiContainer;

/**
 * Normalizes the cursor to be independent of gui scalings.<br>
 * That way, a TAS recorded in e.g. Gui Scale "Large" can also be played back on Gui Scale "Small"
 *
 * @author Scribble, Darkmoon
 */
public class PointerNormalizer {

	/**
	 * Mathematically removes scaling from the x coordinate
	 * @param pointerX The current pointer coordinate
	 * @return The normalized x coordinate
	 */
	public static int getNormalizedX(int pointerX) {
		Minecraft mc = Minecraft.getMinecraft();
		ScaledResolution scaled = new ScaledResolution(mc);
		return (int) (pointerX - (scaled.getScaledWidth() / 2D));
	}

	/**
	 * Mathematically removes scaling from the y coordinate
	 * @param pointerY The current pointer coordinate
	 * @return The normalized y coordinate
	 */
	public static int getNormalizedY(int pointerY) {
		Minecraft mc = Minecraft.getMinecraft();
		ScaledResolution scaled = new ScaledResolution(mc);

		int out = pointerY;

		if (mc.currentScreen instanceof GuiContainer) {
			out = (int) (pointerY - (scaled.getScaledHeight() / 2D));
		} else if (mc.currentScreen instanceof GuiWorldSelection|| mc.currentScreen instanceof GuiMultiplayer) {
			// TODO Figure out what to do here
		} else {
			out = (int) (pointerY - (scaled.getScaledHeight() / 4 + 72 + -16));
		}

		return out;
	}

	/**
	 * Reapplies gui scaling to the normalized pointer x coordinate
	 * @param normalizedX The normalized pointer coordinate
	 * @return The scaled coordinate
	 */
	public static int reapplyScalingX(int normalizedX) {
		Minecraft mc = Minecraft.getMinecraft();
		ScaledResolution scaled = new ScaledResolution(mc);
		int out = (int) Math.round(normalizedX + (scaled.getScaledWidth() / 2D));
		return clamp(out, 0, scaled.getScaledWidth());
	}

	/**
	 * Reapplies gui scaling to the normalized pointer y coordinate
	 * @param normalizedY The normalized pointer coordinate
	 * @return The scaled coordinate
	 */
	public static int reapplyScalingY(int normalizedY) {
		Minecraft mc = Minecraft.getMinecraft();
		ScaledResolution scaled = new ScaledResolution(mc);

		int out = normalizedY;
		if (mc.currentScreen instanceof GuiContainer) {
			out = (int) Math.round(normalizedY + (scaled.getScaledHeight() / 2D));
		} else if (mc.currentScreen instanceof GuiWorldSelection || mc.currentScreen instanceof GuiMultiplayer) {
			// TODO Figure out what to do here
		} else {
			out = (int) (normalizedY + (scaled.getScaledHeight() / 4 + 72 + -16));
		}

		return clamp(out, 0, scaled.getScaledHeight());
	}

	private static int clamp(int value, int lower, int upper) {
		if (value < lower) {
			return lower;
		} else {
			return Math.min(value, upper);
		}
	}

	public static void printAspectRatio() {
		int height = Minecraft.getMinecraft().displayHeight;
		int width = Minecraft.getMinecraft().displayWidth;
		int gcd = greatestCommonDivisor(width, height);
		if (gcd == 0) {
			System.out.println(gcd);
		} else {
			System.out.println(width / gcd + ":" + height / gcd);
		}
	}

	private static int greatestCommonDivisor(int a, int b) {
		return (b == 0) ? a : greatestCommonDivisor(b, a % b);
	}

	/*
	 * Here lies 10 hours of work for something I didn't even use. This code
	 * normalizes the pointers coordinates and scales it depending on the screen
	 * width and height. After 10 hours of trial and error, I finally managed to
	 * make it work, only to realize that this has no use whatsoever. The guis don't
	 * work this way and you can't even use the pointer properly... But now I have
	 * made it so I will let it stay here until I find a use, to spare me another 10
	 * hours.
	 */

//	private double getNormalizedXOld(double pointerX) {
//		Minecraft mc = Minecraft.getMinecraft();
//		ScaledResolution scaled = new ScaledResolution(mc);
//		return (double) pointerX / (double) mc.displayWidth / (4D / (double) scaled.getScaleFactor());
//	}
//
//	public static double getNormalizedYOld(int pointerY) {
//		Minecraft mc = Minecraft.getMinecraft();
//		ScaledResolution scaled = new ScaledResolution(mc);
//		double out = (double) pointerY / (double) mc.displayHeight / (4D / (double) scaled.getScaleFactor());
//		return out;
//	}
//
//	public static int getCoordsXOld(double normalizedX) {
//		Minecraft mc = Minecraft.getMinecraft();
//		ScaledResolution scaled = new ScaledResolution(mc);
//		double guiScaled = normalizedX * (double) mc.displayWidth * (4D / (double) scaled.getScaleFactor());
//		int out = (int) Math.round(guiScaled);
//		return out;
//	}
//
//	public static int getCoordsYOld(double normalizedY) {
//		Minecraft mc = Minecraft.getMinecraft();
//		ScaledResolution scaled = new ScaledResolution(mc);
//		double guiScaled = normalizedY * (double) mc.displayHeight * (4D / (double) scaled.getScaleFactor());
//		int out = (int) Math.round(guiScaled);
//		return out;
//	}

}
