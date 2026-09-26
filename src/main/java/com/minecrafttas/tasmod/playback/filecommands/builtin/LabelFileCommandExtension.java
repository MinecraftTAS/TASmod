package com.minecrafttas.tasmod.playback.filecommands.builtin;

import java.nio.file.Path;

import com.minecrafttas.tasmod.playback.PlaybackControllerClient.InputContainer;
import com.minecrafttas.tasmod.playback.filecommands.PlaybackFileCommand;
import com.minecrafttas.tasmod.playback.filecommands.PlaybackFileCommand.FileCommandsInTickList;
import com.minecrafttas.tasmod.playback.filecommands.PlaybackFileCommand.PlaybackFileCommandExtension;
import com.minecrafttas.tasmod.playback.filecommands.PlaybackFileCommand.SortedFileCommandContainer;

public class LabelFileCommandExtension extends PlaybackFileCommandExtension {

	private String labelText = "";

	public LabelFileCommandExtension() {
		this("label");
	}

	public LabelFileCommandExtension(String tempDirName) {
		super(tempDirName);
		enabled = true;
	}

	public LabelFileCommandExtension(Path tempDir) {
		super(tempDir);
		enabled = true;
	}

	@Override
	public String getExtensionName() {
		return "tasmod_label@v1";
	}

	@Override
	public String[] getFileCommandNames() {
		return new String[] { "label" };
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

		FileCommandsInTickList line = containerInTick.get("label");
		if (line == null) {
			return;
		}

		for (PlaybackFileCommand command : line) {
			if (command == null)
				continue;
			labelText = String.join(", ", command.getArgs());
		}
	}

	@Override
	public void onClear() {
		super.onClear();
		labelText = "";
	}

	public String getLabelText() {
		return labelText;
	}
}

