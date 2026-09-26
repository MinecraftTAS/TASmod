package com.minecrafttas.tasmod.playback.filecommands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

import com.minecrafttas.tasmod.mctcommon.Configuration;
import com.minecrafttas.tasmod.mctcommon.registry.AbstractRegistry;
import com.minecrafttas.tasmod.events.EventPlaybackClient;
import com.minecrafttas.tasmod.playback.PlaybackControllerClient.InputContainer;
import com.minecrafttas.tasmod.playback.filecommands.PlaybackFileCommand.PlaybackFileCommandExtension;
import com.minecrafttas.tasmod.playback.filecommands.PlaybackFileCommand.SortedFileCommandContainer;
import com.minecrafttas.tasmod.playback.filecommands.PlaybackFileCommand.UnsortedFileCommandContainer;
import com.minecrafttas.tasmod.registries.TASmodConfig;

public class PlaybackFileCommandsRegistry extends AbstractRegistry<PlaybackFileCommandExtension> implements EventPlaybackClient.EventRecordTick, EventPlaybackClient.EventPlaybackTick, EventPlaybackClient.EventRecordClear {

	private List<PlaybackFileCommandExtension> enabledExtensions = new ArrayList<>();

	private Configuration config = null;

	public PlaybackFileCommandsRegistry() {
		super("FILECOMMAND_REGISTRY", new LinkedHashMap<>());
	}

	@Override
	public void register(PlaybackFileCommandExtension extension) {
		super.register(extension);
		enabledExtensions = getEnabled();
	}

	@Override
	public void unregister(PlaybackFileCommandExtension extension) {
		super.unregister(extension);
		enabledExtensions = getEnabled();
	}

	public boolean setEnabled(String extensionName, boolean enabled) {
		return setEnabled(extensionName, enabled, true);
	}

	public boolean setEnabled(String extensionName, boolean enabled, boolean saveToConfig) {
		PlaybackFileCommandExtension extension = REGISTRY.get(extensionName);
		if (extension == null) {
			return false;
		}
		extension.setEnabled(enabled);
		enabledExtensions = getEnabled();

		if (saveToConfig) {
			saveConfig();
		}
		return true;
	}

	private void disableAll() {
		REGISTRY.forEach((name, value) -> {
			value.setEnabled(false);
		});
	}

	public void setEnabled(String... extensionNames) {
		setEnabled(Arrays.asList(extensionNames));
	}

	public void setEnabled(List<String> extensionNames) {
		setEnabled(extensionNames, false);
	}

	public void setEnabled(List<String> extensionNames, boolean saveToConfig) {
		disableAll();
		for (String name : extensionNames) {
			setEnabled(name, true, false);
		}
		if (saveToConfig)
			saveConfig();
	}

	public List<PlaybackFileCommandExtension> getEnabled() {
		List<PlaybackFileCommandExtension> out = new ArrayList<>();

		for (PlaybackFileCommandExtension element : REGISTRY.values()) {
			if (element.isEnabled()) {
				out.add(element);
			}
		}

		return out;
	}

	public List<PlaybackFileCommandExtension> getAll() {
		return new ArrayList<>(REGISTRY.values());
	}

	@Override
	public void onRecordTick(long index, InputContainer container) {
		enabledExtensions.forEach(extension -> {
			if (extension.isEnabled()) {
				extension.onRecord(index, container);
			}
		});
	}

	@Override
	public void onPlaybackTick(long index, InputContainer container) {
		enabledExtensions.forEach(extension -> {
			if (extension.isEnabled()) {
				extension.onPlayback(index, container);
			}
		});
	}

	public UnsortedFileCommandContainer handleOnSerialiseInline(long currentTick, InputContainer container) {
		SortedFileCommandContainer out = new SortedFileCommandContainer();
		for (PlaybackFileCommandExtension extension : enabledExtensions) {
			SortedFileCommandContainer extensionContainer = extension.onSerialiseInlineComment(currentTick, container);
			if (extensionContainer != null) {
				out.putAll(extensionContainer);
			}
		}
		return out.unsort();
	}

	public UnsortedFileCommandContainer handleOnSerialiseEndline(long currentTick, InputContainer container) {
		SortedFileCommandContainer out = new SortedFileCommandContainer();
		for (PlaybackFileCommandExtension extension : enabledExtensions) {
			SortedFileCommandContainer extensionContainer = extension.onSerialiseEndlineComment(currentTick, container);
			if (extensionContainer != null) {
				out.putAll(extensionContainer);
			}
		}
		return out.unsort();
	}

	public void handleOnDeserialiseInline(long currentTick, InputContainer deserialisedContainer, UnsortedFileCommandContainer unsortedInlineFileCommands) {
		SortedFileCommandContainer fileCommandContainer = unsortedInlineFileCommands.sort();
		for (PlaybackFileCommandExtension extension : enabledExtensions) {
			String[] fileCommandNames = extension.getFileCommandNames();
			extension.onDeserialiseInlineComment(currentTick, deserialisedContainer, fileCommandContainer.split(fileCommandNames));
		}
	}

	public void handleOnDeserialiseEndline(long currentTick, InputContainer deserialisedContainer, UnsortedFileCommandContainer unsortedEndlineFileCommands) {
		SortedFileCommandContainer sortedEndlineFileCommands = unsortedEndlineFileCommands.sort();
		for (PlaybackFileCommandExtension extension : enabledExtensions) {
			String[] fileCommandNames = extension.getFileCommandNames();
			extension.onDeserialiseEndlineComment(currentTick, deserialisedContainer, sortedEndlineFileCommands.split(fileCommandNames));
		}
	}

	@Override
	public void onRecordingClear() {
		onClear();
	}

	public void onClear() {
		REGISTRY.values().forEach(fileCommandExtension -> {
			fileCommandExtension.onClear();
		});
	}

	public void setConfig(Configuration config) {
		this.config = config;
		loadConfig();
	}

	private void loadConfig() {
		if (config == null) {
			return;
		}
		String enabled = config.get(TASmodConfig.EnabledFileCommands);
		setEnabled(Arrays.asList(enabled.split(", ")));
	}

	private void saveConfig() {
		if (config == null) {
			return;
		}
		List<String> nameList = new ArrayList<>();

		enabledExtensions.forEach(element -> {
			nameList.add(element.getExtensionName());
		});
		config.set(TASmodConfig.EnabledFileCommands, String.join(", ", nameList));
		config.save();
	}
}

