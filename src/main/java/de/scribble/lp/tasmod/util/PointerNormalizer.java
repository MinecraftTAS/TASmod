package de.scribble.lp.tasmod.util;

import java.awt.MouseInfo;

import org.lwjgl.opengl.Display;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
/**
 * Normalizes pointer coordinates so it works on different screen and window positions
 * @author ScribbleLP, Darkmoon
 *
 */
public class PointerNormalizer {
	public static double getNormalizedX(int pointerX) {
		Minecraft mc=Minecraft.getMinecraft();
		ScaledResolution scaled=new ScaledResolution(mc);
		double out = (double) pointerX/(double) mc.displayWidth/(4D/(double)scaled.getScaleFactor());
		return out;
	}
	public static double getNormalizedY(int pointerY) {
		Minecraft mc=Minecraft.getMinecraft();
		ScaledResolution scaled=new ScaledResolution(mc);
		double out=(double) pointerY/(double) mc.displayHeight/(4D/(double)scaled.getScaleFactor());
		return out;
	}
	public static int getCoordsX(double normalizedX) {
		Minecraft mc=Minecraft.getMinecraft();
		ScaledResolution scaled=new ScaledResolution(mc);
		int out=(int) Math.round(normalizedX*(double) mc.displayWidth*(4D/(double)scaled.getScaleFactor()));
		return out;
	}
	public static int getCoordsY(double normalizedY) {
		Minecraft mc=Minecraft.getMinecraft();
		ScaledResolution scaled=new ScaledResolution(mc);
		int out=(int) Math.round(normalizedY*(double) mc.displayHeight*(4D/(double)scaled.getScaleFactor()));
		return out;
	}
}
