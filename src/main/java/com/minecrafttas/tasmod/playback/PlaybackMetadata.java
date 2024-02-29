package com.minecrafttas.tasmod.playback;

import java.util.LinkedHashMap;
import java.util.Properties;

/**
 * Stores all tasfile specific metadata like author, rerecords and name.<br>
 * <br>
 * Each metadata is grouped by a group name.<br>
 * Made to be easily modifiable so that you can add your own metadata.
 */
public class PlaybackMetadata {
	LinkedHashMap<String, Properties> metadata;
	
	public PlaybackMetadata() {
		this.metadata = new LinkedHashMap<>();
	}
	
	
}
