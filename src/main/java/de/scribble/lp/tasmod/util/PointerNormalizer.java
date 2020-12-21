package de.scribble.lp.tasmod.util;

import java.awt.MouseInfo;

import org.lwjgl.opengl.Display;

import net.minecraft.client.Minecraft;
/**
 * Normalizes pointer coordinates so it works on different screen and window positions
 * @author ScribbleLP, Darkmoon
 *
 */
public class PointerNormalizer {
	public static double getNormalizedX(int pointerX) {
		Minecraft mc=Minecraft.getMinecraft();
		double out = (double)(pointerX-Display.getX())/(double)mc.displayWidth;
		return limiterX(out,mc);
	}
	public static double getNormalizedY(int pointerY) {
		Minecraft mc=Minecraft.getMinecraft();
		double out = (double)(pointerY-Display.getY())/(double)mc.displayHeight;
		return limiterY(out, mc);
	}
	public static int getCoordsX(double normalizedX) {
		Minecraft mc=Minecraft.getMinecraft();
		int out=(int) ((limiterX(normalizedX,mc)*(double)mc.displayWidth)+(double)Display.getX());
		System.out.println(out);
		return out;
	}
	public static int getCoordsY(double normalizedY) {
		Minecraft mc=Minecraft.getMinecraft();
		int out=(int) ((limiterY(normalizedY, mc)*(double)mc.displayHeight)+(double)Display.getY());
		return out;
	}
	private static double limiterX(double out, Minecraft mc) {
		if(!mc.isFullScreen()) {
			if(out<0D) out=0D;
			if(out>1D) out=1D;
		}
		return out;
	}
	private static double limiterY(double out, Minecraft mc) {
		if(!mc.isFullScreen()) {
			if(out<0.05D) out=0.05D;
			if(out>1D) out=1D;
		}
		return out;
	}
}
