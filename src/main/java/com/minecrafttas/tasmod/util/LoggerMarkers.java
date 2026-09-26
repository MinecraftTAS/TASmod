package com.minecrafttas.tasmod.util;

import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import com.minecrafttas.tasmod.events.EventClient.EventDrawHotbarAlways;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;

import com.mojang.blaze3d.systems.RenderSystem;

/**
 * <p>A list of Log4J markers which can be added to logging statements.
 * 
 * <p>Simply add the marker as the first argument:
 * <pre>LOGGER.info({@linkplain LoggerMarkers}.{@link #Event}, "Message");</pre>
 * 
 * <p>You can then turn off log messages by adding a VM option to your run configuration:
 * <pre>-Dtasmod.marker.event=DENY</pre>
 * 
 * <p>To add new markers, follow the pattern in this class then head to src/main/resources/log4j.xml.
 * There you can add the marker to the 'Filters' xml-tag
 * 
 * <pre>
 * 	<Filters>
 * 		<MarkerFilter marker="Event" onMatch="${sys:tasmod.marker.event:-ACCEPT}" onMismatch="NEUTRAL" />
 * 	</Filters>
 * 	</pre>
 * 
 * @author Scribble
 *
 */
public class LoggerMarkers implements EventDrawHotbarAlways {

	public static final Marker Event = MarkerManager.getMarker("Event");

	public static final Marker Savestate = MarkerManager.getMarker("Savestate");

	public static final Marker Networking = MarkerManager.getMarker("Networking");

	public static final Marker Tickrate = MarkerManager.getMarker("Tickrate");

	public static final Marker Playback = MarkerManager.getMarker("Playback");

	public static final Marker Keyboard = MarkerManager.getMarker("Keyboard");

	public static final Marker Mouse = MarkerManager.getMarker("Mouse");

	@Override
	public void onDrawHotbarAlways() {
		// TODO: Rewrite for newer mappings - ScaledResolution and GlStateManager not available
	}

	private void drawMarker(int posX, int posY, int textColor) {
		// TODO: Rewrite for newer mappings - Gui.drawRect not available
		// int alpha = 0x80000000;
		// Gui.drawRect(posX, posY, posX + 1, posY + 1, textColor + alpha);
	}
}