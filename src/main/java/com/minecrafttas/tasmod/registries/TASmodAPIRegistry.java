package com.minecrafttas.tasmod.registries;

import com.minecrafttas.tasmod.commands.client.ClientCommandBase;
import com.minecrafttas.tasmod.commands.client.ClientCommandRegistry;
import com.minecrafttas.tasmod.playback.filecommands.PlaybackFileCommandsRegistry;
import com.minecrafttas.tasmod.playback.metadata.PlaybackMetadataRegistry;
import com.minecrafttas.tasmod.playback.tasfile.flavor.SerialiserFlavorBase;
import com.minecrafttas.tasmod.playback.tasfile.flavor.SerialiserFlavorRegistry;
import com.minecrafttas.tasmod.playback.tasfile.flavor.builtin.Beta1Flavor;
import com.minecrafttas.tasmod.savestates.storage.SavestateStorageExtensionBase;
import com.minecrafttas.tasmod.savestates.storage.SavestateStorageExtensionRegistry;
import net.minecraft.commands.CommandSourceStack;



public class TASmodAPIRegistry {
	/**
	 * <p>Registry for registering custom metadata that is stored in the TASfile.
	 * 
	 * <p>The default metadata includes general information such as author name,<br>
	 * savestate/rerecord count and category.
	 * 
	 * <p>Any custom class has to implement PlaybackMetadataExtension
	 */
	public static final PlaybackMetadataRegistry PLAYBACK_METADATA = new PlaybackMetadataRegistry();

	/**
	 * <p>Registry for registering custom behavior for each tick during recording and playback.
	 * 
	 * <p>File commands give the opportunity to run commands on each recorded tick and each played back tick.<br>
	 * File commands also have access to the TASfile so that data can be stored and read in/from the TASfile.
	 * 
	 */
	public static final PlaybackFileCommandsRegistry PLAYBACK_FILE_COMMAND = new PlaybackFileCommandsRegistry();

	/**
	 * <p>Registry for registering custom serialiser flavors that dictate the syntax of the inputs stored in the TASfile.
	 * 
	 * <p>Either create a new flavor by extending {@link SerialiserFlavorBase}<br>
	 * or extend an existing flavor (like {@link Beta1Flavor}) and overwrite parts of the methods.
	 * 
	 * <p>The resulting flavor can be registered here and can be found as a saving option with /saveTAS
	 */
	public static final SerialiserFlavorRegistry SERIALISER_FLAVOR = new SerialiserFlavorRegistry();

	/**
	 * <p>Registry for registering commands that are only executed on the client
	 * 
	 * <p>Create a new ClientCommand by extending {@link ClientCommandBase},<br>
	 * then create a command like normal, as it extends from the vanilla {@link CommandSourceStack}
	 */
	public static final ClientCommandRegistry CLIENT_COMMANDS = new ClientCommandRegistry();

	/**
	 * <p>Registry for registering additional data that should be stored or loaded during a Savestate or Loadstate respectively
	 * 
	 * <p>Create a new SavestateStorageExtension by extending {@link SavestateStorageExtensionBase}
	 */
	public static final SavestateStorageExtensionRegistry SAVESTATE_STORAGE = new SavestateStorageExtensionRegistry();
}

