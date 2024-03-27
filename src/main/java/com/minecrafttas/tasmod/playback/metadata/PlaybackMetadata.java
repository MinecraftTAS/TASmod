package com.minecrafttas.tasmod.playback.metadata;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Stores a section of<br>
 * <br>
 */
public class PlaybackMetadata {
	/**
	 * Debug extension name
	 */
	String extensionName;
	Properties metadata;

	public PlaybackMetadata() {
		this.metadata = new Properties();
	}
	
	public PlaybackMetadata(String extensionName) {
		this();
		this.extensionName = extensionName; 
	}

	public void setValue(String key, String value) {
		if(key.contains("=")) {
			throw new IllegalArgumentException(String.format("%sKeyname %s can't contain =", extensionName!=null?extensionName+": ":"", key));
		}
	}

	public String getValue(String key) {
		return metadata.getProperty(key);
	}

	@Override
	public String toString() {
		String out = "";
		for (Object keyObj : metadata.keySet()) {
			String key = (String) keyObj;
			String value = getValue(key);
			out += (String.format("%s=%s\n", key, value));
		}
		return out;
	}
	
	public List<String> toStringList(){
		List<String> out= new ArrayList<>();
		for (Object keyObj : metadata.keySet()) {
			String key = (String) keyObj;
			String value = getValue(key);
			out.add(String.format("%s=%s\n", key, value));
		}
		return out;
	}
	
	public static PlaybackMetadata fromStringList(String extensionName, List<String> list) {
		return fromStringList(list);
	}
	
	public static PlaybackMetadata fromStringList(List<String> list) { 
		PlaybackMetadata out = new PlaybackMetadata();
		
        final Pattern pattern = Pattern.compile("(\\w+)=(.+)");
		
		for(String data: list) {
			Matcher matcher = pattern.matcher(data);
			if(matcher.find()) {
				String key = matcher.group(1);
				String value = matcher.group(2);
				out.setValue(key, value);
			}
		}
		
		return out;
	}
}
