package com.minecrafttas.tasmod.playback.tasfile;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import com.dselent.bigarraylist.BigArrayList;
import com.minecrafttas.tasmod.playback.PlaybackControllerClient;
import com.minecrafttas.tasmod.playback.PlaybackControllerClient.InputContainer;
import com.minecrafttas.tasmod.playback.tasfile.exception.PlaybackLoadException;
import com.minecrafttas.tasmod.playback.tasfile.exception.PlaybackSaveException;
import com.minecrafttas.tasmod.playback.tasfile.flavor.SerialiserFlavorBase;
import com.minecrafttas.tasmod.registries.TASmodAPIRegistry;
import com.minecrafttas.tasmod.savestates.SavestateHandlerClient;
import com.minecrafttas.tasmod.util.FileThread;

/**
 * Loads and stores the {@link PlaybackControllerClient} to/from a file.<br>
 * 
 * @author Scribble
 */
public class PlaybackSerialiser {

	/**
	 * The default flavor name used for saving TASfiles when no flavor is specified,<br>
	 * used in {@link #saveToFile(Path, BigArrayList, String, long)}
	 */
	private static String defaultFlavor = "beta1";

	/**
	 * Saves the {@link PlaybackControllerClient} to a file
	 * 
	 * @param file The file to save the serialised inputs to
	 * @param controller The {@link PlaybackControllerClient} to use. Uses the {@link PlaybackControllerClient#getInputs() getInputs()} method, to extract the ticks.
	 * @param flavorName The name of the {@link SerialiserFlavorBase flavor} to use for the tasfile
	 * @throws PlaybackSaveException When a saving operation fails
	 */
	public static void saveToFile(Path file, PlaybackControllerClient controller, String flavorName) throws PlaybackSaveException {
		saveToFile(file, controller, flavorName, -1L);
	}

	/**
	 * Saves the {@link PlaybackControllerClient} <i>partially</i> to a file
	 * 
	 * @param file The file to save the serialised inputs to
	 * @param controller The {@link PlaybackControllerClient} to use. Uses the {@link PlaybackControllerClient#getInputs() getInputs()} method, to extract the ticks.
	 * @param flavorName The name of the {@link SerialiserFlavorBase flavor} to use for the tasfile
	 * @param stopIndex The index at which the serialiser stops. Use -1L to parse the entire file 
	 * @throws PlaybackSaveException When a saving operation fails
	 */
	public static void saveToFile(Path file, PlaybackControllerClient controller, String flavorName, long stopIndex) throws PlaybackSaveException {
		if (controller == null) {
			throw new PlaybackSaveException("Save to file failed. No controller specified");
		}
		saveToFile(file, controller.getInputs(), flavorName, stopIndex);
	}

	/**
	 * Saves a BigArrayList of {@link InputContainer InputContainers} to a file
	 * 
	 * @param file The file to save the serialised inputs to
	 * @param container The list of {@link InputContainer InputContainers} to use
	 * @param flavorName The name of the {@link SerialiserFlavorBase flavor} to use for the tasfile
	 * @throws PlaybackSaveException When a saving operation fails
	 */
	public static void saveToFile(Path file, BigArrayList<InputContainer> container, String flavorName) throws PlaybackSaveException {
		saveToFile(file, container, flavorName, -1);
	}

	/**
	 * Saves a BigArrayList of {@link InputContainer InputContainers} <i>partially</i> to a file
	 * @param path The file to save the serialised inputs to
	 * @param container The list of {@link InputContainer InputContainers} to use
	 * @param flavorName The name of the {@link SerialiserFlavorBase flavor} to use for the tasfile
	 * @param stopIndex The index at which the serialiser stops. Use -1L to parse the entire file 
	 * @throws PlaybackSaveException When a saving operation fails
	 */
	public static void saveToFile(Path path, BigArrayList<InputContainer> container, String flavorName, long stopIndex) throws PlaybackSaveException {
		if (path == null) {
			throw new PlaybackSaveException("Save to file failed. No file specified");
		}

		if (container == null) {
			throw new PlaybackSaveException("Save to file failed. No inputcontainer list specified");
		}

		if (flavorName == null || flavorName.isEmpty()) {
			if (defaultFlavor == null || defaultFlavor.isEmpty())
				throw new PlaybackSaveException("No default flavor specified... Please specify a flavor name first");
			flavorName = defaultFlavor;
		}

		FileThread writerThread;
		try {
			writerThread = new FileThread(path, false);
		} catch (IOException e) {
			throw new PlaybackSaveException(e, "Trying to save the file %s, but the file can't be created", path.getFileName().toString());
		}
		writerThread.start();

		SerialiserFlavorBase flavor = TASmodAPIRegistry.SERIALISER_FLAVOR.getFlavor(flavorName);

		if (flavor == null) {
			throw new PlaybackSaveException("Flavor %s doesn't exist", flavorName);
		}

		defaultFlavor = flavorName;

		List<String> header = flavor.serialiseHeader();
		for (String line : header) {
			writerThread.addLine(line);
		}

		BigArrayList<String> tickLines = flavor.serialise(container, stopIndex);
		for (String tickLine : tickLines) {
			writerThread.addLine(tickLine);
		}

		writerThread.close();
	}

	/**
	 * Loads a BigArrayList of {@link InputContainer InputContainers} from a file.<br>
	 * Tries to determine the {@link SerialiserFlavorBase flavor} by reading the header of the TASfile
	 * 
	 * @param file The file to load from
	 * @return The loaded BigArrayList of {@link InputContainer InputContainers}
	 * @throws PlaybackLoadException If the file contains errors
	 * @throws IOException If the file could not be read
	 */
	public static BigArrayList<InputContainer> loadFromFile(Path file) throws PlaybackLoadException, IOException {
		return loadFromFile(file, true);
	}

	/**
	 * Loads a BigArrayList of {@link InputContainer InputContainers} from a file.<br>
	 * Tries to determine the {@link SerialiserFlavorBase flavor} by reading the header of the TASfile
	 * 
	 * <p>Has a parameter to stop processing extension data, usually used when
	 * {@link SavestateHandlerClient#loadstate(String) loading savestates} to prevent loadstates from overwriting data
	 * 
	 * @param file The file to load from
	 * @param processExtensions Sets {@link SerialiserFlavorBase#processExtensions}
	 * @return The loaded BigArrayList of {@link InputContainer InputContainers}
	 * @throws PlaybackLoadException If the file contains errors
	 * @throws IOException If the file could not be read
	 */
	public static BigArrayList<InputContainer> loadFromFile(Path file, boolean processExtensions) throws PlaybackLoadException, IOException {
		if (file == null) {
			throw new PlaybackLoadException("Load from file failed. No file specified");
		}
		if (!Files.exists(file)) {
			throw new PlaybackLoadException("Trying to load %s but the file doesn't exist", file.getFileName().toString());
		}

		SerialiserFlavorBase flavor = readFlavor(file);

		flavor.setProcessExtensions(processExtensions);

		return loadFromFile(file, flavor);
	}

	/**
	 * Loads a BigArrayList of {@link InputContainer InputContainers} from a file, with a specific flavor
	 * 
	 * @param file The file to load from
	 * @param flavorName The name of the {@link SerialiserFlavorBase flavor} to use. If the detected flavor in the TASfile mismatches, a {@link PlaybackLoadException} is thrown
	 * @return The loaded BigArrayList of {@link InputContainer InputContainers}
	 * @throws PlaybackLoadException If the file contains errors
	 * @throws IOException If the file could not be read
	 */
	public static BigArrayList<InputContainer> loadFromFile(Path file, String flavorName) throws PlaybackLoadException, IOException {
		return loadFromFile(file, flavorName, true);
	}

	/**
	 * Loads a BigArrayList of {@link InputContainer InputContainers} from a file, with a specific flavor
	 * 
	 * <p>Has a parameter to stop processing extension data, usually used when
	 * {@link SavestateHandlerClient#loadstate(String) loading savestates} to prevent loadstates from overwriting data
	 * 
	 * @param file The file to load from
	 * @param flavorName The name of the {@link SerialiserFlavorBase flavor} to use. If the detected flavor in the TASfile mismatches, a {@link PlaybackLoadException} is thrown
	 * @param processExtensions Sets {@link SerialiserFlavorBase#processExtensions}
	 * @return The loaded BigArrayList of {@link InputContainer InputContainers}
	 * @throws PlaybackLoadException If the file contains errors
	 * @throws IOException If the file could not be read
	 */
	public static BigArrayList<InputContainer> loadFromFile(Path file, String flavorName, boolean processExtensions) throws PlaybackLoadException, IOException {

		// If the flavor is null or empty, try to determine the flavor by reading the header
		if (flavorName == null || flavorName.isEmpty()) {
			return loadFromFile(file);
		}

		// Try to get the flavor from the registry via its name
		SerialiserFlavorBase flavor = TASmodAPIRegistry.SERIALISER_FLAVOR.getFlavor(flavorName);

		if (flavor == null) {
			throw new PlaybackLoadException("Flavor name %s doesn't exist.", flavorName);
		}

		// Read the head of the TASfile to check if the flavors match
		SerialiserFlavorBase flavorInFile = readFlavor(file);
		if (!flavor.equals(flavorInFile)) {
			throw new PlaybackLoadException("Detected flavor %s in the TASfile, which does not match the specified flavor: %s", flavorInFile.getExtensionName(), flavor.getExtensionName());
		}

		flavor.setProcessExtensions(processExtensions);

		return loadFromFile(file, flavor);
	}

	/**
	 * Loads a BigArrayList of {@link InputContainer InputContainers} from a file, with a specific flavor
	 * 
	 * @param file The file to load from
	 * @param flavor The {@link SerialiserFlavorBase flavor} to use. If the detected flavor in the TASfile mismatches, a {@link PlaybackLoadException} is thrown
	 * @return The loaded BigArrayList of {@link InputContainer InputContainers}
	 * @throws PlaybackLoadException If the file contains errors
	 * @throws IOException If the file could not be read
	 */
	public static BigArrayList<InputContainer> loadFromFile(Path file, SerialiserFlavorBase flavor) throws PlaybackLoadException, IOException {
		if (file == null) {
			throw new PlaybackLoadException("Load from file failed. No file specified");
		}

		// Read file
		BufferedReader reader = null;

		try {
			reader = new BufferedReader(new FileReader(file.toFile()));
		} catch (FileNotFoundException e) {
			throw new PlaybackLoadException("Trying to load %s, but the file doesn't exist", file.getFileName().toString());
		}

		BigArrayList<String> lines = new BigArrayList<>();
		String line = null;
		while ((line = reader.readLine()) != null) {
			lines.add(line);
		}
		reader.close();

		// Deserialise Header
		List<String> headerLines = flavor.extractHeader(lines);
		flavor.deserialiseHeader(headerLines);

		// Deserialise main data
		BigArrayList<InputContainer> deserialisedContainers = flavor.deserialise(lines, headerLines.size());

		return deserialisedContainers;
	}

	/**
	 * Searches in a list of lines if one of the {@link SerialiserFlavorBase flavors} matches
	 * @param lines The lines to search through
	 * @param flavorList The list of {@link SerialiserFlavorBase flavor} to check for
	 * @return A copy of the {@link SerialiserFlavorBase flavor} that was found
	 * @throws PlaybackLoadException If no {@link SerialiserFlavorBase flavor} was found
	 */
	public static SerialiserFlavorBase searchForFlavor(List<String> lines, List<SerialiserFlavorBase> flavorList) {
		for (SerialiserFlavorBase flavor : flavorList) {
			if (flavor.checkFlavorName(lines)) {
				return flavor.clone();
			}
		}
		throw new PlaybackLoadException("Couldn't find a flavorname in the file. TASfile is missing a flavor-extension or the file is broken");
	}

	/**
	 * Reads the first 100 lines of the TASfile and checks for a flavorname in the file
	 * @param file The file to search through
	 * @return A copy of the {@link SerialiserFlavorBase flavor} that was found
	 * @throws PlaybackLoadException If an error was found during reading
	 * @throws IOException If the reading fails
	 */
	public static SerialiserFlavorBase readFlavor(Path file) throws PlaybackLoadException, IOException {
		// Read file
		BufferedReader reader = null;

		try {
			reader = new BufferedReader(new FileReader(file.toFile()));
		} catch (FileNotFoundException e) {
			throw new PlaybackLoadException("Trying to load %s but the file doesn't exist", file.getFileName().toString());
		}

		List<String> lines = new ArrayList<>();
		String line = null;

		// Reads the first 100 lines to search for the flavor
		for (int i = 0; i < 100; i++) {

			line = reader.readLine();

			if (line != null) {
				lines.add(line);
			}
		}
		reader.close();

		SerialiserFlavorBase flavor = null;

		flavor = searchForFlavor(lines, TASmodAPIRegistry.SERIALISER_FLAVOR.getFlavors());
		return flavor;
	}
}

