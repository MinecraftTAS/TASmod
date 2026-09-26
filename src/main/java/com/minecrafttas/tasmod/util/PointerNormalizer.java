package com.minecrafttas.tasmod.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;

/**
 * Stub PointerNormalizer - GUI scaling needs rewrite for modern API
 */
public class PointerNormalizer {

    private static int getGuiWidth(Minecraft mc) {
        return mc.getWindow().getWidth(); // Simplified
    }

    private static int getGuiHeight(Minecraft mc) {
        return mc.getWindow().getHeight(); // Simplified
    }

    public static int getNormalizedX(int pointerX) {
        Minecraft mc = Minecraft.getInstance();
        int guiWidth = getGuiWidth(mc);
        return (int) (pointerX - (guiWidth / 2D));
    }

    public static int getNormalizedY(int pointerY) {
        Minecraft mc = Minecraft.getInstance();
        Screen currentScreen = getScreen(mc);
        int guiHeight = getGuiHeight(mc);

        int out = pointerY;

        if (currentScreen instanceof AbstractContainerScreen) {
            out = (int) (pointerY - (guiHeight / 2D));
        } else if (currentScreen != null && (currentScreen.getClass().getSimpleName().equals("WorldSelectionScreen") || currentScreen.getClass().getSimpleName().equals("MultiplayerScreen"))) {
            // TODO Figure out what to do here
        } else {
            out = (int) (pointerY - (guiHeight / 4 + 72 + -16));
        }

        return out;
    }

    public static int reapplyScalingX(int normalizedX) {
        Minecraft mc = Minecraft.getInstance();
        int guiWidth = getGuiWidth(mc);
        int out = (int) Math.round(normalizedX + (guiWidth / 2D));
        return clamp(out, 0, guiWidth);
    }

    public static int reapplyScalingY(int normalizedY) {
        Minecraft mc = Minecraft.getInstance();
        Screen currentScreen = getScreen(mc);
        int guiHeight = getGuiHeight(mc);

        int out = normalizedY;
        if (currentScreen instanceof AbstractContainerScreen) {
            out = (int) Math.round(normalizedY + (guiHeight / 2D));
        } else if (currentScreen != null && (currentScreen.getClass().getSimpleName().equals("WorldSelectionScreen") || currentScreen.getClass().getSimpleName().equals("MultiplayerScreen"))) {
            // TODO Figure out what to do here
        } else {
            out = (int) (normalizedY + (guiHeight / 4 + 72 + -16));
        }

        return clamp(out, 0, guiHeight);
    }

    private static int clamp(int value, int lower, int upper) {
        if (value < lower) {
            return lower;
        } else {
            return Math.min(value, upper);
        }
    }

    public static void printAspectRatio() {
        Minecraft mc = Minecraft.getInstance();
        int height = getGuiHeight(mc);
        int width = getGuiWidth(mc);
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

    private static Screen getScreen(Minecraft mc) {
        try {
            java.lang.reflect.Field field = Minecraft.class.getDeclaredField("screen");
            field.setAccessible(true);
            return (Screen) field.get(mc);
        } catch (Exception e) {
            return null;
        }
    }
}