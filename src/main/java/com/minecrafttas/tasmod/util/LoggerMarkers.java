package com.minecrafttas.tasmod.util;

import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

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
 * 	&lt;Filters&gt;
 * 		&lt;MarkerFilter marker="Event" onMatch="${sys:tasmod.marker.event:-ACCEPT}" onMismatch="NEUTRAL" /&gt;
 * 	&lt;/Filters&gt;
 * 	</pre>
 * 
 * @author Scribble
 *
 */
public class LoggerMarkers {
	
	public static final Marker Event = MarkerManager.getMarker("Event");
	
	public static final Marker Savestate = MarkerManager.getMarker("Savestate");
	
	public static final Marker Networking = MarkerManager.getMarker("Networking");
	
	public static final Marker Tickrate = MarkerManager.getMarker("Tickrate");

	public static final Marker Playback = MarkerManager.getMarker("Playback");
}
