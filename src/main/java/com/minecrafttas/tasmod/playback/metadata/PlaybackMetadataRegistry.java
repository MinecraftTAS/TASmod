package com.minecrafttas.tasmod.playback.metadata;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import com.minecrafttas.mctcommon.MCTCommon;

/**
 * Registry for registering custom metadata that is stored in the TASFile.<br>
 * <br>
 * The default metadata includes general information such as author name, savestate/rerecord count and category.<br>
 * <br>
 * Any custom class has to extend PlaybackMetadataExtension
 * 
 */
public class PlaybackMetadataRegistry {

	private static final ArrayList<PlaybackMetadataExtension> METADATA_EXTENSION = new ArrayList<>();

	/**
	 * Registers a new class as a metadata extension
	 * @param extension
	 */
	public static void register(PlaybackMetadataExtension extension) {
		if(extension==null) {
			throw new NullPointerException("Tried to register a playback extension with value null");
		}
		
		if(containsClass(extension)) {
			MCTCommon.LOGGER.warn("Trying to register the playback extension {}, but another instance of this class is already registered!", extension.getClass().getName());
			return;
		}
		
		if (!METADATA_EXTENSION.contains(extension)) {
			METADATA_EXTENSION.add(extension);
		} else {
			MCTCommon.LOGGER.warn("Trying to register the playback extension {}, but it is already registered!", extension.getClass().getName());
		}
	}

	public static void unregister(PlaybackMetadataExtension extension) {
		if(extension==null) {
			throw new NullPointerException("Tried to unregister an extension with value null");
		}
		if (METADATA_EXTENSION.contains(extension)) {
			METADATA_EXTENSION.remove(extension);
		} else {
			MCTCommon.LOGGER.warn("Trying to unregister the playback extension {}, but it was not registered!", extension.getClass().getName());
		}
	}
	
	public static void handleOnCreate() {
		
	}
	
	public static LinkedHashMap<String, PlaybackMetadata> handleOnLoad() {
		return null;
	}
	
	public static void handleOnStore(LinkedHashMap<String, PlaybackMetadata> metadata) {
	}
	
	private static boolean containsClass(PlaybackMetadataExtension newExtension) {
		for(PlaybackMetadataExtension extension : METADATA_EXTENSION) {
			if(extension.getClass().equals(newExtension.getClass())) {
				return true;
			}
		}
		return false;
	}

	public static interface PlaybackMetadataExtension {
		
		public String getExtensionName();
		
		public void onCreate();
		
		public PlaybackMetadata onStore();

		public void onLoad(PlaybackMetadata metadata);
	}
}
