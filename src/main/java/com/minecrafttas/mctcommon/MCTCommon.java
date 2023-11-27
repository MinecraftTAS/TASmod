package com.minecrafttas.mctcommon;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

public class MCTCommon {
	public static final Logger LOGGER = LogManager.getLogger("MCTCommon");
	
	public static final Marker Event = MarkerManager.getMarker("Event");
	
	public static final Marker Server = MarkerManager.getMarker("Server");
	
	public static final Marker Client = MarkerManager.getMarker("Client");
	
	public static final Marker Timeout = MarkerManager.getMarker("Timeout");

}
