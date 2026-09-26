package com.minecrafttas.tasmod.playback.metadata;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.minecrafttas.tasmod.mctcommon.registry.Registerable;

/**
 * Stores a section of<br>
 * <br>
 */
public class PlaybackMetadata {

	private String extensionName;
	private LinkedHashMap<String, String> data;

	private static String SEPERATOR = ":";

	public PlaybackMetadata(PlaybackMetadataExtension extension) {
		this(extension.getExtensionName());
	}

	public PlaybackMetadata(String extensionName) {
		this.extensionName = extensionName;
		this.data = new LinkedHashMap<String, String>();
	}

	public PlaybackMetadata(String extensionName, LinkedHashMap<String, String> data) {
		this.extensionName = extensionName;
		this.data = data;
	}

	public void setValue(String key, String value) {
		data.put(key, value);
	}

	public void setValue(Object key, String value) {
		setValue(key.toString(), value);
	}

	public String getValue(String key) {
		return data.get(key);
	}

	public String getValue(Object key) {
		return getValue(key.toString());
	}

	@Override
	public String toString() {
		String out = "";
		for (String key : data.keySet()) {
			String value = getValue(key);
			out += (String.format("%s%s%s\n", key, SEPERATOR, value));
		}
		return out;
	}

	public List<String> toStringList() {
		List<String> out = new ArrayList<>();
		for (Object keyObj : data.keySet()) {
			String key = (String) keyObj;
			String value = getValue(key);
			out.add(String.format("%s%s%s\n", key, SEPERATOR, value));
		}
		return out;
	}

	public String getExtensionName() {
		return extensionName;
	}

	public LinkedHashMap<String, String> getData() {
		return data;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof PlaybackMetadata) {
			PlaybackMetadata other = (PlaybackMetadata) obj;
			return other.data.equals(data) && other.extensionName.equals(extensionName);
		}
		return super.equals(obj);
	}

	public static PlaybackMetadata fromStringList(String extensionName, List<String> list) {
		PlaybackMetadata out = new PlaybackMetadata(extensionName);

		final Pattern pattern = Pattern.compile("(\\w+)\\" + SEPERATOR + "(.+)");

		for (String data : list) {
			Matcher matcher = pattern.matcher(data);
			if (matcher.find()) {
				String key = matcher.group(1);
				String value = matcher.group(2);
				out.setValue(key, value);
			}
		}

		return out;
	}

	public static PlaybackMetadata fromHashMap(String extensionName, LinkedHashMap<String, String> data) {
		return new PlaybackMetadata(extensionName, new LinkedHashMap<>(data));
	}

	public static abstract class PlaybackMetadataExtension implements Registerable {

		/**
		 * Currently unused.<br>
		 * Maybe in the future, TASes have to be created with /create, then you can interactively set the values...<br>
		 */
		public void onCreate() {
		};

		/**
		 * Runs, when the TASfile is being stored to a file.<br>
		 * Create a new {@link PlaybackMetadata} with <code>PlaybackMetadata metadata = new PlaybackMetadata(this);</code>.<br>
		 * This will ensure, that the metadata is linked to this extension by using the {@link PlaybackMetadataExtension#getExtensionName()}.<br>
		 * 
		 * @return The {@link PlaybackMetadata} to be saved in the TASfile
		 */
		public abstract PlaybackMetadata onStore();

		/**
		 * Runs when the TASfile is being loaded from a file<br>
		 * 
		 * @param metadata The metadata for this extension to read from
		 */
		public abstract void onLoad(PlaybackMetadata metadata);

		/**
		 * Runs when the PlaybackController is cleared
		 */
		public abstract void onClear();
	}
}

