package com.minecrafttas.tasmod.playback.metadata;

import java.util.ArrayList;

import com.minecrafttas.mctcommon.MCTCommon;

public class PlaybackMetadataRegistry {

	private static final ArrayList<PlaybackMetadataExtension> METADATA_EXTENSION = new ArrayList<>();

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
	
	public static PlaybackMetadata handleOnLoad() {
		return null; //TODO implement
	}
	
	public static void handleOnStore(PlaybackMetadata metadata) {
		//TODO
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
		public PlaybackMetadata onStore();

		public void onLoad(PlaybackMetadata metadata);
	}
}
