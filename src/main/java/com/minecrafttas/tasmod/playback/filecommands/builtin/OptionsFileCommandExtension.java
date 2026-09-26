package com.minecrafttas.tasmod.playback.filecommands.builtin;

import java.nio.file.Path;

import com.minecrafttas.tasmod.TASmod;
import com.minecrafttas.tasmod.playback.PlaybackControllerClient.InputContainer;
import com.minecrafttas.tasmod.playback.filecommands.PlaybackFileCommand;
import com.minecrafttas.tasmod.playback.filecommands.PlaybackFileCommand.FileCommandsInTickList;
import com.minecrafttas.tasmod.playback.filecommands.PlaybackFileCommand.PlaybackFileCommandExtension;
import com.minecrafttas.tasmod.playback.filecommands.PlaybackFileCommand.SortedFileCommandContainer;
import com.minecrafttas.tasmod.util.LoggerMarkers;

public class OptionsFileCommandExtension extends PlaybackFileCommandExtension {

	private boolean shouldRenderHud = true;

	public OptionsFileCommandExtension() {
		this("hud");
	}

	public OptionsFileCommandExtension(String tempDirName) {
		super(tempDirName);
		enabled = true;
	}

	public OptionsFileCommandExtension(Path tempDir) {
		super(tempDir);
		enabled = true;
	}

	@Override
	public String getExtensionName() {
		return "tasmod_options@v1";
	}

	@Override
	public String[] getFileCommandNames() {
		return new String[] { "hud" };
	}

	@Override
	public void onPlayback(long tick, InputContainer inputContainer) {
		if (inlineFileCommandStorage.size() <= tick) {
			return;
		}
		SortedFileCommandContainer containerInTick = inlineFileCommandStorage.get(tick);
		if (containerInTick == null) {
			return;
		}

		FileCommandsInTickList line = containerInTick.get("hud");
		if (line == null) {
			return;
		}

		for (PlaybackFileCommand command : line) {
			if (command == null)
				continue;
			String[] args = command.getArgs();
			if (args.length == 1) {
				/*
				 * Ok this may seem dumb, but Boolean.parseBoolean returns false,
				 * even if something other then true or false was passed...
				 * If someone finds something less idiotic please tell me...
				 */
				switch (args[0]) {
					case "true":
						shouldRenderHud = true;
						break;

					case "false":
						shouldRenderHud = false;
						break;

					default:
						TASmod.LOGGER.warn(LoggerMarkers.Playback, "FileCommand hud has the wrong argument in tick {}: {} (Must be true or false)", tick, args[0]);
						break;
				}
			} else {
				TASmod.LOGGER.warn(LoggerMarkers.Playback, "FileCommand hud has the wrong number of arguments in tick {}: {}", tick, args.length);
			}
		}
	}

	@Override
	public void onClear() {
		super.onClear();
		shouldRenderHud = true;
	}

	public boolean shouldRenderHud() {
		return shouldRenderHud;
	}
}

