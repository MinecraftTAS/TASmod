package de.pfannekuchen.tasmod.controlbytes;

/**
 * Handles playback control bytes
 * @author Pancake
 */
public class ControlByteHandler {

	/**
	 * Resets all control-byte-controlled settings
	 */
	public static void reset() {
		ControlByteHandler.hideInfoBox = true;
		ControlByteHandler.text = "";
		ControlByteHandler.shouldInterpolate = false;
		ControlByteHandler.shouldRenderHud = true;
	}
	
	/**
	 * Reacts to control bytes
	 * @param command Control Command
	 * @param args Arguments
	 */
	public static void onControlByte(String command, String[] args) {
		switch (command.toLowerCase()) {
			case "interpolation":
				interpolation(args);
				break;
			case "hud":
				hud(args);
				break;
			case "info":
				info(args);
			default:
				break;
		}
	}
	
	private static void info(String[] args) {
		ControlByteHandler.hideInfoBox = "off".equals(args[0].trim()) || "false".equals(args[0].trim()) || "no".equals(args[0].trim()) || "0".equals(args[0].trim());
		// Parse array as text
		ControlByteHandler.text = "";
		for (String string : args) {
			ControlByteHandler.text += " " + string;
		}
		ControlByteHandler.text = ControlByteHandler.text.trim();
	}

	public static void interpolation(String[] args) {
		ControlByteHandler.shouldInterpolate = "on".equals(args[0].trim()) || "true".equals(args[0].trim()) || "yes".equals(args[0].trim()) || "1".equals(args[0].trim());
	}

	public static void hud(String[] args) {
		ControlByteHandler.shouldRenderHud = "on".equals(args[0].trim()) || "true".equals(args[0].trim()) || "yes".equals(args[0].trim()) || "1".equals(args[0].trim());
	}
	
	public static boolean hideInfoBox = true;
	public static String text = "";
	public static boolean shouldInterpolate = false;
	public static boolean shouldRenderHud = true;
	
}
