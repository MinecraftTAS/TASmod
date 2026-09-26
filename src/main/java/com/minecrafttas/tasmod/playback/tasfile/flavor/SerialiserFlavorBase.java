package com.minecrafttas.tasmod.playback.tasfile.flavor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.dselent.bigarraylist.BigArrayList;
import com.minecrafttas.tasmod.mctcommon.registry.Registerable;
import com.minecrafttas.tasmod.playback.PlaybackControllerClient.CommentContainer;
import com.minecrafttas.tasmod.playback.PlaybackControllerClient.InputContainer;
import com.minecrafttas.tasmod.playback.filecommands.PlaybackFileCommand;
import com.minecrafttas.tasmod.playback.filecommands.PlaybackFileCommand.FileCommandsInCommentList;
import com.minecrafttas.tasmod.playback.filecommands.PlaybackFileCommand.PlaybackFileCommandExtension;
import com.minecrafttas.tasmod.playback.filecommands.PlaybackFileCommand.UnsortedFileCommandContainer;
import com.minecrafttas.tasmod.playback.metadata.PlaybackMetadata;
import com.minecrafttas.tasmod.playback.tasfile.PlaybackSerialiser;
import com.minecrafttas.tasmod.playback.tasfile.exception.PlaybackLoadException;
import com.minecrafttas.tasmod.registries.TASmodAPIRegistry;
import com.minecrafttas.tasmod.virtual.Subtickable;
import com.minecrafttas.tasmod.virtual.VirtualCameraAngle;
import com.minecrafttas.tasmod.virtual.VirtualKey;
import com.minecrafttas.tasmod.virtual.VirtualKeyboard;
import com.minecrafttas.tasmod.virtual.VirtualMouse;

/**
 * <p>The base class of a flavor
 * 
 * <p>All serialisation and deserialisation is broken apart into functions whenever possible,<br>
 * with the intention of allowing small changes to the existing syntax.
 * 
 * <p>Adding functionality to playback should be made via {@link PlaybackFileCommand PlaybackFileCommands}<br>
 * instead of creating a new syntax and adding new information to the header should be made via {@link PlaybackMetadata}
 * 
 * <h4>Sections</h4>
 * <p>The TASfile has 2 main sections, which are called seperately by the {@link PlaybackSerialiser}:
 * 
 * <ol>
 * 	<li>
 * 		<strong>Header</strong><br>
 * 		Contains metadata about this TAS, like credits and start position,<br>
 *		but also a list of enabled extensions and the name of the flavor that was used to encode the file.
 * 	</li>
 * 	<li>
 * 		<strong>Container</strong><br>
 * 		Contains the actual inputs per tick, inputs in a subtick (a.k.a in a frame), comments and other extensions.
 * 	</li>
 * </ol>
 * 
 * Both sections have serialise and deserialise methods:
 * 
 * <ul>
 * 	<li>Serialisation
 * 		<ul>
 * 			<li>{@link #serialiseHeader()}</li>
 * 			<li>{@link #serialise(BigArrayList, long)}</li>
 * 		</ul>
 * 	</li>
 * 	<li>Deserialisation
 * 		<ul>
 * 			<li>{@link #deserialiseHeader(List)}</li>
 * 			<li>{@link #deserialise(BigArrayList, long)}</li>
 * 		</ul>
 * 	</li>
 * </ul>
 * 
 * Clicking on either of these will lead you to a breakdown in their respective javadocs
 * 
 * @author Scribble
 */
public abstract class SerialiserFlavorBase implements Registerable {

	/**
	 * The current line that is being serialised or deserialised. Used for debugging
	 */
	protected long currentLine = 1;

	/**
	 * The current tick that is being serialised or deserialised. Used for debugging
	 */
	protected long currentTick = 0;

	/**
	 * Debug subtick field for error handling
	 */
	protected int currentSubtick = 0;

	/**
	 * Previous serialised or deserialised container, used for allowing relative values in {@link #deserialiseRelativeFloat(String, String, Float) deserialiseRelativeFloat}
	 */
	protected InputContainer previousInputContainer = null;

	/**
	 * If true, process extension data like {@link PlaybackMetadata PlaybackMetadata} and {@link PlaybackFileCommand PlaybackFileCommands}
	 */
	protected boolean processExtensions = true;

	/**
	 * Rotation counter for clamping the yaw
	 */
	protected int yawRotations = 0;

	/*==============================================
		   _____           _       _ _          
		  / ____|         (_)     | (_)         
		 | (___   ___ _ __ _  __ _| |_ ___  ___ 
		  \___ \ / _ \ '__| |/ _` | | / __|/ _ \
		  ____) |  __/ |  | | (_| | | \__ \  __/
		 |_____/ \___|_|  |_|\__,_|_|_|___/\___|	
		 
	  ==============================================*/

	/*
		 _  _  ____   __   ____  ____  ____ 
		/ )( \(  __) / _\ (    \(  __)(  _ \
		) __ ( ) _) /    \ ) D ( ) _)  )   /
		\_)(_/(____)\_/\_/(____/(____)(__\_)
	
	 */

	/**
	 * <h5>Example</h5>
	 * <pre>
	 * ##################### TASfile ####################
	 * </pre>
	 * 
	 * @return The very top of the header
	 */
	protected String headerStart() {
		return createCenteredHeading("TASfile", '#', 50);
	}

	/**
	 * <p>The end of the header, used for detecting when the header stops
	 * <h5>Example</h5>
	 * <pre>
	 * ##################################################
	 * </pre>
	 * 
	 * @return The end of the header
	 */
	protected String headerEnd() {
		return createPaddedString('#', 50);
	}

	/**
	 * <p>Serialises the flavor of this file, the enabled file commands and other metadata
	 * <h5>Tree</h5>
* <pre>
* serialiseHeader
*	-> {@link #headerStart()}
*	-> {@link #serialiseFlavorName(List)}
*	-> {@link #serialiseEnabledFileCommandNames(List)}
*	-> {@link #serialiseMetadata(List)}
*		-> {@link #serialiseMetadataName(List, String)}
*		-> {@link #serialiseMetadataValues(List, LinkedHashMap)}
*	-> {@link #headerEnd()}
* </pre>
	 * <h5>Example</h5>
	 * <pre>
	 * ##################### TASfile ####################					// {@link #headerStart()}
	 * Flavor: beta1 										// {@link #serialiseFlavorName(List)}
	 * FileCommand-Extensions: tasmod_desyncMonitor@v1, tasmod_options@v1, tasmod_label@v1	// {@link #serialiseEnabledFileCommandNames(List)}
	 * 
	 * --------------------- Credits -------------------- 					// {@link #serialiseMetadataName(List, String)}
	 * Title:Insert TAS category here 							// {@link #serialiseMetadataValues(List, LinkedHashMap)}
	 * Author:Insert author here
	 * Playing Time:00:00.0
	 * Rerecords:0
	 * 
	 * ----------------- Start Position -----------------
	 * x:-32.577311363268976
	 * y:56.0
	 * z:-4.457057187505265
	 * pitch:29.25007
	 * yaw:-88.80094
	 * 
	 * ##################################################					// {@link #headerEnd()}
	 * </pre>
	 * 
	 * @return List of lines containing the header
	 */
	public List<String> serialiseHeader() {
		List<String> out = new ArrayList<>();
		out.add(headerStart());
		serialiseFlavorName(out);
		serialiseEnabledFileCommandNames(out);
		serialiseMetadata(out);
		out.add(headerEnd());
		return out;
	}

	/**
	 * <p>How the flavor name is serialised
	 * <p>You normally don't have to edit this,<br>
	 * as the flavor name is taken from the extension name.
	 * <h5>Example</h5>
	 * <pre>
	 * Flavor: beta1
	 * </pre>
	 * 
	 * @param out The serialised lines, passed by reference
	 */
	protected void serialiseFlavorName(List<String> out) {
		out.add("Flavor: " + getExtensionName());
	}

	/**
	 * <p>Adds the file commands that are enabled to the lines
	 * <h5>Example</h5>
	 * <pre>
	 * FileCommand-Extensions: tasmod_label@v1, tasmod_desyncMonitor@v1
	 * </pre>
	 * 
	 * @param out The serialised lines, passed by reference
	 */
	protected void serialiseEnabledFileCommandNames(List<String> out) {
		List<String> stringlist = new ArrayList<>();
		List<PlaybackFileCommandExtension> extensionList = TASmodAPIRegistry.PLAYBACK_FILE_COMMAND.getEnabled();
		if (processExtensions) {
			extensionList.forEach(extension -> stringlist.add(extension.getExtensionName()));
		}
		out.add("FileCommand-Extensions: " + String.join(", ", stringlist));
		out.add("");
	}

	/**
	 * <p>Serialises the metadata to the header of the TASfile
	 * <h5>Example</h5>
	 * <pre>
	 * --------------------- Credits --------------------
	 * Title:Insert TAS category here
	 * Author:Insert author here
	 * Playing Time:00:00.0
	 * Rerecords:0
	 * 
	 * ----------------- Start Position -----------------
	 * x:-32.577311363268976
	 * y:56.0
	 * z:-4.457057187505265
	 * pitch:29.25007
	 * yaw:-88.80094
	 * </pre>
	 * 
	 * @param out
	 */
	protected void serialiseMetadata(List<String> out) {
		if (!processExtensions)
			return;

		List<PlaybackMetadata> metadataList = TASmodAPIRegistry.PLAYBACK_METADATA.handleOnStore();

		for (PlaybackMetadata metadata : metadataList) {
			serialiseMetadataName(out, metadata.getExtensionName());
			serialiseMetadataValues(out, metadata.getData());
			out.add("");
		}
	}

	/**
	 * <p>Serialises only the name of the metadata section
	 * <h5>Example</h5>
	 * <pre>
	 * --------------------- Credits --------------------
	 * </pre>
	 * @param out The lines passed in by reference
	 * @param name The name to process
	 */
	protected void serialiseMetadataName(List<String> out, String name) {
		out.add(createCenteredHeading(name, '-', 50));
	}

	/**
	 * <p>Serialises only the values of the metadata section
	 * <h5>Example</h5>
	 * <pre>
	 * Title:Insert TAS category here
	 * Author:Insert author here
	 * Playing Time:00:00.0
	 * Rerecords:0
	 * </pre>
	 * 
	 * @param out
	 * @param data
	 */
	protected void serialiseMetadataValues(List<String> out, LinkedHashMap<String, String> data) {
		data.forEach((key, value) -> {
			out.add(String.format("%s:%s", key, value));
		});
	}

	/*
		  ___  __   __ _  ____  __   __  __ _  ____  ____ 
		 / __)/  \ (  ( \(_  _)/ _\ (  )(  ( \(  __)(  _ \
		( (__(  O )/    /  )( /    \ )( /    / ) _)  )   /
		 \___)\__/ \_)__) (__)\_/\_/(__)\_)__)(____)(__\_)
	
	 */

	/**
	 * <p>Serialises a list of inputs into a list of strings
* <h5>Tree</h5>
* <pre>
* serialise
* -> {@link #serialiseContainer(BigArrayList, InputContainer)}
*     -> {@link #serialiseKeyboard(VirtualKeyboard)}
*     -> {@link #serialiseKeyboardSubtick(VirtualKeyboard)}
*     -> {@link #serialiseMouse(VirtualMouse)}
*     -> {@link #serialiseMouseSubtick(VirtualMouse)}
*     -> {@link #serialiseCameraAngle(VirtualCameraAngle)}
*     -> {@link #serialiseCameraAngleSubtick(VirtualCameraAngle)}
*     -> {@link #serialiseInlineComments(List, UnsortedFileCommandContainer)}
*     -> {@link #serialiseInlineComment(String)}
*     -> {@link #serialiseFileCommandsInline(List)}
*         -> {@link #serialiseFileCommand(PlaybackFileCommand)}
*     -> {@link #serialiseEndlineComments(List, UnsortedFileCommandContainer)}	// Same as serialiseInlineComments
*     -> {@link #serialiseEndlineComment(String)}
*     -> {@link #serialiseFileCommandsEndline(FileCommandsInCommentList)}	// Unused
*         -> {@link #serialiseFileCommand(PlaybackFileCommand)}
*     -> {@link #mergeInputs(BigArrayList, List, List, List, List)}
*         -> {@link #mergeInput(long, String, String, String, String)}
*         -> {@link #mergeSubtickInput(long, String, String, String, String)}
* </pre>
	 * 
	 * @param inputs The inputs to serialise
	 * @param toTick The tick where to stop, used for partial serialisation by savestates. -1 to serialise all
	 * @return The list of lines
	 */
	public BigArrayList<String> serialise(BigArrayList<InputContainer> inputs, long toTick) {
		BigArrayList<String> out = new BigArrayList<>();

		for (int i = 0; i < inputs.size(); i++) {
			if (toTick == i) {
				break;
			}
			currentTick = i;
			InputContainer container = inputs.get(i).clone();
			serialiseContainer(out, container);
			previousInputContainer = container;
		}
		return out;
	}

	/**
	 * Main serialising method of a single {@link InputContainer}
	 * 
	 * @param out The list of serialised lines, passed in by reference
	 * @param container The {@link InputContainer} to serialise
	 */
	protected void serialiseContainer(BigArrayList<String> out, InputContainer container) {
		currentLine = out.size() - 1;
		List<String> serialisedKeyboard = serialiseKeyboard(container.getKeyboard());
		List<String> serialisedMouse = serialiseMouse(container.getMouse());
		List<String> serialisedCameraAngle = serialiseCameraAngle(container.getCameraAngle());
		pruneListEndEmpty(serialisedCameraAngle);

		UnsortedFileCommandContainer fileCommandsInline = TASmodAPIRegistry.PLAYBACK_FILE_COMMAND.handleOnSerialiseInline(currentTick, container);
		UnsortedFileCommandContainer fileCommandsEndline = TASmodAPIRegistry.PLAYBACK_FILE_COMMAND.handleOnSerialiseEndline(currentTick, container);

		CommentContainer comments = container.getComments();
		List<String> serialisedInlineComments = serialiseInlineComments(comments.getInlineComments(), fileCommandsInline);
		List<String> serialisedEndlineComments = serialiseEndlineComments(comments.getEndlineComments(), fileCommandsEndline);

		mergeInputs(out, serialisedKeyboard, serialisedMouse, serialisedCameraAngle, serialisedInlineComments, serialisedEndlineComments);
	}

	/**
	 * <p>Serialises a {@link VirtualKeyboard}
	 * 
	 * <p>A {@link VirtualKeyboard} is most often comprised of multiple subticks,<br>
	 * which are each serialised in {@link #serialiseKeyboardSubtick(VirtualKeyboard)}
	 * <h5>Example</h5>
	 * <pre>
	 * 	W;w
	 * 	A;a
	 * 	S,D;sd
	 * </pre>
	 * 
	 * @param keyboard The keyboard to serialise
	 * @return A list of serialised keyboardSubticks
	 */
	protected List<String> serialiseKeyboard(VirtualKeyboard keyboard) {
		List<String> out = new ArrayList<>();

		List<VirtualKeyboard> subticks = new ArrayList<>(keyboard.getAll());
//		pruneListEndEmptySubtickable(subticks);

		for (VirtualKeyboard subtick : subticks) {
			out.add(serialiseKeyboardSubtick(subtick));
		}
		return out;
	}

	/**
	 * <p>Serialises a single keyboard subtick
	 * <p>Used for setting the format of a keyboard input in general
	 * <p>The input is split between keycodes and key characters by a semicolon.<br>
	 * While the key code can be written with a number (e.g. the key with the label W is keycode 17),<br>
	 * Only the "name of the keycode" is used for serialisation. After the semicolon a key character is used.<br>
	 * This is what is used for the chat or books, as, when holding e.g. shift, a capitalized character is used instead (SHIFT,W;W).<br>
	 * Keycodes on the other hand have no concept of capitalisation and are used for the actual movement keys (Forward, Backward)
	 * <h5>Example</h5>
	 * <pre>
	 * 	W,S;ws
	 * </pre>
	 * 
	 * @param keyboardSubtick The subtick to serialise
	 * @return The serialised subtick
	 */
	protected String serialiseKeyboardSubtick(VirtualKeyboard keyboardSubtick) {
		return String.format("%s;%s", String.join(",", keyboardSubtick.getCurrentPresses()), charListToString(keyboardSubtick.getCharList()));
	}

	/**
	 * <p>Utility method for converting a char list to a string
	 * <p>Replaces line break characters with \\n
	 * 
	 * @param charList The list to use
	 * @return The created string
	 */
	protected String charListToString(List<Character> charList) {
		String charString = "";
		if (!charList.isEmpty()) {
			charString = charList.stream().map(Object::toString).collect(Collectors.joining());
			charString = StringUtils.replace(charString, "\r", "\\n");
			charString = StringUtils.replace(charString, "\n", "\\n");
		}
		return charString;
	}

	/**
	 * <p>Serialises a {@link VirtualMouse}
	 * <p>A {@link VirtualMouse} is most often comprised of multiple subticks,<br>
	 * which are each serialised in {@link #serialiseMouseSubtick(VirtualMouse)}
	 * <h5>Example</h5>
	 * <pre>
	 * 	LC;0,15,21
	 * 	RC;-15,15,21
	 * 	RC,MC;30,14,20
	 * </pre>
	 * 
	 * @param mouse The mouse to serialise
	 * @return A list of serialised mouse subticks
	 */
	protected List<String> serialiseMouse(VirtualMouse mouse) {
		List<String> out = new ArrayList<>();

		List<VirtualMouse> subticks = new ArrayList<>(mouse.getAll());
//		pruneListEndEmptySubtickable(subticks);

		for (VirtualMouse subtick : subticks) {
			out.add(serialiseMouseSubtick(subtick));
		}
		return out;
	}

	/**
	 * <p>Serialises a single mouse subtick
	 * <p>The mouse subtick is comprised of the following:<br>
	 * mouseKeycodes;scrollWheel,cursorX,cursorY
	 * <h5>Example</h5>
	 * <pre>
	 * 	LC;0,15,21
	 * </pre>
	 * 
	 * @param mouseSubtick The mouse subtick to serialise
	 * @return The serialised mouse subtick
	 */
	protected String serialiseMouseSubtick(VirtualMouse mouseSubtick) {
		return String.format("%s;%s,%s,%s", String.join(",", mouseSubtick.getCurrentPresses()), mouseSubtick.getScrollWheel(), mouseSubtick.getCursorX(), mouseSubtick.getCursorY());
	}

	/**
	 * <p>Serialises a {@link VirtualCameraAngle}
	 * <p>A {@link VirtualCameraAngle} is most often comprised of multiple subticks,<br>
	 * which are each serialised in {@link #serialiseCameraAngleSubtick(VirtualCameraAngle)}
	 * <h5>Example</h5>
	 * <pre>
	 * 	35;26
	 * 	34;25
	 * 	140;-130
	 * </pre>
	 * 
	 * @param cameraAngle Camera angle to serialise
	 * @return The serialised list of camera angles
	 */
	protected List<String> serialiseCameraAngle(VirtualCameraAngle cameraAngle) {

		VirtualCameraAngle previousCamera = null;

		List<String> out = new ArrayList<>();
		for (VirtualCameraAngle subtick : cameraAngle.getAll()) {

			if (!subtick.equals(previousCamera))
				out.add(serialiseCameraAngleSubtick(subtick));

			previousCamera = subtick;
		}
		return out;
	}

	/**
	 * <p>Serialises a single camera angle subtick
	 * <p>The subtick is comprised of:<br>
	 * The camera angle yaw and the camera angle pitch
	 * <h5>Example</h5>
	 * <pre>
	 * 	140;-130
	 * </pre>
	 * 
	 * @param cameraAngleSubtick The camera angle subtick to serialise
	 * @return The serialised camera angle subtick
	 */
	protected String serialiseCameraAngleSubtick(VirtualCameraAngle cameraAngleSubtick) {
		return String.format("%s;%s", clampYaw(cameraAngleSubtick.getYaw()), cameraAngleSubtick.getPitch());
	}

	/**
	 * <p>Serialise comments that take up an entire line
	 * <p>In addition, comments can contain {@link PlaybackFileCommand FileCommands} that are serialised in {@link #serialiseFileCommandsInline(List)}
	 * <h5>Example</h5>
	 * <pre>
	 * // Inline comment
	 * 12|W;w||0;0
	 * </pre>
	 * 
	 * @param inlineComments The list of inline comments to serialise
	 * @param fileCommandsInline The list of file commands to serialise
	 * @return List of comments including file commands
	 */
	protected List<String> serialiseInlineComments(List<String> inlineComments, UnsortedFileCommandContainer fileCommandsInline) {
		List<String> out = new ArrayList<>();

		Queue<FileCommandsInCommentList> fileCommandQueue = null;
		if (fileCommandsInline != null) {
			fileCommandQueue = new LinkedList<>(fileCommandsInline);
		}

		// Serialise comments and merge them with file commands
		if (inlineComments != null) {

			Queue<String> commentQueue = new LinkedList<>(inlineComments);

			// Iterate through comments
			while (!commentQueue.isEmpty()) {
				String comment = commentQueue.poll(); // Due to commentQueue being a LinkedList, comment can be null at this point! 

				String command = null;
				if (fileCommandQueue != null) {
					command = serialiseFileCommandsInline(fileCommandQueue.poll()); // Trying to poll a fileCommand. Command can be null at this point
				}

				// Add an empty line if comment and command is null
				if (comment == null && command == null) {
					out.add("");
					continue;
				}

				out.add(serialiseInlineComment(joinNotEmpty(" ", command, comment)));
			}
		}

		if (fileCommandQueue != null) {

			// If the fileCommandQueue is not empty or longer than the commentQueue,
			// add the rest of the fileCommands to the end
			while (!fileCommandQueue.isEmpty()) {

				String command = serialiseFileCommandsInline(fileCommandQueue.poll());
				if (command != null) {
					out.add(serialiseInlineComment(command));
				} else {
					out.add(""); // Add an empty line if command is null
				}
			}
		}

		return out;
	}

	/**
	 * <p>Comment format for inline comments
	 * <h5>Example</h5>
	 * <pre>
	 * // Inline comment
	 * </pre>
	 * 
	 * @param comment Content in the comment
	 * @return The inline comment
	 */
	protected String serialiseInlineComment(String comment) {
		return String.format("// %s", comment);
	}

	/**
	 * <p>Serialise comments that are written at the end of the line
	 * <h5>Example</h5>
	 * <pre>
	 *	12|W;w||0;0	// Endline comment
	 * </pre>
	 * 
	 * @param endlineComments The list of endline comments to serialise
	 * @param fileCommandsEndline The list of file commands to serialise
	 * @return The serialised comments
	 */
	protected List<String> serialiseEndlineComments(List<String> endlineComments, UnsortedFileCommandContainer fileCommandsEndline) {
		List<String> out = new ArrayList<>();

		Queue<FileCommandsInCommentList> fileCommandQueue = null;
		if (fileCommandsEndline != null) {
			fileCommandQueue = new LinkedList<>(fileCommandsEndline);
		}

		// Serialise comments and merge them with file commands
		if (endlineComments != null) {

			Queue<String> commentQueue = new LinkedList<>(endlineComments);

			// Iterate through comments
			while (!commentQueue.isEmpty()) {
				String comment = commentQueue.poll(); // Due to commentQueue being a LinkedList, comment can be null at this point! 

				String command = null;
				if (fileCommandQueue != null) {
					command = serialiseFileCommandsEndline(fileCommandQueue.poll()); // Trying to poll a fileCommand. Command can be null at this point
				}

				// Add an empty line if comment and command is null
				if (comment == null && command == null) {
					out.add("");
					continue;
				}

				out.add(serialiseEndlineComment(joinNotEmpty(" ", command, comment)));
			}
		}

		if (fileCommandQueue != null) {

			// If the fileCommandQueue is not empty or longer than the commentQueue,
			// add the rest of the fileCommands to the end
			while (!fileCommandQueue.isEmpty()) {

				String command = serialiseFileCommandsEndline(fileCommandQueue.poll());
				if (command != null) {
					out.add(serialiseEndlineComment(command));
				} else {
					out.add(""); // Add an empty line if command is null
				}
			}
		}

		return out;
	}

	/**
	 * <p>Comment format for endline comments
	 * <h5>Example</h5>
	 * <pre>
	 * // Endline comment
	 * </pre>
	 * 
	 * @param comment Content in the comment
	 * @return The endline comment
	 */
	protected String serialiseEndlineComment(String comment) {
		return String.format("// %s", comment);
	}

	/**
	 * <p>Serialises a list of file commands in an inline comment
	 * <p>Uses {@link #serialiseFileCommand(PlaybackFileCommand) serialiseFileCommand} for the actual file command format,<br>
	 * while this method dictates how they are joined together
	 * <p>By default, multiple file commands may be serialised like this
	 * <h5>Example</h5>
	 * <pre>
	 * 	// $fileCommandName1(argument1); $fileCommandName2(argument1, argument2);
	 * </pre>
	 * 
	 * @param fileCommands The file commands to serialise
	 * @return A string of serialised file commands or null if fileCommands is null
	 */
	protected String serialiseFileCommandsInline(FileCommandsInCommentList fileCommands) {
		// File commands is null if there are no file commands in the comment.
		// Return null if that is the case
		if (fileCommands == null) {
			return null;
		}
		List<String> serialisedCommands = new ArrayList<>();
		for (PlaybackFileCommand command : fileCommands) {
			serialisedCommands.add(serialiseFileCommand(command));
		}
		return joinNotEmpty(" ", serialisedCommands);
	}

	/**
	 * <p>Serialises a list of file commands in an endline comment
	 * <p>This is added in case a flavor needs a different format for endline and inline commands,<br>
	 * but by default this is the same as {@link #serialiseFileCommandsInline(List) serialiseFileCommandsInLine}
	 * <h5>Example</h5>
	 * <pre>
	 * 	12|W;w||0;0	// $fileCommandName1(argument1); $fileCommandName2(argument1, argument2);
	 * </pre>
	 * 
	 * @param fileCommands The file commands to serialise
	 * @return A string of serialised file commands or null if fileCommands is null
	 */
	protected String serialiseFileCommandsEndline(FileCommandsInCommentList fileCommands) {
		return serialiseFileCommandsInline(fileCommands);
	}

	/**
	 * <p>Serialises a single file command.
	 * <p>Used for setting the format of file commands
	 * <h5>Example</h5>
	 * <pre>
	 * 	$fileCommandName(argument1, argument2, argument3);
	 * </pre>
	 * 
	 * <p>Has to check if {@link #processExtensions} is false
	 * 
	 * @param fileCommand The {@link PlaybackFileCommand} to serialise
	 * @return The serialised file command, empty if {@link #processExtensions} is false
	 */
	protected String serialiseFileCommand(PlaybackFileCommand fileCommand) {
		if (!processExtensions || fileCommand == null)
			return "";
		return String.format("$%s(%s);", fileCommand.getName(), String.join(", ", fileCommand.getArgs()));
	}

	/**
	 * <p>Merges lists of keyboard, mouse, camera angle, inline and endline comments together into one string
	 * <h5>Example</h5>
	 * <pre>
	 * // Inline comment
	 * // $inlineFileCommand(arg);
	 * 256|W;w|;0,0,0|31.778223;85.11482		// Endline comment
	 *	1|W,S;s|;0,0,0|34.47822;82.56482	// $endlineFileCommand(arg)
	 *	2|;||37.02822;79.86482
	 * </pre>
	 * 
	 * @param out The list of lines that will be written to file, passed in by reference
	 * @param serialisedKeyboard The serialised keyboard from {@link #serialiseKeyboard(VirtualKeyboard)}
	 * @param serialisedMouse The serialised mouse from {@link #serialiseMouse(VirtualMouse)}
	 * @param serialisedCameraAngle The serialised camera angle from {@link #serialiseCameraAngle(VirtualCameraAngle)}
	 * @param serialisedInlineComments The inline comments from {@link #serialiseInlineComments(List, List)}
	 * @param serialisedEndlineComments The endline comments from {@link #serialiseEndlineComments(List, List)}
	 */
	protected void mergeInputs(BigArrayList<String> out, List<String> serialisedKeyboard, List<String> serialisedMouse, List<String> serialisedCameraAngle, List<String> serialisedInlineComments, List<String> serialisedEndlineComments) {

		/*
		 *  Firstly add inline comments, as they appear before the inputs in the container:
		 *  Example:
		 *  // This is an inline comment
		 *  1|||0;0
		 */
		out.addAll(serialisedInlineComments);

		/*
		 * Copy inputs with ticks and subticks into a queue,
		 * so they can be serialised even if the length is different
		 */
		Queue<String> keyboardQueue = new LinkedBlockingQueue<>(serialisedKeyboard);
		Queue<String> mouseQueue = new LinkedBlockingQueue<>(serialisedMouse);
		Queue<String> cameraAngleQueue = new LinkedBlockingQueue<>(serialisedCameraAngle);
		Queue<String> endlineCommentQueue = new LinkedBlockingQueue<>(serialisedEndlineComments);

		String kb = getOrEmpty(keyboardQueue.poll());
		String ms = getOrEmpty(mouseQueue.poll());
		String ca = getOrEmpty(cameraAngleQueue.poll());

		String elc = getOrEmpty(endlineCommentQueue.poll());
		if (!elc.isEmpty()) {
			elc = "\t\t" + elc;
		}

		// Add tick line, not indented
		out.add(mergeInput(currentTick, kb, ms, ca, elc));

		// Add subtick lines, indented
		currentSubtick = 0;
		while (!keyboardQueue.isEmpty() || !mouseQueue.isEmpty() || !cameraAngleQueue.isEmpty()) {
			currentSubtick++;
			kb = getOrEmpty(keyboardQueue.poll());
			ms = getOrEmpty(mouseQueue.poll());
			ca = getOrEmpty(cameraAngleQueue.poll());
			elc = getOrEmpty(endlineCommentQueue.poll());
			if (!elc.isEmpty()) {
				elc = "\t\t" + elc;
			}

			out.add(mergeSubtickInput(currentSubtick, kb, ms, ca, elc));
		}

		/*
		 *  Add the rest of the endline comments.
		 *  Normally there shouldn't be more comments
		 *  than subticks, but maybe some file command extension
		 *  demands it
		 */
		while (!endlineCommentQueue.isEmpty()) {
			elc = getOrEmpty(endlineCommentQueue.poll());
			out.add(String.format("\t|||;\t\t%s", elc));
		}
		currentSubtick = 0;
	}

	/**
	 * <p>How parent inputs are merged
	 * <h5>Example</h5>
	 * <pre>
	 * 256|W;w|;0,0,0|31.778223;85.11482		// Endline comment
	 * </pre>
	 * 
	 * @param currentTick The current tick
	 * @param keyboard The serialised keyboard 
	 * @param mouse The serialised mouse
	 * @param cameraAngle The serialises camera angle
	 * @param endLineComment The end line comment
	 * @return The merged strings
	 */
	protected String mergeInput(long currentTick, String keyboard, String mouse, String cameraAngle, String endLineComment) {
		return String.format("%s|%s|%s|%s%s", currentTick, keyboard, mouse, cameraAngle, endLineComment);
	}

	/**
	 * <p>How subtick inputs are merged
	 * <h5>Example</h5>
	 * <pre>
	 * 256|W;w|;0,0,0|31.778223;85.11482		// Parent
	 *	1|W,S;s|;0,0,0|34.47822;82.56482		// Subtick
	 * </pre>
	 * 
	 * @param currentSubtick The current subtick in this sequence
	 * @param keyboard The serialised keyboard
	 * @param mouse The serialised mouse
	 * @param cameraAngle The serialised camera angle
	 * @param endLineComment The serialised end line comment
	 * @return The merged subticks
	 */
	protected String mergeSubtickInput(int currentSubtick, String keyboard, String mouse, String cameraAngle, String endLineComment) {
		return String.format("\t%s|%s|%s|%s%s", currentSubtick, keyboard, mouse, cameraAngle, endLineComment);
	}

	/**
	 * If a string is null, return an empty string
	 * 
	 * @param string String to check
	 * @return The string or empty if null
	 */
	protected String getOrEmpty(String string) {
		return string == null ? "" : string;
	}

	/**
	 * Joins strings together but ignores empty strings
	 * 
	 * @param delimiter The delimiter of the joined string
	 * @param args The strings to join
	 * @return Joined string
	 */
	protected String joinNotEmpty(String delimiter, Iterable<String> args) {
		String out = "";

		List<String> copy = new ArrayList<>();

		args.forEach((arg) -> {
			if (arg != null && !arg.isEmpty()) {
				copy.add(arg);
			}
		});

		out = String.join(delimiter, copy);

		return out;
	}

	/**
	 * Joins strings together but ignores empty strings
	 * 
	 * @param delimiter The delimiter of the joined string
	 * @param args The strings to join
	 * @return Joined string
	 */
	protected String joinNotEmpty(String delimiter, String... args) {
		return joinNotEmpty(delimiter, Arrays.asList(args));
	}

	/*========================================================
	 	  _____                      _       _ _          
		 |  __ \                    (_)     | (_)         
		 | |  | | ___  ___  ___ _ __ _  __ _| |_ ___  ___ 
		 | |  | |/ _ \/ __|/ _ \ '__| |/ _` | | / __|/ _ \
		 | |__| |  __/\__ \  __/ |  | | (_| | | \__ \  __/
		 |_____/ \___||___/\___|_|  |_|\__,_|_|_|___/\___|
	                                              
	  ========================================================                                             
	 * 
	 */

	/*
		 _  _  ____   __   ____  ____  ____ 
		/ )( \(  __) / _\ (    \(  __)(  _ \
		) __ ( ) _) /    \ ) D ( ) _)  )   /
		\_)(_/(____)\_/\_/(____/(____)(__\_)
	
	 */

	/**
	 * <p>Checks if the name of this flavor is present in the header of the TASfile.
	 * <p>Used to determine the flavor of the file if the flavor is not given
	 * 
	 * @param headerLines The lines from the header to check
	 * @return True, if the flavor name is present in the header
	 */
	public boolean checkFlavorName(List<String> headerLines) {
		for (String line : headerLines) {
			Matcher matcher = extract("^Flavor: " + getExtensionName(), line);

			if (matcher.find()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * <p>Extracts the header from the TASfile
	 * <p>Optimization to seperate the header from the actual inputs.<br>
	 * Only reads a maximum of 1000 and until it finds {@link #headerEnd()}
	 * 
	 * @param lines The total lines to check
	 * @return The list of lines containing the header
	 * @throws PlaybackLoadException If the end of the header is not found after 1000 lines
	 */
	public List<String> extractHeader(BigArrayList<String> lines) {
		List<String> extracted = new ArrayList<>();

		long maxExtract = 1000;

		maxExtract = lines.size() < maxExtract ? lines.size() : maxExtract;

		for (long i = 0; i < maxExtract; i++) {
			String line = lines.get(i);
			extracted.add(line);

			if (line.equals(headerEnd()))
				return extracted;
		}
		throw new PlaybackLoadException("Cannot find the end of the header");
	}

	/**
	 * <p>Deserialise header lines
	 * <pre>
	 *  deserialiseHeader
	 *  笝懌楳笝 {@link #deserialiseMetadata(List)}
	 *  笝披楳笝 {@link #deserialiseEnabledFileCommandNames(List)}
	 * </pre>
	 * 
	 * @param headerLines The header lines to deserialise
	 * @see #serialiseHeader()
	 */
	public void deserialiseHeader(List<String> headerLines) {
		deserialiseMetadata(headerLines);
		deserialiseEnabledFileCommandNames(headerLines);
	}

	/**
	 * <p>Deserialises the TASfile metadata
	 * 
	 * @param headerLines
	 * @see #serialiseMetadata(List)
	 */
	protected void deserialiseMetadata(List<String> headerLines) {
		if (!processExtensions)
			return;

		List<PlaybackMetadata> out = new ArrayList<>();

		String metadataName = null;
		LinkedHashMap<String, String> values = new LinkedHashMap<>();

		for (String headerLine : headerLines) {

			Matcher nameMatcher = extract("^-+ ([^-]+)", headerLine); // If the line starts with ###, an optional space char after and then capture the name 
			Matcher valueMatcher = extract("^([^#].*?):\\s*(.+)", headerLine); // If the line doesn't start with a #, then the key of the metadata, then a : then any or no number of whitespace chars, then the value of the metadata

			if (nameMatcher.find()) {

				if (metadataName != null && !metadataName.equals(nameMatcher.group(1))) { // If metadataName is null, then the first section begins
																							// If metadataName is different than the newMetadataName,
																							// then a new section begins and we first need to store the old.
					out.add(PlaybackMetadata.fromHashMap(metadataName, values));
					values.clear();
				}
				metadataName = nameMatcher.group(1).trim();
				continue;

			} else if (metadataName != null && valueMatcher.find()) {
				values.put(valueMatcher.group(1).trim(), valueMatcher.group(2).trim());
			}
		}

		if (metadataName != null)
			out.add(PlaybackMetadata.fromHashMap(metadataName, values));

		TASmodAPIRegistry.PLAYBACK_METADATA.handleOnLoad(out);
	}

	/**
	 * <p>Deserialises file command extension names and enables them
	 * 
	 * @param headerLines The header lines to search
	 * @see #serialiseEnabledFileCommandNames(List)
	 * @throws PlaybackLoadException If the "FileCommand-Extensions" keyword is not found in the header
	 */
	protected void deserialiseEnabledFileCommandNames(List<String> headerLines) {
		if (!processExtensions) // Stops FileCommandProcessing
			return;

		for (String line : headerLines) {
			Matcher matcher = extract("FileCommand-Extensions: ?(.*)", line);

			if (matcher.find()) {

				if (!processExtensions)
					return;

				String extensionStrings = matcher.group(1);
				String[] extensionNames = extensionStrings.split(", ?");

				TASmodAPIRegistry.PLAYBACK_FILE_COMMAND.setEnabled(extensionNames);
				return;
			}
		}
		throw new PlaybackLoadException("FileCommand-Extensions value was not found in the header");
	}

	/*
		  ___  __   __ _  ____  __   __  __ _  ____  ____ 
		 / __)/  \ (  ( \(_  _)/ _\ (  )(  ( \(  __)(  _ \
		( (__(  O )/    /  )( /    \ )( /    / ) _)  )   /
		 \___)\__/ \_)__) (__)\_/\_/(__)\_)__)(____)(__\_)
	
	 */

	/**
	 * <p>Deserialises the input part of the TASfile
	 * 
* <pre>
* deserialise
* -> {@link #extractContainer(List, BigArrayList, long)}
* -> {@link #deserialiseContainer(BigArrayList, List)}
*     -> {@link #deserialiseMultipleInlineComments(List, UnsortedFileCommandContainer)}
*     -> {@link #deserialiseInlineComment(String, FileCommandsInCommentList)}
*         -> {@link #deserialiseFileCommandsInline(String, FileCommandsInCommentList)}
*     -> {@link #splitTickLines(List, List, List, List, List, List)}
*     -> {@link #deserialiseEndlineComment(String, FileCommandsInCommentList)}
*         -> {@link #deserialiseFileCommandsEndline(String, FileCommandsInCommentList)}
*     -> {@link #deserialiseKeyboard(List)}
*     -> {@link #deserialiseMouse(List)}
*     -> {@link #deserialiseCameraAngle(List)}
* </pre>
	 * 
	 * @param lines The serialised lines of the TASfile
	 * @param startPos The position when the header ends and the inputs start
	 * @return A list of {@link InputContainer InputContainers}
	 */
	public BigArrayList<InputContainer> deserialise(BigArrayList<String> lines, long startPos) {
		BigArrayList<InputContainer> out = new BigArrayList<>();

		if (processExtensions)
			TASmodAPIRegistry.PLAYBACK_FILE_COMMAND.onClear();

		for (long i = startPos; i < lines.size(); i++) {
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

	/**
	 * <p>Extract phases for a tick container.
	 * <p>A container has a certain order.<br>
	 * This enum contains phases that are updated when extracting and verifying a container in {@link SerialiserFlavorBase#extractContainer(List, BigArrayList, long)}
	 * 
	 * @author Scribble
	 */
	protected enum ExtractPhases {
		/**
		 * Inline comment phase.
		 * 
		 * <pre>
		 * ---
		 * // This is a comment
		 * // $fileCommand();
		 * 
		 * ---
		 * </pre>
		 * 
		 * Empty lines also count as comments
		 */
		COMMENTS,
		/**
		 * Tick phase. Start with a number, then a | character
		 * 
		 * <pre>
		 * ---
		 * 57|W,LCONTROL;w|;0,887,626|17.85;-202.74799
		 * ---
		 * </pre>
		 * 
		 * Only one line should be in this phase
		 */
		TICK,
		/**
		 * Subtick phase. Start with a tabulator, then a number, then a | character
		 * 
		 * <pre>
		 * ---
		 * 	1||RC;0,1580,658|17.85;-202.74799		// This is an endline comment
		 * 	2||;0,1580,658|17.85;-202.74799
		 * ---
		 * </pre>
		 * Can have multiple subticks
		 */
		SUBTICK,
		/**
		 * We are outside a tick
		 */
		NONE
	}

	/**
	 * <p>
	 * Extracts all the lines corresponding to one tick+subticks a.k.a one
	 * {@link InputContainer "InputcContainer"} from the incoming lines.<br>
	 * The extracted containers are easier to process than using a huge list.<br>
	 * Furthermore, this method ensures the correct formatting of the lines.
	 * <p>
	 * A container has multiple parts to it, that are split into
	 * {@link ExtractPhases}<br>
	 * The container starts in {@link ExtractPhases#NONE}.
	 * 
	 * <pre>
	 * --- {@link ExtractPhases#COMMENTS Comment phase} --- 
	 * // This is a comment 
	 * // $fileCommand(); 
	 * --- {@link ExtractPhases#TICK Tick phase} ---
	 * 57|W,LCONTROL;w|;0,887,626|17.85;-202.74799 
	 * --- {@link ExtractPhases#SUBTICK Subtick phase} --- 
	 * 	1||RC;0,1580,658|17.85;-202.74799	// This is an endline comment 
	 * 	2||;0,1580,658|17.85;-202.74799
	 * ---------------------
	 * </pre>
	 * 
	 * <h4>Logic</h4>
	 * <ol>
	 * <li>Phase: None
	 * <ol>
	 * <li>If a comment is found, set the phase to comment</li>
	 * <li>If a tick is found, set the phase to tick</li>
	 * <li>If a subtick is found, throw an error. Subticks always come after
	 * ticks</li>
	 * </ol>
	 * </li>
	 * <li>Phase: Comment
	 * <ol>
	 * <li>If a tick is found, set the phase to tick</li>
	 * <li>If a subtick is found, throw an error. Subticks always come after
	 * ticks</li>
	 * </ol>
	 * </li>
	 * <li>Phase: Tick
	 * <ol>
	 * <li>If a subtick is found, set the phase to subticks</li>
	 * <li>If a tick is found, end the extraction</li>
	 * <li>If a comment is found, end the extraction</li>
	 * </ol>
	 * </li>
	 * <li>Phase: Subtick
	 * <ol>
	 * <li>If a tick is found, end the extraction</li>
	 * <li>If a comment is found, end the extraction</li>
	 * </ol>
	 * </li>
	 * </ol>
	 * 
	 * @param extracted The extracted lines, passed in by reference
	 * @param lines     The line list
	 * @param startPos  The start position of this tick
	 * @return The updated index for the next tick
	 * @throws PlaybackLoadException When the order of phases is wrong
	 */
	protected long extractContainer(List<String> extracted, BigArrayList<String> lines, long startPos) {
		ExtractPhases phase = ExtractPhases.NONE;

		String commentRegex = "^//";
		String tickRegex = "^\\d+\\|";
		String subtickRegex = "^\t\\d+\\|";

		long counter = 0L;
		for (long i = startPos; i < lines.size(); i++) {
			String line = lines.get(i);

			switch (phase) {
				case NONE:
					if (contains(subtickRegex, line)) { // Subtick
						throw new PlaybackLoadException(startPos + counter + 1, currentTick, currentSubtick, "Error while trying to parse the file. This should not be a subtick at this position");
					}

					if (contains(commentRegex, line) || line.isEmpty()) { // Comment
						phase = ExtractPhases.COMMENTS;
					} else if (contains(tickRegex, line)) { // Tick
						phase = ExtractPhases.TICK;
					}

					break;
				case COMMENTS:
					if (contains(subtickRegex, line)) { // Subtick
						throw new PlaybackLoadException(startPos + counter + 1, currentTick, currentSubtick, "Error while trying to parse the file. This should not be a subtick at this position");
					}

					if (contains(tickRegex, line)) { // Tick
						phase = ExtractPhases.TICK;
					}

					break;
				case TICK:
					if (contains(subtickRegex, line)) { // Subtick
						phase = ExtractPhases.SUBTICK;
					}

					if (contains(commentRegex, line) || contains(tickRegex, line) || line.isEmpty()) { // Comment
						return startPos + counter - 1;
					}

					break;
				case SUBTICK:
					if (contains(commentRegex, line) || contains(tickRegex, line) || line.isEmpty()) { // Comment
						return startPos + counter - 1;
					}
					break;
			}
			if (phase != ExtractPhases.NONE) {
				extracted.add(line);
			}
			counter++;
		}
		return startPos + counter - 1;
	}

	/**
	 * Main deserialising method of a single {@link InputContainer}
	 * 
	 * In each step, incoming lines are broken down to it's components:
	 * 
	 * <ol>
	 * 	<li>Lines are split in inline comments and ticks</li>
	 * 	<li>Inline comments are processed</li>
	 * 	<li>Tick lines are split into keyboard, mouse, cameraAngle, endlineComments, endlineFileCommands</li>
	 * 	<li>All components from the previous step get deserialised into actual objects</li>
	 * 	<li>Optional if {@link #processExtensions} is true: Update FileCommands</li>
	 * </ol>
	 * 
	 * @param out The list of {@link InputContainers}, passed in by reference
	 * @param containerLines The lines to deserialise
	 */
	protected void deserialiseContainer(BigArrayList<InputContainer> out, List<String> containerLines) {
		// Split lines into comments and ticks
		List<String> inlineComments = new ArrayList<>();
		List<String> tickLines = new ArrayList<>();
		splitContainer(containerLines, inlineComments, tickLines);

		// Process inline comments
		UnsortedFileCommandContainer inlineFileCommands = new UnsortedFileCommandContainer();
		deserialiseMultipleInlineComments(inlineComments, inlineFileCommands);

		// Split ticks into components 
		List<String> keyboardStrings = new ArrayList<>();
		List<String> mouseStrings = new ArrayList<>();
		List<String> cameraAngleStrings = new ArrayList<>();
		List<String> endlineComments = new ArrayList<>();
		UnsortedFileCommandContainer endlineFileCommands = new UnsortedFileCommandContainer();
		splitTickLines(tickLines, keyboardStrings, mouseStrings, cameraAngleStrings, endlineComments, endlineFileCommands);

		/*
		 * The previous step splits everything into multiple lists.
		 * However, the process makes it so every list has the same number of elements.
		 * While this is true for keyboard, mouse and camera,
		 * endlineComments will have empty lines.
		 * 
		 * Ideally we want to remove all empty lines,
		 * however comments do allow empty lines between ticks.
		 * Therefore we remove empty lines, but starting from the back of the list
		 */
		pruneListEndNull(endlineComments);

		// Deserialise each component
		VirtualKeyboard keyboard = deserialiseKeyboard(keyboardStrings);
		VirtualMouse mouse = deserialiseMouse(mouseStrings);
		VirtualCameraAngle cameraAngle = deserialiseCameraAngle(cameraAngleStrings);
		CommentContainer comments = new CommentContainer(inlineComments, endlineComments); // Comments don't need deserialisation

		InputContainer deserialisedContainer = new InputContainer(keyboard, mouse, cameraAngle, comments);

		// Update FileCommands
		if (processExtensions) {
			TASmodAPIRegistry.PLAYBACK_FILE_COMMAND.handleOnDeserialiseInline(currentTick, deserialisedContainer, inlineFileCommands);
			TASmodAPIRegistry.PLAYBACK_FILE_COMMAND.handleOnDeserialiseEndline(currentTick, deserialisedContainer, endlineFileCommands);
		}

		// Set the previous input container, used for relative coordinates
		previousInputContainer = deserialisedContainer;

		out.add(deserialisedContainer);
	}

	/**
	 * @return The regex used for detecting inline comments
	 */
	protected String inlineComment() {
		return "^//";
	}

	/**
	 * Splits container into inline comments and ticks.
	 * 
	 * <pre>
	 * // This is an inline comment
	 * 57|W,LCONTROL;w|;0,887,626|17.85;-202.74799 // This is an endline comment, but that is still part of a tick and processed later
	 * </pre>
	 * 
	 * @param lines The lines to process
	 * @param inlineComments The list to add the inline comments
	 * @param ticks The list to add the ticks to
	 */
	protected void splitContainer(List<String> lines, List<String> inlineComments, List<String> ticks) {
		for (String line : lines) {
			if (contains(inlineComment(), line)) {
				inlineComments.add(line);
			} else {
				ticks.add(line);
			}
		}
	}

	/**
	 * Deserialises a list of inline comments into comments and inline file commands
	 * 
	 * @param inlineComments The comments to deserialise. Will contain comments without filecommands
	 * @param inlineFileCommands The {@link UnsortedFileCommandContainer} passed in by reference
	 */
	protected void deserialiseMultipleInlineComments(List<String> inlineComments, UnsortedFileCommandContainer inlineFileCommands) {
		for (int i = 0; i < inlineComments.size(); i++) {
			FileCommandsInCommentList deserialisedFileCommands = new FileCommandsInCommentList();
			String comment = inlineComments.get(i);

			inlineComments.set(i, deserialiseInlineComment(comment, deserialisedFileCommands));

			if (deserialisedFileCommands.isEmpty()) {
				deserialisedFileCommands = null;
			}
			inlineFileCommands.add(deserialisedFileCommands);
		}
	}

	/**
	 * Processes an inline comment
	 * 
	 * It
	 * <ol>
	 * 	<li>Extracts any inlineFileCommands</li>
	 * 	<li>Removes the leading <code>//</code> from the comment</li>
	 * 	<li>Trims the whitespaces</li>
	 * </ol>
	 * 
	 * @param comment The inline comment to process
	 * @param deserialisedFileCommands The {@link FileCommandsInCommentList}. Passed in by reference
	 * @return The processed comment. Null if comment is empty
	 */
	protected String deserialiseInlineComment(String comment, FileCommandsInCommentList deserialisedFileCommands) {
		comment = deserialiseFileCommandsInline(comment, deserialisedFileCommands);
		comment = extract("^// ?(.+)", comment, 1);
		if (comment != null) {
			comment = comment.trim();
			if (comment.isEmpty()) {
				comment = null;
			}
		}
		return comment;
	}

	/**
	 * @return The regex used for detecting endline comments
	 */
	protected String endlineComment() {
		return "(//.+)";
	}

	/**
	 * Processes an endline comment
	 * 
	 * It
	 * <ol>
	 * 	<li>Extracts any endlineFileCommands</li>
	 * 	<li>Removes the leading <code>//</code> from the comment</li>
	 * 	<li>Trims the whitespaces</li>
	 * </ol>
	 * 
	 * @param comment The endline comment to process
	 * @param deserialisedFileCommands The {@link FileCommandsInCommentList}. Passed in by reference
	 * @return The processed comment. Null if comment is empty
	 */
	protected String deserialiseEndlineComment(String comment, FileCommandsInCommentList deserialisedFileCommands) {
		comment = deserialiseFileCommandsEndline(comment, deserialisedFileCommands);
		comment = extract("^// ?(.+)", comment, 1);
		if (comment != null) {
			comment = comment.trim();
			if (comment.isEmpty()) {
				comment = null;
			}
		}
		return comment;
	}

	/**
	 * Extracts and deserialises one or more inlineFileCommands
	 * 
	 * @param comment The comment to process
	 * @param deserialisedFileCommands The {@link FileCommandsInCommentList}. Passed in by reference
	 * @return The comment minus the fileCommands
	 */
	protected String deserialiseFileCommandsInline(String comment, FileCommandsInCommentList deserialisedFileCommands) {
		Matcher matcher = extract("\\$(.+?)\\((.*?)\\);", comment);

		// Iterate through all file commands and add each to the list
		while (matcher.find()) {
			String name = matcher.group(1);
			String[] args = matcher.group(2).split(", ?");

			if (processExtensions)
				deserialisedFileCommands.add(new PlaybackFileCommand(name, args));

			comment = matcher.replaceFirst("");
			matcher.reset(comment);
		}

		return comment;
	}

	/**
	 * Extracts and deserialises one or more endlineFileCommands
	 * 
	 * @param comment The comment to process
	 * @param deserialisedFileCommands The {@link FileCommandsInCommentList}. Passed in by reference
	 * @return The comment minus the fileCommands
	 */
	protected String deserialiseFileCommandsEndline(String comment, FileCommandsInCommentList deserialisedFileCommands) {
		Matcher matcher = extract("\\$(.+?)\\((.*?)\\);", comment);

		// Iterate through all file commands and add each to the list
		while (matcher.find()) {
			String name = matcher.group(1);
			String[] args = matcher.group(2).split(", ?");

			if (processExtensions)
				deserialisedFileCommands.add(new PlaybackFileCommand(name, args));

			comment = matcher.replaceFirst("");
			matcher.reset(comment);
		}

		return comment;
	}

	/**
	 * @return The regex used in {@link #splitTickLines(List, List, List, List, List, UnsortedFileCommandContainer)}
	 */
	protected String splitTickLineRegex() {
		return "^\\t?\\d+\\|(.*?)\\|(.*?)\\|(\\S*)\\s?";
	}

	/**
	 * Splits tick lines into it's components
	 * 
	 * @param lines The lines to split
	 * @param serialisedKeyboard The empty keyboard list to add to, passed in by reference
	 * @param serialisedMouse The empty mouse list to add to, passed in by reference
	 * @param serialisedCameraAngle The empty camera angle list to add to, passed in by reference
	 * @param commentsAtEnd The empty comments list to add to, passed in by reference
	 * @param endlineFileCommands The empty file commands list to add to, passed in by reference
	 */
	protected void splitTickLines(List<String> lines, List<String> serialisedKeyboard, List<String> serialisedMouse, List<String> serialisedCameraAngle, List<String> commentsAtEnd, UnsortedFileCommandContainer endlineFileCommands) {

		String previousCamera = null;
		if (previousInputContainer != null) {
			VirtualCameraAngle camera = previousInputContainer.getCameraAngle();
			previousCamera = String.format("%s;%s", camera.getYaw(), camera.getPitch());
		}

		for (String line : lines) {
			Matcher tickMatcher = extract(splitTickLineRegex(), line);

			if (tickMatcher.find()) {
				if (!tickMatcher.group(1).isEmpty()) {
					serialisedKeyboard.add(tickMatcher.group(1));
				}
				if (!tickMatcher.group(2).isEmpty()) {
					serialisedMouse.add(tickMatcher.group(2));
				}

				if (!tickMatcher.group(3).isEmpty()) {
					serialisedCameraAngle.add(tickMatcher.group(3));
					previousCamera = tickMatcher.group(3);
				} else {
					if (previousCamera != null)
						serialisedCameraAngle.add(previousCamera);
				}

				FileCommandsInCommentList deserialisedFileCommands = new FileCommandsInCommentList();

				String endlineComment = line.substring(tickMatcher.group(0).length());
				commentsAtEnd.add(deserialiseEndlineComment(endlineComment, deserialisedFileCommands));

				if (deserialisedFileCommands.isEmpty())
					deserialisedFileCommands = null;

				endlineFileCommands.add(deserialisedFileCommands);
			}
		}
	}

	/**
	 * Deserialises a list of keyboard strings in a tick
	 * 
	 * @param keyboardStrings The list to process
	 * @return The {@link VirtualKeyboard} of this tick
	 * @throws PlaybackLoadException When the user made an error in the file
	 */
	protected VirtualKeyboard deserialiseKeyboard(List<String> keyboardStrings) {
		VirtualKeyboard out = new VirtualKeyboard();

		currentSubtick = 0;
		for (String line : keyboardStrings) {
			Matcher matcher = extract("(.*?);(.*)", line);

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

	/**
	 * Deserialises a list of mouse strings in a tick
	 * 
	 * @param mouseStrings The list to process
	 * @return The {@link VirtualMouse} of this tick
	 * @throws PlaybackLoadException When the user made an error in the file
	 */
	protected VirtualMouse deserialiseMouse(List<String> mouseStrings) {
		VirtualMouse out = new VirtualMouse();

		currentSubtick = 0;
		Integer previousCursorX = previousInputContainer == null ? null : previousInputContainer.getMouse().getCursorX();
		Integer previousCursorY = previousInputContainer == null ? null : previousInputContainer.getMouse().getCursorY();

		for (String line : mouseStrings) {
			Matcher matcher = extract("(.*?);(.+)", line);
			if (matcher.find()) {
				String[] buttons = matcher.group(1).split(",");
				String[] functions = matcher.group(2).split(",");

				Set<Integer> keycodes = deserialiseVirtualMouseKey(buttons);
				int scrollwheel;
				Integer cursorX;
				Integer cursorY;

				if (functions.length == 3) {
					scrollwheel = parseInt("scrollwheel", functions[0]);
					cursorX = deserialiseRelativeInt("cursorX", functions[1], previousCursorX);
					cursorY = deserialiseRelativeInt("cursorY", functions[2], previousCursorY);
				} else {
					throw new PlaybackLoadException(currentLine, currentTick, currentSubtick, "Mouse can't be read. Probably a missing comma: %s", line);
				}

				out.updateFromState(keycodes, scrollwheel, cursorX, cursorY);

				previousCursorX = cursorX;
				previousCursorY = cursorY;
			} else {
				throw new PlaybackLoadException(currentLine, currentTick, currentSubtick, "Mouse is missing a semicolon");
			}
			currentSubtick++;
		}
		return out;
	}

	/**
	 * Deserialises a list of cameraAngle strings in a tick
	 * 
	 * @param cameraAngleStrings The list to process
	 * @return The {@link VirtualCameraAngle} of this tick
	 * @throws PlaybackLoadException When the user made an error in the file
	 */
	protected VirtualCameraAngle deserialiseCameraAngle(List<String> cameraAngleStrings) {
		VirtualCameraAngle out = new VirtualCameraAngle(null, null, false);

		currentSubtick = 0;
		Float previousYaw = previousInputContainer == null ? null : previousInputContainer.getCameraAngle().getYaw();
		Float previousPitch = previousInputContainer == null ? null : previousInputContainer.getCameraAngle().getPitch();

		for (String line : cameraAngleStrings) {
			Matcher matcher = extract("(.+?);(.+)", line);

			if (matcher.find()) {
				String cameraYawString = matcher.group(1);
				String cameraPitchString = matcher.group(2);

				Float cameraYaw = null;
				Float cameraPitch = null;

				if (!"null".equals(cameraYawString))
					cameraYaw = deserialiseRelativeFloat("camera yaw", cameraYawString, previousYaw);

				if (!"null".equals(cameraPitchString))
					cameraPitch = deserialiseRelativeFloat("camera pitch", cameraPitchString, previousPitch);

				cameraYaw = unclampYaw(cameraYaw, previousYaw);

				out.updateFromState(cameraPitch, cameraYaw);

				previousYaw = cameraYaw;
				previousPitch = cameraPitch;
			} else {
				throw new PlaybackLoadException(currentLine, currentTick, currentSubtick, "Camera is missing a semicolon");
			}
			currentSubtick++;
		}
		return out;
	}

	/**
	 * Deserialises keypresses in one subtick
	 * 
	 * @param keyString The list of keyStrings in the current subtick
	 * @return A {@link Set} of integer keycodes
	 * @throws PlaybackLoadException When the user made an error in the file
	 */
	protected Set<Integer> deserialiseVirtualKeyboardKey(String[] keyString) {
		Set<Integer> out = new HashSet<>();

		for (int i = 0; i < keyString.length; i++) {
			String key = keyString[i];
			Integer keycode = deserialiseVirtualKey(key, (vkey) -> {
				if (vkey < 0) {
					throw new PlaybackLoadException(currentLine, currentTick, currentSubtick, "Keyboard section contains a mouse key: %s", VirtualKey.get(vkey));
				}
			});

			if (out.contains(keycode))
				throw new PlaybackLoadException(currentLine, currentTick, currentSubtick, "Keyboard has a duplicate key press");

			if (keycode != null)
				out.add(keycode);
		}
		return out;
	}

	/**
	 * Deserialises mousepresses in one subtick
	 * 
	 * @param keyString The list of keyStrings in the current subtick
	 * @return A {@link Set} of integer keycodes
	 * @throws PlaybackLoadException When the user made an error in the file
	 */
	protected Set<Integer> deserialiseVirtualMouseKey(String[] keyString) {
		Set<Integer> out = new HashSet<>();

		for (int i = 0; i < keyString.length; i++) {
			String key = keyString[i];
			Integer keycode = deserialiseVirtualKey(key, (vkey) -> {
				if (vkey >= 0) {
					throw new PlaybackLoadException(currentLine, currentTick, currentSubtick, "Mouse section contains a keyboard key: %s", VirtualKey.get(vkey));
				}
			});

			if (out.contains(keycode))
				throw new PlaybackLoadException(currentLine, currentTick, currentSubtick, "Mouse has a duplicate key press");

			if (keycode != null)
				out.add(keycode);
		}
		return out;
	}

	/**
	 * <p>Deserialises a singular VirtualKey.
	 * This could be either a keyboard or a mouse key
	 * 
	 * <p>All key names supported are listed in {@link VirtualKey}.
	 * 
	 * <p>Also can process literal integer keycode strings like 17.
	 * 
	 * @param key The key string to check
	 * @param keyValidator An external validator to check. Used for mouse and keyboard as they have different keycode ranges
	 * @return The keycode of the string key or null if key is empty
	 * @throws PlaybackLoadException When a keycode can't be parsed
	 */
	protected Integer deserialiseVirtualKey(String key, WrongKeyCheck keyValidator) {

		Integer vkey = null;
		if (key.isEmpty()) {
			return null;
		}
		/* Instead of keynames such as W, A, S, KEY_1, NUMPAD3 you can also write the numerical keycodes
		 * into the tasfile, e.g. 17, 30, 31, 2, 81. This enables TASmod to support every current and future
		 * keycodes, even if no name was given to the key in VirtualKey.*/
		else if (isNumeric(key)) {
			vkey = Integer.parseInt(key);
		} else {
			vkey = VirtualKey.getKeycode(key);
		}

		if (vkey == null) {
			throw new PlaybackLoadException(currentLine, currentTick, currentSubtick, "The keycode %s does not exist", key);
		}

		keyValidator.checkKey(vkey);

		return vkey;
	}

	/**
	 * Lambda for checking keycode ranges
	 * 
	 * @see SerialiserFlavorBase#deserialiseVirtualKey(String, WrongKeyCheck)
	 * @author Scribble
	 */
	@FunctionalInterface
	protected interface WrongKeyCheck {
		public void checkKey(int key) throws PlaybackLoadException;
	}

	/**
	 * Wrapper around {@link Integer#parseInt(String)}
	 * 
	 * @param name The name what is currently being parsed, used in error messages
	 * @param intstring The string to parse
	 * @return The parsed integer
	 * @throws PlaybackLoadException If a {@link NumberFormatException} is thrown during parsing
	 */
	protected int parseInt(String name, String intstring) {
		try {
			return Integer.parseInt(intstring);
		} catch (NumberFormatException e) {
			throw new PlaybackLoadException(currentLine, currentTick, currentSubtick, e, "The %s could not be processed. This should be a number: %s", name, intstring);
		}
	}

	/**
	 * <p>Deserialises values in the form of "~10".
	 * <p>These values will be compared and added to the value from the previous tick or subtick
	 * 
	 * @param name The name what is currently being parsed, used in error messages
	 * @param intstring The string to parse
	 * @param previous The value from the previous tick
	 * @return The parsed and adjusted integer
	 * @throws PlaybackLoadException If a {@link NumberFormatException} is thrown during parsing
	 */
	protected int deserialiseRelativeInt(String name, String intstring, Integer previous) {
		int out = 0;
		if (intstring.startsWith("~")) {
			intstring = intstring.replace("~", "");
			int relative = parseInt(name, intstring);
			if (previous != null) {
				out = previous + relative;
			} else {
				throw new PlaybackLoadException(currentLine, currentTick, currentSubtick, "Can't process relative value ~%s in %s. Previous value for comparing is not available", intstring, name);
			}
		} else {
			out = parseInt(name, intstring);
		}
		return out;
	}

	/**
	 * Wrapper around {@link Float#parseFloat(String)}
	 * 
	 * @param name The name what is currently being parsed, used in error messages
	 * @param floatstring The string to parse
	 * @return The parsed float
	 * @throws PlaybackLoadException If a {@link NumberFormatException} is thrown during parsing
	 */
	protected float parseFloat(String name, String floatstring) {
		try {
			return Float.parseFloat(floatstring);
		} catch (NumberFormatException e) {
			throw new PlaybackLoadException(currentLine, currentTick, currentSubtick, e, "The %s could not be processed. This should be a decimal number: %s", name, floatstring);
		}
	}

	/**
	 * <p>Deserialises values in the form of "~10.0".
	 * <p>These values will be compared and added to the value from the previous tick or subtick
	 * 
	 * @param name The name what is currently being parsed, used in error messages
	 * @param floatstring The string to parse
	 * @param previous The value from the previous tick
	 * @return The parsed and adjusted float
	 * @throws PlaybackLoadException If a {@link NumberFormatException} is thrown during parsing
	 */
	protected Float deserialiseRelativeFloat(String name, String floatstring, Float previous) {
		if (floatstring == null)
			return null;

		float out = 0;
		if (floatstring.startsWith("~")) {
			floatstring = floatstring.replace("~", "");
			float relative = parseFloat(name, floatstring);
			if (previous != null)
				out = previous + relative;
			else
				throw new PlaybackLoadException(currentLine, currentTick, currentSubtick, "Can't process relative value ~%s in %s. Previous value for comparing is not available", floatstring, name);

		} else
			out = parseFloat(name, floatstring);

		return out;
	}

	/**
	 * Utility method to extract something with regex
	 * 
	 * @param regex The regex to use
	 * @param haystack The string to search
	 * @return The {@link Matcher} for this regex
	 */
	protected Matcher extract(String regex, String haystack) {
		Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE);
		Matcher matcher = pattern.matcher(haystack);

		return matcher;
	}

	/**
	 * Utility method to extract something with regex and a group
	 * 
	 * @param regex The regex to use
	 * @param haystack The string to search
	 * @param group The group to extract
	 * @return The extracted string
	 */
	protected String extract(String regex, String haystack, int group) {
		Matcher matcher = extract(regex, haystack);
		if (matcher.find()) {
			return matcher.group(group);
		}
		return null;
	}

	/**
	 * Utility method to check if a string contains a regex patterns
	 * 
	 * @param regex The regex to use
	 * @param haystack The string to search
	 * @return True if the string contains the regex pattern
	 */
	protected boolean contains(String regex, String haystack) {
		return extract(regex, haystack).find();
	}

	/**
	 * Utility method to check if the string is numeric
	 * 
	 * @param string The string to search
	 * @return True if the string is numeric
	 */
	protected boolean isNumeric(String string) {
		return Pattern.matches("-?\\d+", string);
	}

	/**
	 * Utility method to check if the string is a float
	 * 
	 * @param string The string to search
	 * @return True if the string is a float
	 */
	protected boolean isFloat(String string) {
		return Pattern.matches("-?\\d+(?:\\.\\d+)?", string);
	}

	/**
	 * Utility method for creating a centered text
	 * 
	 * @param text The text to center
	 * @param spacingChar The char which should be used for spacing
	 * @param headingWidth The total width
	 * @return The centered text
	 */
	public static String createCenteredHeading(String text, char spacingChar, int headingWidth) {

		if (text == null || text.isEmpty()) {
			return createPaddedString(spacingChar, headingWidth);
		}

		text = " " + text + " ";

		int spacingWidth = headingWidth - text.length();

		String paddingPre = createPaddedString(spacingChar, spacingWidth % 2 == 1 ? spacingWidth / 2 + 1 : spacingWidth / 2);
		String paddingSuf = createPaddedString(spacingChar, spacingWidth / 2);

		return String.format("%s%s%s", paddingPre, text, paddingSuf);
	}

	/**
	 * Utility method for repeating a char a certain amount
	 * 
	 * @param spacingChar The char to use
	 * @param width The width to repeat the char
	 * @return The paddedString
	 */
	private static String createPaddedString(char spacingChar, int width) {
		char[] spacingLine = new char[width];
		for (int i = 0; i < spacingLine.length; i++) {
			spacingLine[i] = spacingChar;
		}
		return new String(spacingLine);
	}

	/**
	 * Empties the list starting from the back if the values are null
	 * 
	 * @param <T>  The element of the list
	 * @param list The list to prune
	 */
	protected <T> void pruneListEndNull(List<T> list) {
		List<T> copy = new ArrayList<>(list);
		for (int i = copy.size() - 1; i >= 0; i--) {
			T element = copy.get(i);
			if (element != null)
				return;
			list.remove(list.size() - 1);
		}
	}

	/**
	 * Empties the list starting from the back if the values are empty
	 * 
	 * @param <T>  The element of the list
	 * @param list The list to prune
	 */
	protected void pruneListEndEmpty(List<String> list) {
		List<String> copy = new ArrayList<>(list);
		for (int i = copy.size() - 1; i >= 0; i--) {
			String element = copy.get(i);
			if (!element.isEmpty())
				return;
			list.remove(list.size() - 1);
		}
	}

	/**
	 * Empties the list starting from the back if the values are empty
	 * 
	 * @param <T>  The element of the list
	 * @param list The list to prune
	 */
	protected <T extends Subtickable<T>> void pruneListEndEmptySubtickable(List<T> list) {
		List<T> copy = new ArrayList<>(list);
		for (int i = copy.size() - 1; i >= 0; i--) {
			T element = copy.get(i);
			if (!element.isEmpty())
				return;
			list.remove(list.size() - 1);
		}
	}

	/**
	 * <p>Clamps the yaw to a value between -180 and 180
	 * 
	 * @param yaw The yaw to clamp
	 * @return The clamped yaw
	 */
	protected Float clampYaw(Float yaw) {
		if (yaw == null)
			return yaw;

		while (yaw >= 180)
			yaw -= 360;
		while (yaw < -180)
			yaw += 360;
		return yaw;
	}

	/**
	 * <p>Unclamping the yaw from a clamped value
	 * <p>Makes it so 170 and a previous value of -170 will return -190,<br>
	 * removing the -180 180 clamp. Uses {@link #yawRotations}
	 * 
	 * @param yaw The yaw to unclamp
	 * @param previous The previous yaw to compare against.
	 * @return The unclamped yaw
	 */
	protected Float unclampYaw(Float yaw, Float previous) {
		if (previous == null || yaw == null)
			return yaw;

		float clampedPrevious = clampYaw(previous);
		if (clampedPrevious >= 0 && (clampedPrevious - yaw) > 180) {
			yawRotations++;
		}
		if (clampedPrevious < 0 && (clampedPrevious - yaw) < -180) {
			yawRotations--;
		}
		return yaw + (360 * yawRotations);
	}

	/**
	 * Set if extensions should be loaded.
	 * 
	 * Setting this to false will stop {@link TASmodAPIRegistry#PLAYBACK_FILE_COMMAND} and {@link TASmodAPIRegistry#PLAYBACK_METADATA} from being processed
	 * 
	 * @param processExtensions
	 */
	public void setProcessExtensions(boolean processExtensions) {
		this.processExtensions = processExtensions;
	}

	/**
	 * @return {@link #currentTick}
	 */
	public long getCurrentTick() {
		return currentTick;
	}

	/**
	 * @return {@link #currentSubtick}
	 */
	public Integer getCurrentSubtick() {
		return currentSubtick;
	}

	@Override
	public String toString() {
		return getExtensionName();
	}

	@Override
	public abstract SerialiserFlavorBase clone();

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof SerialiserFlavorBase) {
			SerialiserFlavorBase flavor = (SerialiserFlavorBase) obj;
			return this.getExtensionName().equals(flavor.getExtensionName());
		}
		return super.equals(obj);
	}
}

