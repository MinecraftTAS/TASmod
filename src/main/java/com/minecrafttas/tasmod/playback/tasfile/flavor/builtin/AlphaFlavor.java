package com.minecrafttas.tasmod.playback.tasfile.flavor.builtin;

import static com.minecrafttas.tasmod.playback.metadata.builtin.CreditsMetadataExtension.CreditFields.Author;
import static com.minecrafttas.tasmod.playback.metadata.builtin.CreditsMetadataExtension.CreditFields.PlayTime;
import static com.minecrafttas.tasmod.playback.metadata.builtin.CreditsMetadataExtension.CreditFields.Rerecords;
import static com.minecrafttas.tasmod.playback.metadata.builtin.CreditsMetadataExtension.CreditFields.Title;
import static com.minecrafttas.tasmod.playback.metadata.builtin.StartpositionMetadataExtension.StartPositionFields.Pitch;
import static com.minecrafttas.tasmod.playback.metadata.builtin.StartpositionMetadataExtension.StartPositionFields.X;
import static com.minecrafttas.tasmod.playback.metadata.builtin.StartpositionMetadataExtension.StartPositionFields.Y;
import static com.minecrafttas.tasmod.playback.metadata.builtin.StartpositionMetadataExtension.StartPositionFields.Yaw;
import static com.minecrafttas.tasmod.playback.metadata.builtin.StartpositionMetadataExtension.StartPositionFields.Z;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;

import com.dselent.bigarraylist.BigArrayList;
import com.minecrafttas.tasmod.playback.PlaybackControllerClient.InputContainer;
import com.minecrafttas.tasmod.playback.filecommands.PlaybackFileCommand;
import com.minecrafttas.tasmod.playback.filecommands.PlaybackFileCommand.FileCommandsInCommentList;
import com.minecrafttas.tasmod.playback.metadata.PlaybackMetadata;
import com.minecrafttas.tasmod.playback.tasfile.exception.PlaybackLoadException;
import com.minecrafttas.tasmod.playback.tasfile.flavor.SerialiserFlavorBase;
import com.minecrafttas.tasmod.registries.TASmodAPIRegistry;
import com.minecrafttas.tasmod.virtual.VirtualCameraAngle;
import com.minecrafttas.tasmod.virtual.VirtualKey;
import com.minecrafttas.tasmod.virtual.VirtualKeyboard;
import com.minecrafttas.tasmod.virtual.VirtualMouse;

public class AlphaFlavor extends SerialiserFlavorBase {

	@Override
	public String getExtensionName() {
		return "alpha";
	}

	@Override
	public SerialiserFlavorBase clone() {
		return new AlphaFlavor();
	}

	@Override
	protected String headerStart() {
		return "################################################# TASFile ###################################################\n";
	}

	@Override
	public List<String> serialiseHeader() {
		List<String> out = new ArrayList<>();

		out.add(headerStart()
				+ "#												Version:1													#\n"
				+ "#							This file was generated using the Minecraft TASMod								#\n"
				+ "#																											#\n"
				+ "#			Any errors while reading this file will be printed out in the console and the chat				#\n"
				+ "#																											#");
		serialiseMetadata(out);
		out.add(headerEnd());
		out.add("#Comments start with \"//\" at the start of the line, comments with # will not be saved");
		return out;
	}

	@Override
	protected String headerEnd() {
		return "#############################################################################################################";
	}

	@Override
	protected void serialiseMetadata(List<String> out) {
		if (!processExtensions)
			return;

		List<PlaybackMetadata> metadataList = TASmodAPIRegistry.PLAYBACK_METADATA.handleOnStore();

		PlaybackMetadata credits = null;
		PlaybackMetadata startPosition = null;

		for (PlaybackMetadata metadata : metadataList) {
			String name = metadata.getExtensionName();
			if (name.equals("Credits"))
				credits = metadata;
			else if (name.equals("Start Position"))
				startPosition = metadata;
		}
		out.add("#------------------------------------------------ Header ---------------------------------------------------#\n"
				+ "#Author:" + credits.getValue(Author) + "\n"
				+ "#																											#\n"
				+ "#Title:" + credits.getValue(Title) + "\n"
				+ "#																											#\n"
				+ "#Playing Time:" + credits.getValue(PlayTime) + "\n"
				+ "#																											#\n"
				+ "#Rerecords:" + credits.getValue(Rerecords) + "\n"
				+ "#																											#\n"
				+ "#----------------------------------------------- Settings --------------------------------------------------#\n"
				+ "#StartPosition:" + processStartPosition(startPosition) + "\n"
				+ "#																											#\n"
				+ "#StartSeed:" + 0); // TODO Add ktrng seed?
	}

	protected String processStartPosition(PlaybackMetadata startPosition) {
		LinkedHashMap<String, String> data = startPosition.getData();
		return String.join(",", data.values());
	}

	@Override
	public boolean checkFlavorName(List<String> headerLines) {
		for (String line : headerLines) {
			Matcher matcher = extract("^#.*Version:1", line);

			if (matcher.find()) {
				return true;
			}
		}
		return false;
	}

	@Override
	protected List<String> serialiseKeyboard(VirtualKeyboard keyboard) {
		/*
		 * Old code from when I did not know String.join exists,
		 * kept relatively unaltered because I want to.  
		 */
		List<String> out = new ArrayList<>();

		List<String> stringy = keyboard.getCurrentPresses();
		String keyString = "";
		if (!stringy.isEmpty()) {
			String seperator = ",";
			for (int i = 0; i < stringy.size(); i++) {
				if (i == stringy.size() - 1) {
					seperator = "";
				}
				if (stringy.get(i).equals("ZERO"))
					continue;
				keyString = keyString.concat(stringy.get(i) + seperator);
			}
		}
		List<Character> charList = keyboard.getCharList();
		String charString = "";
		if (!charList.isEmpty()) {
			for (int i = 0; i < charList.size(); i++) {
				charString = charString.concat(Character.toString(charList.get(i)));
			}
			charString = charString.replace("\r", "\\n");
			charString = charString.replace("\n", "\\n");
		}

		out.add("Keyboard:" + keyString + ";" + charString); // Keyboard didn't support subticks, only the current key is processed

		return out;
	}

	@Override
	protected List<String> serialiseMouse(VirtualMouse mouse) {
		/*
		 * Old code from when I did not know String.join exists,
		 * kept relatively unaltered because I want to.  
		 */
		List<String> out = new ArrayList<>();
		List<String> stringy = mouse.getCurrentPresses();
		String keyString = "";
		if (!stringy.isEmpty()) {
			String seperator = ",";
			for (int i = 0; i < stringy.size(); i++) {
				if (i == stringy.size() - 1) {
					seperator = "";
				}
				if (stringy.get(i).equals("MOUSEMOVED"))
					continue;
				keyString = keyString.concat(stringy.get(i) + seperator);
			}
		}

		List<VirtualMouse> path = new ArrayList<>(mouse.getAll()); // I previously called subticks "paths" as it was mainly used for the mouse...
//		pruneListEndEmptySubtickable(path);

		/*
		 * The mouse supported subticks,
		 * but it was handled differently in alpha...
		 * The subticks where added in square brackets, seperated by a "->"
		 * Not the best solution in hindsight,
		 * but that was apparently the first thing that came to my mind back then...
		 */
		String pathString = "";
		if (!path.isEmpty()) {
			String seperator = "->";
			for (int i = 0; i < path.size(); i++) {
				if (i == path.size() - 1) {
					seperator = "";
				}

				VirtualMouse singlePath = path.get(i);

				pathString = pathString.concat("[" + serialisePath(singlePath) + "]" + seperator);
			}
		}
		out.add("Mouse:" + keyString + ";" + pathString);
		return out;
	}

	protected String serialisePath(VirtualMouse path) {
		String keyString = "";
		List<String> strings = new ArrayList<String>();

		path.getPressedKeys().forEach((virtualkeys) -> {
			strings.add(VirtualKey.getName(virtualkeys));
		});
		if (!strings.isEmpty()) {
			String seperator = ",";
			for (int i = 0; i < strings.size(); i++) {
				if (i == strings.size() - 1) {
					seperator = "";
				}
				keyString = keyString.concat(strings.get(i) + seperator);
			}
		}
		if (keyString.isEmpty()) {
			return "MOUSEMOVED," + path.getScrollWheel() + "," + path.getCursorX() + "," + path.getCursorY();
		} else {
			return keyString + "," + path.getScrollWheel() + "," + path.getCursorX() + "," + path.getCursorY();
		}
	}

	@Override
	protected List<String> serialiseCameraAngle(VirtualCameraAngle subticks) {
		List<String> out = new ArrayList<>();

		/*
		 * The camera was called "subticks" in previous iterations of this code.
		 * To honor this fact, it is also called subticks here, even though
		 * actual subticks were not supported
		 */
		float pitch = subticks.getPitch() == null ? 0f : subticks.getPitch();
		float yaw = subticks.getYaw() == null ? 0f : subticks.getYaw();
		out.add("Camera:" + pitch + ";" + yaw);
		return out;
	}

	@Override
	protected String serialiseFileCommandsInline(FileCommandsInCommentList fileCommands) {
		if (fileCommands == null) {
			return null;
		}
		List<String> serialisedCommands = new ArrayList<>();
		for (PlaybackFileCommand command : fileCommands) {
			if ("hud".equals(command.getName())) {
				serialisedCommands.add(String.format("$hud %s", command.getArgs()[0].equals("true") ? "on" : "off"));
			}
			if ("label".equals(command.getName())) {
				serialisedCommands.add(String.format("$info %s", command.getArgs().length == 0 ? "off" : String.join(" ", command.getArgs())));
			}
		}
		return String.join(" ", serialisedCommands);
	}

	@Override
	protected String serialiseFileCommandsEndline(FileCommandsInCommentList fileCommands) {
		if (fileCommands == null) {
			return null;
		}
		List<String> serialisedCommands = new ArrayList<>();
		for (PlaybackFileCommand command : fileCommands) {
			if ("desyncMonitor".equals(command.getName())) {
				serialisedCommands.add(String.format("Monitoring:%s 0", String.join(" ", command.getArgs())));
			}
		}
		return String.join(" ", serialisedCommands);
	}

	@Override
	protected String serialiseInlineComment(String comment) {
		return String.format("//%s", comment);
	}

	@Override
	protected String serialiseEndlineComment(String comment) {
		return String.format("//%s", comment);
	}

	@Override
	protected String mergeInput(long currentTick, String keyboard, String mouse, String cameraAngle, String endLineComment) {
		return String.format("%s|%s|%s|%s~&\t\t%s", currentTick, keyboard, mouse, cameraAngle, endLineComment);
	}

	@Override
	protected void deserialiseMetadata(List<String> headerLines) {
		String author = "Insert author here";

		String title = "Insert TAS category here";

		String playtime = "00:00.0";

		String rerecords = "0";
		// No default start location
		String startLocation = "";

		for (String line : headerLines) {
			if (line.startsWith("#Author:")) {
				author = line.split(":")[1];
				// Read title tag
			} else if (line.startsWith("#Title:")) {
				title = line.split(":")[1];
				// Read playtime
			} else if (line.startsWith("#Playing Time:")) {
				playtime = line.split("Playing Time:")[1];
				// Read rerecords
			} else if (line.startsWith("#Rerecords:")) {
				rerecords = line.split(":")[1];
				// Read start position
			} else if (line.startsWith("#StartPosition:")) {
				startLocation = line.replace("#StartPosition:", "");
			}
//			// Read start seed
//			else if (line.startsWith("#StartSeed:")) {
//				startSeed = Long.parseLong(line.replace("#StartSeed:", ""));
//			}
		}

		PlaybackMetadata creditsMetada = new PlaybackMetadata("Credits");
		creditsMetada.setValue(Author, author);
		creditsMetada.setValue(Title, title);
		creditsMetada.setValue(PlayTime, playtime);
		creditsMetada.setValue(Rerecords, rerecords);

		PlaybackMetadata startPositionMetadata = new PlaybackMetadata("Start Position");
		String[] split = startLocation.split(",");
		startPositionMetadata.setValue(X, split[0]);
		startPositionMetadata.setValue(Y, split[1]);
		startPositionMetadata.setValue(Z, split[2]);
		startPositionMetadata.setValue(Pitch, split[3]);
		startPositionMetadata.setValue(Yaw, split[4]);

		List<PlaybackMetadata> metadataList = new ArrayList<>();
		metadataList.add(creditsMetada);
		metadataList.add(startPositionMetadata);

		TASmodAPIRegistry.PLAYBACK_METADATA.handleOnLoad(metadataList);
	}

	@Override
	protected String splitTickLineRegex() {
		return "^\\d+\\|(.*?)\\|(.*?)\\|(\\S*)~&";
	}

	@Override
	protected String deserialiseFileCommandsInline(String comment, FileCommandsInCommentList deserialisedFileCommands) {
		Matcher matcher = extract("\\$(.+?) (.+?)", comment);

		// Iterate through all file commands and add each to the list
		while (matcher.find()) {
			String name = matcher.group(1);
			String[] args = matcher.group(2).split(" ");

			if ("hud".equals(name)) {
				args[0] = "on".equals(args[0]) ? "true" : "false";
			} else if ("info".equals(name)) {
				name = "label";
				args[0] = "off".equals(args[0]) ? "" : args[0];
			}

			if (processExtensions)
				deserialisedFileCommands.add(new PlaybackFileCommand(name, args));

			comment = matcher.replaceFirst("");
			matcher.reset(comment);
		}

		return comment;
	}

	@Override
	protected String deserialiseFileCommandsEndline(String comment, FileCommandsInCommentList deserialisedFileCommands) {
		Matcher matcher = extract("Monitoring:(.+)", comment);

		// Iterate through all file commands and add each to the list
		while (matcher.find()) {
			String name = "desyncMonitor";
			String[] args = matcher.group(1).split(" ");

			String[] shortenedArgs = new String[6];
			for (int i = 0; i < 6; i++) {
				shortenedArgs[i] = args[i];
			}

			if (processExtensions)
				deserialisedFileCommands.add(new PlaybackFileCommand(name, shortenedArgs));

			comment = matcher.replaceFirst("");
			matcher.reset(comment);
		}

		return comment;
	}

	@Override
	protected VirtualKeyboard deserialiseKeyboard(List<String> keyboardStrings) {
		VirtualKeyboard out = new VirtualKeyboard();

		currentSubtick = 0;
		for (String line : keyboardStrings) {
			Matcher matcher = extract("Keyboard:(.*?);(.*)", line);
			if (matcher.find()) {
				String[] keys = matcher.group(1).split(",");
				char[] chars = matcher.group(2).toCharArray();

				Set<Integer> keycodes = deserialiseVirtualKeyboardKey(keys);
				out.updateFromState(keycodes, chars);
			} else {
				throw new PlaybackLoadException(currentLine, currentTick, currentSubtick, "Keyboard could not be read. Probably a missing semicolon: %s", line);
			}
			currentSubtick++;
		}
		return out;
	}

	@Override
	public BigArrayList<InputContainer> deserialise(BigArrayList<String> lines, long startPos) {
		BigArrayList<InputContainer> out = new BigArrayList<>();
		for (long i = startPos; i < lines.size(); i++) {

			if (lines.get(i).startsWith("#")) {
				continue;
			}
			List<String> container = new ArrayList<>();
			// Extract the tick and set the index
			i = extractContainer(container, lines, i);
			currentLine = i;
			// Deserialise container
			deserialiseContainer(out, container);
			currentTick++;
		}
		previousInputContainer = null;
		return out;
	}

	@Override
	protected VirtualMouse deserialiseMouse(List<String> mouseStrings) {
		String section = mouseStrings.get(0);
		VirtualMouse mouse = new VirtualMouse();

		// Remove the prefix
		section = section.replace("Mouse:", "");

		//Split into buttons and paths...
		String buttons = section.split(";")[0];
		String path = section.split(";")[1];

		//Check whether the button is empty
		if (!buttons.isEmpty()) {

			//Splitting multiple buttons
			String[] splitButtons = buttons.split(",");
			Set<Integer> keycodes = deserialiseVirtualMouseKey(splitButtons);
			mouse.updateFromState(keycodes, 0, 0, 0);
		}
		readPath(path, mouse);

		return mouse;
	}

	protected void readPath(String section, VirtualMouse mouse) {

		section = section.replace("[", "").replace("]", "");
		String[] pathNodes = section.split("->");

		for (String pathNode : pathNodes) {
			String[] split = pathNode.split(",");

			int length = split.length;
			int scrollWheel = 0;
			int cursorX = 0;
			int cursorY = 0;
			try {
				scrollWheel = Integer.parseInt(split[length - 3]);
				cursorX = Integer.parseInt(split[length - 2]);
				cursorY = Integer.parseInt(split[length - 1]);
			} catch (NumberFormatException e) {
				throw new PlaybackLoadException("'" + pathNode + "' couldn't be read in line " + currentLine + ": Something is not a number");
			} catch (ArrayIndexOutOfBoundsException e) {
				throw new PlaybackLoadException("'" + pathNode + "' couldn't be read in line " + currentLine + ": Something is missing or is too much");
			}
			Set<Integer> keyList = new HashSet<>();
			for (int i = 0; i < length - 3; i++) {
				String key = split[i];
				Integer keyCode = VirtualKey.getKeycode(key);
				if (keyCode == null) {
					throw new PlaybackLoadException(currentLine, currentTick, currentSubtick, "Could not find keycode");
				}
				keyList.add(keyCode);
			}
			mouse.updateFromState(keyList, scrollWheel, cursorX, cursorY);
		}
	}

	@Override
	protected VirtualCameraAngle deserialiseCameraAngle(List<String> cameraAngleStrings) {
		VirtualCameraAngle out = new VirtualCameraAngle();

		currentSubtick = 0;
		Float previousPitch = previousInputContainer == null ? null : previousInputContainer.getCameraAngle().getPitch();
		Float previousYaw = previousInputContainer == null ? null : previousInputContainer.getCameraAngle().getYaw();

		for (String line : cameraAngleStrings) {
			Matcher matcher = extract("Camera:(.+?);(.+)", line);

			if (matcher.find()) {
				String cameraYawString = matcher.group(2);
				String cameraPitchString = matcher.group(1);

				Float cameraYaw = null;
				Float cameraPitch = null;

				if (!"null".equals(cameraYawString))
					cameraYaw = deserialiseRelativeFloat("camera yaw", cameraYawString, previousYaw);

				if (!"null".equals(cameraPitchString))
					cameraPitch = deserialiseRelativeFloat("camera pitch", cameraPitchString, previousPitch);

				out.updateFromState(cameraPitch, cameraYaw);
			} else {
				throw new PlaybackLoadException(currentLine, currentTick, currentSubtick, "Camera is missing a semicolon");
			}
			currentSubtick++;
		}
		return out;
	}

	@Override
	protected void deserialiseEnabledFileCommandNames(List<String> headerLines) {
		/*
		 * Alpha has these file commands hardcoded
		 */
		TASmodAPIRegistry.PLAYBACK_FILE_COMMAND.setEnabled("tasmod_label@v1", "tasmod_desyncMonitor@v1", "tasmod_options@v1");
	}
}

