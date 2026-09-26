package com.minecrafttas.tasmod.playback.metadata;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import com.minecrafttas.tasmod.mctcommon.registry.AbstractRegistry;
import com.minecrafttas.tasmod.TASmod;
import com.minecrafttas.tasmod.events.EventPlaybackClient;
import com.minecrafttas.tasmod.playback.metadata.PlaybackMetadata.PlaybackMetadataExtension;

/**
 * Registry for registering custom metadata that is stored in the TASfile.<br>
 * <br>
 * The default metadata includes general information such as author name,
 * savestate/rerecord count and category.<br>
 * <br>
 * Any custom class has to extend PlaybackMetadataExtension
 * 
 * @author Scribble
 */
public class PlaybackMetadataRegistry extends AbstractRegistry<PlaybackMetadataExtension> implements EventPlaybackClient.EventRecordClear {

	public PlaybackMetadataRegistry() {
		super("METADATA_REGISTRY", new LinkedHashMap<>());
	}

	public static void handleOnCreate() {

	}

	public List<PlaybackMetadata> handleOnStore() {
		List<PlaybackMetadata> metadataList = new ArrayList<>();
		for (PlaybackMetadataExtension extension : REGISTRY.values()) {
			metadataList.add(extension.onStore());
		}
		return metadataList;
	}

	public void handleOnLoad(List<PlaybackMetadata> meta) {
		if (meta.isEmpty())
			return;
		for (PlaybackMetadata metadata : meta) {
			if (REGISTRY.containsKey(metadata.getExtensionName())) {
				PlaybackMetadataExtension extension = REGISTRY.get(metadata.getExtensionName());

				extension.onLoad(metadata);
			} else {
				TASmod.LOGGER.warn("The metadata extension {} was not found while loading the TASfile. Things might not be correctly loaded!", metadata.getExtensionName());
			}
		}
	}

	@Override
	public void onRecordingClear() {
		REGISTRY.forEach((key, extension) -> {
			extension.onClear();
		});
	}
}

