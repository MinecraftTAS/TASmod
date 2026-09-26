package com.minecrafttas.tasmod.savestates;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import com.minecrafttas.tasmod.mctcommon.file.AbstractDataFile;
import com.minecrafttas.tasmod.TASmod;
import com.minecrafttas.tasmod.savestates.exceptions.LoadstateException;
import com.minecrafttas.tasmod.savestates.exceptions.SavestateDeleteException;
import com.minecrafttas.tasmod.savestates.exceptions.SavestateException;
import com.minecrafttas.tasmod.savestates.files.SavestateTrackerFile;
import com.minecrafttas.tasmod.util.I18n;

/**
 * Manages the savestates on the filesystem and assignes new indices
 * 
 * @author Scribble
 */
public class SavestateIndexer {

	/**
	 * The logger
	 */
	private final Logger logger;

	/**
	 * The Minecraft saves dir, usually <code>.minecraft/saves</code>
	 */
	private Path savesDir;
	/**
	 * The base directory of savestates, usually <code>.minecraft/saves/savestates</code>
	 */
	private final Path savestateBaseDirectory;
	/**
	 * The name of the world that is currently open
	 */
	private final String worldname;
	/**
	 * The name of the current savestate dir, usually <code>.minecraft/saves/savestates/worldname-Savestates</code>
	 */
	private final Path currentSavestateDir;

	/**
	 * The list of savestates 
	 */
	private final LinkedHashMap<Integer, Savestate> savestateList;
	/**
	 * The current savestate that is loaded
	 */
	private Savestate currentSavestate;

	/**
	 * The data directory within a savestate, usually <code>.minecraft/saves/savestates/worldname-Savestates/worldname-Savestate1/tas</code>
	 */
	public static final Path savestateDataDir = Paths.get("tas");
	/**
	 * The savestate file within a savestate, usually <code>.minecraft/saves/savestates/worldname-Savestates/worldname-Savestate1/tas/savestate.json</code>
	 */
	private static final Path savestateFilePath = savestateDataDir.resolve("savestate.json");

	/**
	 * The file keeping a tally of total savestates and loadstates
	 */
	private final SavestateTrackerFile trackerfile;

	/**
	 * Creates a new savestate indexer
	 * @param logger The logger used for logging
	 * @param savesDir The Minecraft saves directory
	 * @param savestateBaseDirectory The base directory of savestates
	 * @param worldname The name of the world that is currently open
	 */
	public SavestateIndexer(Logger logger, Path savesDir, Path savestateBaseDirectory, String worldname) {
		this.logger = logger;
		this.savestateBaseDirectory = savestateBaseDirectory;
		this.savesDir = savesDir;
		this.worldname = worldname;
		this.currentSavestateDir = savestateBaseDirectory.resolve(String.format("%s-Savestates", worldname));
		this.trackerfile = new SavestateTrackerFile(currentSavestateDir.resolve(String.format(worldname + "-info.txt")));
		savestateList = new LinkedHashMap<>();
		createSavestateDir();

		Path savestateDat = savesDir.resolve(worldname).resolve(savestateFilePath);
		if (Files.exists(savestateDat)) {
			currentSavestate = new Savestate(savestateDat, null);
			currentSavestate.load();
		} else {
			currentSavestate = new Savestate(savestateDat, 0, null, null, null);
		}
		reload();
	}

	/**
	 * Creates the directories leading to {@link #currentSavestateDir} 
	 */
	private void createSavestateDir() {
		try {
			Files.createDirectories(currentSavestateDir);
		} catch (IOException e) {
			logger.catching(e);
		}
	}

	/**
	 * Create a new savestate
	 * @param index The index to save
	 * @return The {@link SavestatePaths}
	 */
	public SavestatePaths createSavestate(int index) {
		return createSavestate(index, null, true);
	}

	/**
	 * Creates a new savestate with parameters
	 * @param index The index to save
	 * @param name The name of the savestate
	 * @param changeIndex True if the index should be changed.
	 * @return The {@link SavestatePaths}
	 */
	public SavestatePaths createSavestate(int index, String name, boolean changeIndex) {
		logger.trace("Creating a savestate in indexer");

		index = getNextIndex(index);

		if (name == null) {
			name = "Savestate #" + index;
		}

		int savedIndex = index;

		currentSavestate.index = index;
		currentSavestate.name = name;
		currentSavestate.date = new Date();

		currentSavestate.save();

		if (!changeIndex)
			currentSavestate.index = savedIndex;

		Path sourceDir = savesDir.resolve(worldname);
		Path targetDir = currentSavestateDir.resolve(worldname + "-Savestate" + index);

		Savestate newSavestate = currentSavestate.clone(targetDir.resolve(savestateFilePath), targetDir);

		trackerfile.increaseSavestateCount();

		savestateList.put(index, newSavestate);
		sortSavestateList();

		return SavestatePaths.of(newSavestate.clone(), sourceDir, targetDir);
	}

	/**
	 * Loads a savestate
	 * @param index The index to load
	 * @param changeIndex True if the index should be changed.
	 * @return The {@link SavestatePaths}
	 * @throws LoadstateException If it can't find the savestate
	 */
	public SavestatePaths loadSavestate(int index, boolean changeIndex) throws LoadstateException {
		logger.trace("Loading a savestate in indexer");
		if (index < 0) {
			index = currentSavestate.getIndex();

			if (savestateList.containsKey(index)) {
				index = findLatestIndex(index);
			}
		}

		Savestate savestateToLoad = savestateList.get(index);

		if (savestateToLoad == null) {
			String translationKey = savestateList.size() == 0 ? "msg.tasmod.savestate.error.none" : "msg.tasmod.savestate.error.noexist";
			throw new LoadstateException(translationKey, index);
		}

		int savedIndex = currentSavestate.index;
		this.currentSavestate = savestateToLoad.clone(savesDir.resolve(worldname).resolve(savestateFilePath), savesDir.resolve(worldname));

		Path sourceDir = savestateToLoad.getFolder();
		Path targetDir = savesDir.resolve(worldname);

		if (sourceDir != null && !Files.exists(sourceDir)) {
			Path missingFile = savesDir.relativize(sourceDir);
			throw new LoadstateException("msg.tasmod.savestate.error.filenoexist", missingFile);
		}

		SavestatePaths out = SavestatePaths.of(savestateToLoad.clone(), sourceDir, targetDir);

		trackerfile.increaseLoadstateCount();

		if (!changeIndex)
			currentSavestate.index = savedIndex;

		return out;
	}

	/**
	 * Renames a savestate
	 * @param index The index to rename
	 * @param name The new name
	 * @return The {@link SavestatePaths}
	 * @throws SavestateException If the savestate doesn't exist or it can't be renamed
	 */
	public SavestatePaths renameSavestate(int index, String name) throws SavestateException {
		Savestate savestateToRename = savestateList.get(index);

		if (savestateToRename == null) {
			throw new SavestateException(I18n.format("msg.tasmod.savestate.error.noexist", index));
		} else if (savestateToRename instanceof FailedSavestate) {
			throw new SavestateException(I18n.format("msg.tasmod.savestate.rename.error"));
		}

		if (name.isEmpty()) {
			name = "Savestate #" + index;
		}

		savestateToRename.name = name;
		savestateToRename.save();

		return SavestatePaths.of(savestateToRename, null, null);
	}

	/**
	 * Renames the current savestate (aka the savestate in the .minecraft/saves/worlname) folder
	 * @param name The name to rename the savestate to
	 */
	public void renameCurrent(String name) {
		currentSavestate.name = name;
		currentSavestate.save();
	}

	/**
	 * Deletes a savestate
	 * @param index The index to delete
	 * @return The {@link SavestatePaths}
	 * @throws SavestateDeleteException If the savestate doesn't exist
	 */
	public SavestatePaths deleteSavestate(int index) throws SavestateDeleteException {
		logger.trace("Deleting savestate {}", index);

		if (index == 0) {
			throw new SavestateDeleteException("msg.tasmod.savestate.delete.error.zero");
		}

		if (!savestateList.containsKey(index)) {
			throw new SavestateDeleteException(I18n.format("msg.tasmod.savestate.error.noexist", index));
		}

		Savestate toDelete = savestateList.get(index);
		Path targetDir = toDelete.getFolder();
		SavestatePaths out = SavestatePaths.of(toDelete, null, targetDir);

		savestateList.remove(index);

		if (!savestateList.containsKey(currentSavestate.index)) {
			currentSavestate.index = findLatestIndex(currentSavestate.index);
		}

		return out;
	}

	/**
	 * Deletes multiple savestates
	 * @param from The starting savestates
	 * @param to The end of the savestates (inclusive)
	 * @param onDelete Runnable that is run every time a savestate is deleted
	 * @param onError Runnable that is run ever time a savestate fails to be deleted
	 */
	public void deleteMultipleSavestates(int from, int to, DeletionRunnable onDelete, ErrorRunnable onError) {
		if (from >= to) {
			onError.run(new SavestateDeleteException("Can't delete amounts that are negative or 0"));
			return;
		}
		for (int i = from; i <= to; i++) {
			try {
				onDelete.run(deleteSavestate(i));
			} catch (Exception e) {
				if (onError != null)
					onError.run(e);
			}
		}
	}

	/**
	 * Sorts the savestate list by key to keep an order
	 */
	private void sortSavestateList() {
		LinkedHashMap<Integer, Savestate> copy = new LinkedHashMap<>();
		//@formatter:off
		savestateList.entrySet().stream()
			.sorted(Map.Entry.comparingByKey())
			.forEach(entry -> copy.put(entry.getKey(), entry.getValue()));
		//@formatter:on
		savestateList.clear();
		savestateList.putAll(copy);
	}

	/**
	 * Reloads the {@link #savestateList} from the disk
	 */
	public void reload() {
		logger.trace("Reloading savestate indexes");
		savestateList.clear();
		Thread t = new Thread(new Runnable() {

			@SuppressWarnings("deprecation")
			@Override
			public void run() {
				Stream<Path> stream = null;
				try {
					stream = Files.list(currentSavestateDir); // Get a list of paths in the specified directory
				} catch (IOException e) {
					logger.catching(e);
					return;
				}

				//@formatter:off
				Set<Path> pathSet = stream
						.filter(file -> Files.isDirectory(file))
						.filter(file -> file.getFileName().toString().startsWith(worldname))
						.collect(Collectors.toSet());
				//@formatter:on

				stream.close();

				Pattern backupIndexPattern = Pattern.compile("-Savestate(\\d+)$");

				pathSet.forEach(path -> {
					Path savestateDat = path.resolve(savestateFilePath);

					Savestate savestate = null;

					/*
					 * Read the index from the folder if the savestate file
					 * doesn't exist
					 */
					if (Files.exists(savestateDat)) {
						savestate = new Savestate(savestateDat, path);
						savestate.load();
					} else if (Files.exists(path.resolve("tasmod/savestateData.txt"))) { // Legacy format

						Path tasmodDir = path.resolve("tasmod");
						Path tasDir = tasmodDir.getParent().resolve(savestateDataDir);
						copyFolder(tasmodDir, tasDir);
						deleteFolder(tasmodDir);

						Path legacyPath = tasDir.resolve("savestateData.txt");
						com.minecrafttas.tasmod.savestates.files.SavestateDataFile dataFile = new com.minecrafttas.tasmod.savestates.files.SavestateDataFile(legacyPath);
						dataFile.load();
						int index = Integer.parseInt(dataFile.get(com.minecrafttas.tasmod.savestates.files.SavestateDataFile.DataValues.INDEX));

						try {
							Files.delete(legacyPath);
						} catch (IOException e) {
							e.printStackTrace();
						}

						savestate = new Savestate(savestateDat, index, "Savestate #" + index, null, path);
						savestate.save();
					} else {
						String filename = path.getFileName().toString();
						Matcher matcher = backupIndexPattern.matcher(filename);
						int backupIndex = -1;
						if (matcher.find()) {
							backupIndex = Integer.parseInt(matcher.group(1));
						}

						logger.warn("Savestate {} does not contain a valid savestate.json, skipping", backupIndex);
						Throwable error = new SavestateException("Savestate.json data file not found in " + savestateBaseDirectory.relativize(savestateDat));
						savestate = new FailedSavestate(path, backupIndex, null, null, error);
					}
					if (savestate.index != null) // Temporary savestates have no index and are not listed in the savestatelist
						savestateList.put(savestate.getIndex(), savestate.clone());
				});
				sortSavestateList();
				try {
					currentSavestate.index = findLatestIndex(currentSavestate.index);
				} catch (Exception e) {
					logger.catching(e);
				}
			}
		}, "Savestate Reload");
		t.run();
	}

	/**
	 * @return The indexes from the {@link #savestateList}
	 */
	public Set<Integer> getIndexList() {
		return savestateList.keySet();
	}

	/**
	 * @return The savestate list
	 */
	public List<Savestate> getSavestateList() {
		return getSavestateList(currentSavestate.index);
	}

	/**
	 * @return The savestate list with a specific center
	 */
	public List<Savestate> getSavestateList(int center) {
		return getSavestateList(center, 10);
	}

	/**
	 * @return The savestate list with a specific center and amount
	 */
	public List<Savestate> getSavestateList(int center, int amount) {
		List<Savestate> out = new LinkedList<>();
		if (center < 0) {
			savestateList.forEach((key, value) -> out.add(value));
			return out;
		}

		LinkedHashMap<Integer, Savestate> copy = new LinkedHashMap<>(savestateList);
		int delta = ((int) amount / 2);

		for (int i = center - delta; i <= center + delta; i++) {
			Savestate entry = copy.get(i);
			if (entry != null)
				out.add(entry);
		}
		return out;
	}

	/**
	 * @return The size of the {@link #savestateList}t
	 */
	public int size() {
		return savestateList.size();
	}

	/**
	 * Finds the latest index of a savestate
	 * @param start The starting index
	 * @return The latest index
	 */
	public int findLatestIndex(int start) {
		if (savestateList.containsKey(start))
			return start;

		for (int i = start; i >= 0; i--) {
			if (savestateList.containsKey(i) && !(savestateList.get(i) instanceof FailedSavestate)) {
				return i;
			}
		}
		return 0;
	}

	/**
	 * @return The {@link #currentSavestate}
	 */
	public Savestate getCurrentSavestate() {
		return currentSavestate;
	}

	/**
	 * @return The {@link Savestate#index} from the {@link #currentSavestate}
	 */
	public int getCurrentIndex() {
		return getCurrentSavestate().index;
	}

	/**
	 * Data class, containing information about the Savestate
	 * 
	 * <ul>
	 * 	<li>The savestate index</li>
	 * 	<li>The name of the savestate</li>
	 * 	<li>The time and date of creation</li>
	 * 	<li>The folder name</li>
	 * </ul>
	 * 
	 * @author Scribble
	 */
	public class Savestate extends AbstractDataFile {

		@Nullable
		protected Integer index;
		@Nullable
		protected String name;
		@Nullable
		protected Date date;
		protected Path folder;
		protected Logger logger = TASmod.LOGGER;

		private Savestate(Path datFile, Path folder) {
			this(datFile, null, null, null, folder);
		}

		private Savestate(Path file, Integer index, String name, Date date, Path folder) {
			super(file, "Savestate", "Stores savestate related data");
			this.index = index;
			this.name = name;
			this.date = date;
			this.folder = folder;
		}

		private Savestate(Path file, Properties properties, Integer index, String name, Date date, Path folder) {
			this(file, index, name, date, folder);
			this.properties = properties;
		}

		public Integer getIndex() {
			return index;
		}

		public String getName() {
			return name;
		}

		public Date getDate() {
			return date;
		}

		public Path getFolder() {
			return folder;
		}

		@Override
		public void save() {
			if (index != null)
				properties.setProperty(Options.INDEX.toString(), Integer.toString(index));
			if (name != null)
				properties.setProperty(Options.NAME.toString(), name);
			if (date != null)
				properties.setProperty(Options.DATE.toString(), Long.toString(ChronoUnit.SECONDS.between(Instant.EPOCH, date.toInstant())));
			super.saveToJson();
		}

		@Override
		public void load() {
			super.loadFromJson();
			try {
				String loadedIndex = properties.getProperty(Options.INDEX.toString());
				if (loadedIndex != null)
					this.index = Integer.parseInt(loadedIndex);
			} catch (Exception e) {
				logger.error("Can't parse '{}' in {}", Options.INDEX.toString(), currentSavestateDir.resolve(savestateFilePath));
				logger.catching(e);
			}
			this.name = properties.getProperty(Options.NAME.toString());
			try {
				String loadedDate = properties.getProperty(Options.DATE.toString());
				if (loadedDate != null)
					this.date = parseDate(loadedDate);
			} catch (Exception e) {
				logger.error("Can't parse '{}' in {}", Options.DATE.toString(), currentSavestateDir.resolve(savestateFilePath));
				logger.catching(e);
			}
		}

		@Override
		protected Savestate clone() {
			return new Savestate(file, properties, index, name, date, folder);
		}

		/**
		 * Clone with a new file
		 * @param newFile The new file to point to
		 * @return The new savestate
		 */
		protected Savestate clone(Path newFile, Path newFolder) {
			return new Savestate(newFile, properties, index, name, date, newFolder);
		}

		private Date parseDate(String dateString) throws Exception {
			long unixTimestamp = Long.parseLong(dateString);
			return Date.from(Instant.ofEpochSecond(unixTimestamp));
		}
	}

	/**
	 * A savestate failing to save
	 * 
	 * @author Scribble
	 */
	public class FailedSavestate extends Savestate {

		private final Throwable t;

		public FailedSavestate(Path file, Throwable t) {
			this(file, null, null, null, t);
		}

		public FailedSavestate(Path file, Integer index, String name, Date date, Throwable t) {
			super(file, index, name, date, null);
			this.t = t;
		}

		public FailedSavestate(Path file, Properties properties, Integer index, String name, Date date, Throwable t) {
			super(file, index, name, date, null);
			this.t = t;
		}

		public Throwable getError() {
			return t;
		}

		@Override
		public void saveToJson() {
		}

		@Override
		public void save() {
		}

		@Override
		public void loadFromJson() {
		}

		@Override
		public void load() {
		}

		@Override
		protected FailedSavestate clone() {
			return new FailedSavestate(file, properties, index, name, date, t);
		}
	}

	/**
	 * Data class containing:
	 * <ul>
	 * 	<li>The {@link Savestate}</li>
	 * 	<li>The source folder</li>
	 * 	<li>The target folder of the savestate</li>
	 * </ul>
	 * 
	 * @author Scribble
	 */
	public static class SavestatePaths {
		private final Savestate savestate;
		private final Path sourceFolder;
		private final Path targetFolder;

		private SavestatePaths(Savestate savestate, Path sourceFolder, Path targetFolder) {
			this.savestate = savestate;
			this.sourceFolder = sourceFolder;
			this.targetFolder = targetFolder;
		}

		public Savestate getSavestate() {
			return savestate;
		}

		public Path getSourceFolder() {
			return sourceFolder;
		}

		public Path getTargetFolder() {
			return targetFolder;
		}

		public static SavestatePaths of(Savestate savestate, Path sourceFolder, Path targetFolder) {
			return new SavestatePaths(savestate, sourceFolder, targetFolder);
		}
	}

	/**
	 * Copies a folder recursively. Replaces existing files
	 * @param src The source folder to copy
	 * @param dest The destination
	 */
	public static void copyFolder(Path src, Path dest) {
		try {
			Files.walk(src).forEach(s -> {
				try {
					Path d = dest.resolve(src.relativize(s));
					if (Files.isDirectory(s)) {
						if (!Files.exists(d))
							Files.createDirectory(d);
						return;
					}
					Files.copy(s, d, StandardCopyOption.REPLACE_EXISTING);
				} catch (Exception e) {
					e.printStackTrace();
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Deletes a folder recursively
	 * @param toDelete The folder to delete
	 */
	public static void deleteFolder(Path toDelete) {
		try {
			Files.walk(toDelete).forEach(s -> {
				if (toDelete.equals(s))
					return;
				if (Files.isDirectory(s)) {
					deleteFolder(s);
				} else {
					try {
						Files.delete(s);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			});
			Files.delete(toDelete);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@FunctionalInterface
	public interface DeletionRunnable {
		public void run(SavestatePaths paths);
	}

	@FunctionalInterface
	public interface ErrorRunnable {
		public void run(Exception e);
	}

	private enum Options {
		INDEX,
		NAME,
		DATE;

		@Override
		public String toString() {
			return super.toString().toLowerCase();
		}
	}

	public Path getCurrentSavestateDir() {
		return currentSavestateDir;
	}

	public boolean exists(int index) {
		index = getNextIndex(index);
		return savestateList.containsKey(index);
	}

	/**
	 * @param index The current index. -1 if the next index should be used
	 * @return The next index to save into
	 */
	public int getNextIndex(int index) {
		if (index < 0) {
			index = currentSavestate.getIndex() + 1;
		}
		return index;
	}

	public SavestatePaths createTempSavestate() {
		Path sourceDirectory = savesDir.resolve(worldname);
		Path targetDirectory = currentSavestateDir.resolve(worldname + "-SavestateTemp");
		Savestate tempSavestate = new Savestate(targetDirectory.resolve(savestateFilePath), null, "Temporary Savestate!", new Date(), targetDirectory);
		return SavestatePaths.of(tempSavestate, sourceDirectory, targetDirectory);
	}

	public SavestatePaths loadTempSavestate() {
		Path sourceDirectory = currentSavestateDir.resolve(worldname + "-SavestateTemp");
		if (!Files.exists(sourceDirectory))
			return null;
		Path targetDirectory = savesDir.resolve(worldname);
		return SavestatePaths.of(currentSavestate.clone(), sourceDirectory, targetDirectory);
	}
}

