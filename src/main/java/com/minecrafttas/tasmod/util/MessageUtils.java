package com.minecrafttas.tasmod.util;

public class MessageUtils {

	public static void splitNewline(String phrase, GuiInterface method) {
		splitNewline(phrase, 10, method);
	}

	public static void splitNewline(String phrase, int distance, GuiInterface method) {
		int y = 0;
		String[] lines = phrase.split("\r?\n");
		for (String line : lines) {
			method.draw(line, y);
			y += distance;
		}
	}

	@FunctionalInterface
	public interface GuiInterface {

		public void draw(String line, int y);
	}
}

