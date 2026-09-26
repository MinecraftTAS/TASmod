package com.minecrafttas.tasmod.playback.filecommands;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import com.dselent.bigarraylist.BigArrayList;
import com.minecrafttas.tasmod.mctcommon.file.AbstractDataFile;
import com.minecrafttas.tasmod.mctcommon.registry.Registerable;
import com.minecrafttas.tasmod.TASmodClient;
import com.minecrafttas.tasmod.commands.CommandFileCommand;
import com.minecrafttas.tasmod.playback.PlaybackControllerClient;
import com.minecrafttas.tasmod.playback.PlaybackControllerClient.InputContainer;
import com.minecrafttas.tasmod.playback.tasfile.PlaybackSerialiser;
import com.minecrafttas.tasmod.playback.tasfile.flavor.SerialiserFlavorBase;

public class PlaybackFileCommand {

	/**
	 * The name of the fileCommand
	 */
	private String name;

	/**
	 * The arguments of the fileCommand
	 */
	private String[] args;

	/**
	 * Creates a new FileCommand with no arguments
	 * @param name The {@link #name}
	 */
	public PlaybackFileCommand(String name) {
		this(name, (String[]) null);
	}

	/**
	 * Creates a new FileCommand
	 * @param name The {@link #name}
	 * @param args The {@link #args}r
	 */
	public PlaybackFileCommand(String name, String... args) {
		if (args == null) {
			args = new String[] {};
		}
		this.name = name;
		this.args = args;
	}

	/**
	 * @return {@link #name}
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return {@link #args}
	 */
	public String[] getArgs() {
		return args;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof PlaybackFileCommand) {
			PlaybackFileCommand other = (PlaybackFileCommand) obj;
			return this.name.equals(other.name) && Arrays.equals(this.args, other.args);
		}
		return super.equals(obj);
	}

	@Override
	public String toString() {
		return String.format("$%s(%s);", name, String.join(", ", args));
	}

	/**
	 * <p>Abstract class for a FileCommandExtension.
	 * <p>Allows for creating custom FileCommands that can be stored within the TASfile<br>
	 * to trigger custom behaviour when a playback is reaching that point
	
	 * @author Scribble
	 */
	public static abstract class PlaybackFileCommandExtension implements Registerable {
		/**
		 * The temporary directory of the {@link #fileCommandStorage}
		 */
		protected final Path tempDir;

		/**
		 * The list where all filecommands for this extension are stored
		 */
		protected BigArrayList<SortedFileCommandContainer> inlineFileCommandStorage;

		/**
		 * The list where all filecommands for this extension are stored
		 */
		protected BigArrayList<SortedFileCommandContainer> endlineFileCommandStorage;

		/**
		 * Creates a new extension with the default {@link #tempDir}
		 */
		public PlaybackFileCommandExtension() {
			this((Path) null);
		}

		/**
		 * <p>Creates a FileCommandExtension and creates a temp folder with<br>
		 * the specified name for the {@link BigArrayList} files in the correct location
		 * 
		 * @param tempFolderName The name of the temp folder
		 */
		public PlaybackFileCommandExtension(String tempFolderName) {
			this(TASmodClient.getTasFileDirectory().resolve("temp").resolve(tempFolderName));
		}

		/**
		 * <p>Creates a FileCommandExtension and creates a temp folder with<br>
		 * at the specified path for the {@link BigArrayList} files
		 * 
		 * @param tempFolderName The name of the temp folder
		 */
		public PlaybackFileCommandExtension(Path tempDirectory) {
			if (tempDirectory == null) {
				tempDir = null;
				inlineFileCommandStorage = new BigArrayList<>();
				endlineFileCommandStorage = new BigArrayList<>();
				return;
			}

			tempDir = tempDirectory;
			try {
				AbstractDataFile.createDirectory(tempDirectory);
			} catch (IOException e) {
				e.printStackTrace();
			}
			inlineFileCommandStorage = new BigArrayList<>(tempDir.toString());
			endlineFileCommandStorage = new BigArrayList<>(tempDir.toString());
		}

		/**
		 * Whether this extension is enabled.<br>
		 * Can be changed e.g. via the {@link CommandFileCommand} command
		 */
		protected boolean enabled = false;

		/**
		 * <p>The names of all file commands that should be handled by this extension
		 * <p>Imagine having the following file commands in the playback file:
		 * <pre>
		 * // $desyncMonitor(13, 0, 1, 1, 1, 1); $hud(true);
		 * </pre>
		 * And you want to support the hud file command with an extension,<br>
		 * then you must return a string array with the name:
		 * <pre>
		 * public String[] getFileCommandNames() {
		 * 	return new String[]{"hud"};
		 * }
		 * </pre>
		 * Now, methods like {@link #onDeserialiseEndlineComment(long, InputContainer, SortedFileCommandContainer) onDeserialiseEndlineComment}
		 * will only have a {@link SortedFileCommandContainer}<br>
		 * with the "hud" {@link FileCommandsInTickList} as a parameter. "desyncMonitor" will be ignored.
		 * <p>
		 * To also include "desyncMonitor" in the {@link SortedFileCommandContainer}, simply add that name to the array:
		 * <pre>
		 * public String[] getFileCommandNames() {
		 * 	return new String[]{"hud", "desyncMonitor"};
		 * }
		 * </pre>
		 * 
		 * @return A string array of file command names
		 */
		public abstract String[] getFileCommandNames();

		/**
		 * Fired when using the {@link CommandFileCommand} and setting this extension to enabled
		 */
		public void onEnable() {
		};

		/**
		 * Fired when using the {@link CommandFileCommand} and setting this extension to disabled
		 */
		public void onDisable() {
		};

		/**
		 * Fired when {@link PlaybackControllerClient#clear()} is called<br>
		 * Make sure to call <code>super.onClear()</code> as it clears the {@link BigArrayLists} in this extension!  
		 */
		public void onClear() {
			inlineFileCommandStorage.clear();
			endlineFileCommandStorage.clear();
		};

		/**
		 * Fired when the {@link PlaybackControllerClient} is recording inputs<br>
		 * Usually used to generate new FileCommands during recording.
		 * @param tick The current tick in the recording
		 * @param inputContainer The current inputs in the recording
		 */
		public void onRecord(long tick, InputContainer inputContainer) {
		};

		/**
		 * Fired when the {@link PlaybackControllerClient} is playing back inputs.<br>
		 * Usually used to generate new FileCommands during playback.
		 * @param tick The current tick in the playback
		 * @param inputContainer The current inputs in the playback
		 */
		public void onPlayback(long tick, InputContainer inputContainer) {
		};

		/**
		 * Fired when the {@link PlaybackSerialiser} writes the inputs to a file.<br>
		 * This is used to store your inlineFileCommands into an comment.
		 * 
		 * @param tick The current tick that is serialised
		 * @param inputContainer The current inputs that are being serialised
		 * @return A {@link SortedFileCommandContainer} with your filecommands for one container that you want serialised
		 */
		public SortedFileCommandContainer onSerialiseInlineComment(long tick, InputContainer inputContainer) {
			SortedFileCommandContainer out = new SortedFileCommandContainer();
			if (tick >= inlineFileCommandStorage.size())
				return out;

			SortedFileCommandContainer currentTick = inlineFileCommandStorage.get(tick);
			if (currentTick == null)
				return out;

			for (String name : getFileCommandNames()) {
				if (currentTick.get(name) != null)
					out.putAll(currentTick.split(name));
			}
			return out;
		}

		/**
		 * Fired when the {@link PlaybackSerialiser} writes the inputs to a file.<br>
		 * This is used to store your endlineFileCommands into an comment.
		 * 
		 * @param tick The current tick that is serialised
		 * @param inputContainer The current inputs that are being serialised
		 * @return A {@link SortedFileCommandContainer} with your filecommands for one container that you want serialised
		 */
		public SortedFileCommandContainer onSerialiseEndlineComment(long tick, InputContainer inputContainer) {
			SortedFileCommandContainer out = new SortedFileCommandContainer();
			if (tick >= endlineFileCommandStorage.size())
				return out;

			SortedFileCommandContainer currentTick = endlineFileCommandStorage.get(tick);
			if (currentTick == null)
				return out;

			for (String name : getFileCommandNames()) {
				if (currentTick.get(name) != null)
					out.putAll(currentTick.split(name));
			}
			return out;
		}

		/**
		 * Fired when the {@link PlaybackSerialiser} reads the inputs from a file.<br>
		 * This is used to load your inlineFileCommands from a comment into {@link #inlineFileCommandStorage} to be used in {@link #onPlayback(long, InputContainer)}.
		 * 
		 * @param tick The current tick that is deserialised
		 * @param inputContainer The current inputs that are being deserialised
		 * @param fileCommandContainer The {@link SortedFileCommandContainer} that was deserialised
		 */
		public void onDeserialiseInlineComment(long tick, InputContainer inputContainer, SortedFileCommandContainer fileCommandContainer) {
			if (fileCommandContainer == null)
				return;

			inlineFileCommandStorage.add(fileCommandContainer);
		}

		/**
		 * Fired when the {@link PlaybackSerialiser} reads the inputs from a file.<br>
		 * This is used to load your endlineFileCommands from a comment into {@link #endlineFileCommandStorage} to be used in {@link #onPlayback(long, InputContainer)}.
		 * 
		 * @param tick The current tick that is deserialised
		 * @param inputContainer The current inputs that are being deserialised
		 * @param fileCommandContainer The {@link SortedFileCommandContainer} that was deserialised
		 */
		public void onDeserialiseEndlineComment(long tick, InputContainer inputContainer, SortedFileCommandContainer fileCommandContainer) {
			if (fileCommandContainer == null)
				return;

			endlineFileCommandStorage.add(fileCommandContainer);
		}

		/**
		 * @return {@link #enabled}
		 */
		public boolean isEnabled() {
			return enabled;
		}

		/**
		 * Set {@link #enabled} and run {@link #onEnable()} and {@link #onDisable()}
		 * @param enabled Sets {@link #enabled}
		 */
		public void setEnabled(boolean enabled) {
			if (enabled)
				onEnable();
			else
				onDisable();
			this.enabled = enabled;
		}

		@Override
		public String toString() {
			return getExtensionName();
		}
	}

	/**
	 * <p>List of FileCommands in one comment.
	 * <p>This class is the same as <code>ArrayList&lt;PlaybackFileCommand&gt;</code>
	 * <p>In a comment, you can have multiple file commands, hence a list is needed to store them all.
	 * <h5>Example</h5>
	 * <pre>
	 * // $desyncMonitor(13, 0, 1, 1, 1, 1); $hud(true);
	 * </pre>
	 * <p>This would translate into an ArrayList like
	 * <pre>
	 * [$desyncMonitor(13, 0, 1, 1, 1, 1);, $hud(true);]
	 * </pre>
	 * 
	 * <p>Used in {@link UnsortedFileCommandContainer} for serialisation and deserialisation
	 * <p>Although this class is the same as {@link FileCommandsInTickList}, their use case differs slightly,<br>
	 * hence I created 2 classes for the sake of clarity.
	 * 
	 * @author Scribble
	 */
	public static class FileCommandsInCommentList extends ArrayList<PlaybackFileCommand> {
	}

	/**
	 * <p>An ArrayList for storing {@link FileCommandsInCommentList} sorted by order of appearence in the {@link InputContainer}
	 * <p>This stands in contrast to the {@link SortedFileCommandContainer}, which can be obtained by calling {@link UnsortedFileCommandContainer#sort() sort()}
	 * <p>This is technically a 2 dimensional List for storing file commands for multiple comments, where {@link FileCommandsInCommentList} is one row of file commands
	 * <p>Used in {@link SerialiserFlavorBase} as this format makes it easier to deal with serialisation and deserialisation
	 * <h5>Example</h5>
	 * <pre>
	 * // $desyncMonitor(13, 0, 1, 1, 1, 1); $hud(true);
	 * // $desyncMonitor(16, 3, 1, 1, 1, 1); $hud(false);
	 * // $label(Test); $hud(false);
	 * </pre>
	 * <p>This would translate into an ArrayList like
	 * <pre>
	 * [
	 * 	[$desyncMonitor(13, 0, 1, 1, 1, 1);, $hud(true);],	&lt;- One {@link FileCommandsInCommentList}
	 * 	[$desyncMonitor(16, 3, 1, 1, 1, 1);, $hud(false);],
	 * 	[$label(Test);, $hud(false);]
	 * ]
	 * </pre>
	 * @author Scribble
	 * @see SortedFileCommandContainer
	 */
	public static class UnsortedFileCommandContainer extends ArrayList<FileCommandsInCommentList> {

		/**
		 * <p>Sorts this array list by the file command names
		 * @return A {@link SortedFileCommandContainer}
		 */
		public SortedFileCommandContainer sort() {
			SortedFileCommandContainer out = new SortedFileCommandContainer();

			/*
			 *  Fill the HashMap in SortedFileCommandContainer with empty FileCommandsInCommentList
			 *  for each different FileCommand name found in this UnsortedFileCommandContainer.
			 *  
			 *  We have to do this, since absent FileCommands are set to null.
			 *  
			 *  Example:
			 *  // $desyncMonitor(13, 0, 1, 1, 1, 1); $hud(true);
			 *  // $desyncMonitor(16, 3, 1, 1, 1, 1); $hud(false);
			 *  // $label(Test); $hud(false);
			 *  
			 *  In line 1, the "label" FC is missing
			 *  In line 2, once again, "label is missing
			 *  In line 3, desyncMonitor is missing.
			 *  
			 *  If it's missing, we need to set that spot to null.
			 *  
			 *  So first we create empty Hashmaps for each:
			 * { 
			 * "desyncMonitor": [],
			 * "hud": [],	
			 * "label": []
			 * }
			 * 
			 * Then iterate through all filecommands and set null where a FileCommand is absent
			 */
			for (FileCommandsInCommentList unsortedFileCommandsList : this) {
				if (unsortedFileCommandsList != null) {
					for (PlaybackFileCommand command : unsortedFileCommandsList) {
						out.put(command.getName(), new FileCommandsInTickList());
					}
				}
			}

			/*
			 * Add the FileCommands to the previously created FileCommandsInCommentLists
			 */
			for (FileCommandsInCommentList unsortedFileCommandsList : this) {
				/*
				 * If the file command is not present in the comment, we have to add
				 * null to the sortedFileCommandsList.
				 * 
				 * To do that, we iterate through all entries in the HashMap
				 */
				for (Map.Entry<String, FileCommandsInTickList> entry : out.entrySet()) {

					String sortedKey = entry.getKey();
					FileCommandsInTickList sortedFileCommandsList = entry.getValue();

					boolean valuePresent = false;
					if (unsortedFileCommandsList != null) {
						/*
						 * Iterates through all filecommands in a comment
						 * and adds it to the sorted list if found
						 */
						for (PlaybackFileCommand command : unsortedFileCommandsList) {
							if (sortedKey.equals(command.getName())) {
								valuePresent = true;
								sortedFileCommandsList.add(command);
							}
						}
					}
					/*
					 * If the value is not found,
					 * add null to indicate that the
					 * file command is missing from this comment
					 */
					if (!valuePresent) {
						sortedFileCommandsList.add(null);
					}
				}
			}
			return out;
		}
	}

	/**
	 * <p>List of FileCommands in one tick.
	 * <p>This class is the same as <code>ArrayList&lt;PlaybackFileCommand&gt;</code>
	 * <p>In a tick, you can have multiple file commands for each subtick, hence a list is needed to store them all.
	 * <p>Used in {@link PlaybackFileCommandExtension PlaybackFileCommandExtensions} as this format makes it easier to deal with processing FileCommands during playback or recording
	 * <h5>Example</h5>
	 * <pre>
	 * // $desyncMonitor(13, 0, 1, 1, 1, 1);
	 * // $desyncMonitor(13, 0, 1, 2, 1, 1);
	 * // $desyncMonitor(13, 0, 1, 10, 1, 1);
	 * </pre>
	 * <p>This would translate into an ArrayList like
	 * <pre>
	 * [$desyncMonitor(13, 0, 1, 1, 1, 1);, $desyncMonitor(13, 0, 1, 2, 1, 1);, $desyncMonitor(13, 0, 1, 10, 1, 1);]
	 * </pre>
	 * 
	 * <p>Used in {@link SortedFileCommandContainer} for processing file commands, either playing back or recording
	 * <p>Although this class is the same as {@link FileCommandsInCommentList}, their use case differs slightly,<br>
	 * hence I created 2 classes for the sake of clarity.
	 * 
	 * @author Scribble
	 */
	public static class FileCommandsInTickList extends ArrayList<PlaybackFileCommand> {
	}

	/**
	 * <p>A LinkedHashMap for storing {@link FileCommandsInTickList} sorted by the name of the FileCommand name.
	 * <p>The key represents the FileCommand name, while the elements are the {@link FileCommandsInTickList}
	 * <p>This stands in contrast to the {@link UnsortedFileCommandContainer}, which can be obtained by calling {@link SortedFileCommandContainer#unsort() unsort()}
	 * <p>Used in {@link PlaybackFileCommandExtension PlaybackFileCommandExtensions} as this format makes it easier to distribute the file commands to their respective class extensions
	 * <h5>Example</h5>
	 * <pre>
	 * // $desyncMonitor(13, 0, 1, 1, 1, 1); $hud(true);
	 * // $desyncMonitor(16, 3, 1, 1, 1, 1); $hud(false);
	 * // $label(Test); $hud(false);
	 * </pre>
	 * <p>This would translate into a LinkedHashMap like
	 * <pre>
	 * {
	 * "desyncMonitor":
	 * 	[$desyncMonitor(13, 0, 1, 1, 1, 1);, $desyncMonitor(16, 3, 1, 1, 1, 1);, null],
	 * 
	 * "hud":
	 * 	[$hud(true);, $hud(false), $hud(false)],	&lt;- One {@link FileCommandsInTickList}
	 * 
	 * "label":
	 * 	[null, null, $label(Test)]
	 * }
	 * <p>While null being the subticks that have no file commands of that type
	 * 
	 * <p>Additionally, these entries can be {@link SortedFileCommandContainer#split(Iterable) split} into multiple containers.
	 * </pre>
	 * @author Scribble
	 */
	public static class SortedFileCommandContainer extends LinkedHashMap<String, FileCommandsInTickList> {

		/**
		 * <p>Adds a new {@link PlaybackFileCommand} to the specified key.
		 * <p>Creates a new {@link FileCommandsInTickList} if it's not already present
		 * @param key The key for the list to add
		 * @param fileCommand The {@link PlaybackFileCommand} to add to the list
		 */
		public void add(String key, PlaybackFileCommand fileCommand) {
			FileCommandsInTickList toAdd = getOrDefault(key, new FileCommandsInTickList());
			if (toAdd.isEmpty()) {
				put(key, toAdd);
			}

			toAdd.add(fileCommand);
		}

		/**
		 * <p>Creates a new {@link SortedFileCommandContainer} with only the keys present
		 * @param keys The keys to split into
		 * @return A new {@link SortedFileCommandContainer} with only the keys present
		 */
		public SortedFileCommandContainer split(String... keys) {
			return split(Arrays.asList(keys));
		}

		/**
		 * <p>Creates a new {@link SortedFileCommandContainer} with only the keys present
		 * @param keys The keys to split into
		 * @return A new {@link SortedFileCommandContainer} with only the keys present
		 */
		public SortedFileCommandContainer split(Iterable<String> keys) {
			SortedFileCommandContainer out = new SortedFileCommandContainer();
			for (String key : keys) {
				if (this.containsKey(key))
					out.put(key, this.get(key));
			}
			return out;
		}

		/**
		 * Sorts this HashMap by order of appeareance and merges filecommands into one line
		 * @return An {@link UnsortedFileCommandContainer}
		 */
		public UnsortedFileCommandContainer unsort() {
			UnsortedFileCommandContainer out = new UnsortedFileCommandContainer();

			int biggestSize = 0;
			for (FileCommandsInTickList list : values()) {
				if (list.size() > biggestSize) {
					biggestSize = list.size();
				}
			}

			for (int i = 0; i < biggestSize; i++) {
				FileCommandsInCommentList unsortedFileCommandsList = new FileCommandsInCommentList();
				for (FileCommandsInTickList list : values()) {
					if (i < list.size()) {
						PlaybackFileCommand fileCommand = list.get(i);
						unsortedFileCommandsList.add(fileCommand);
					} else {
						unsortedFileCommandsList.add(null);
					}
				}
				out.add(unsortedFileCommandsList);
			}

			return out;
		}

		@Override
		public boolean equals(Object o) {
			if (o instanceof SortedFileCommandContainer) {
				SortedFileCommandContainer other = (SortedFileCommandContainer) o;
				if (this.size() != other.size())
					return false;
				for (java.util.Map.Entry<String, FileCommandsInTickList> entry : other.entrySet()) {
					String key = entry.getKey();
					FileCommandsInTickList val = entry.getValue();

					if (!this.containsKey(key) && !this.get(key).equals(val))
						return false;
				}
				return true;
			}
			return super.equals(o);
		}
	}
}

