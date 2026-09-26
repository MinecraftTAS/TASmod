package com.minecrafttas.tasmod.playback.metadata.builtin;

import static com.minecrafttas.tasmod.TASmod.LOGGER;

import com.minecrafttas.tasmod.events.EventPlaybackClient.EventControllerStateChange;
import com.minecrafttas.tasmod.events.EventPlaybackClient.EventPlaybackJoinedWorld;
import com.minecrafttas.tasmod.playback.PlaybackControllerClient.TASstate;
import com.minecrafttas.tasmod.playback.metadata.PlaybackMetadata;
import com.minecrafttas.tasmod.playback.metadata.PlaybackMetadata.PlaybackMetadataExtension;
import com.minecrafttas.tasmod.playback.tasfile.exception.PlaybackLoadException;
import com.minecrafttas.tasmod.util.LoggerMarkers;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

/**
 * Adds credits to the playback metadata<br>
 * <br>
 * Credits can be changed in the file and will be printed in chat, when the
 * player joins a world after /fullplay
 */
public class CreditsMetadataExtension extends PlaybackMetadataExtension implements EventPlaybackJoinedWorld, EventControllerStateChange {

	/**
	 * The title/category of the TAS (e.g. KillSquid - Any% Glitched)
	 */
	protected String title = "Insert TAS category here";
	/**
	 * The author(s) of the TAS (e.g. Scribble, Pancake)
	 */
	protected String authors = "Insert author here";
	/**
	 * How long the TAS is going to take (e.g. 00:01.0 or 20ticks)
	 */
	protected String playtime = "00:00.0";
	/**
	 * How often a savestate was loaded as a measurement of effort (e.g. 200)
	 */
	protected int rerecords = 0;

	/**
	 * If the credits where already printed in this instance
	 */
	protected boolean creditsPrinted = false;

	@Override
	public String getExtensionName() {
		return "Credits";
	}

	@Override
	public void onCreate() {
		// Unused atm
	}

	public enum CreditFields {
		Title("Title"),
		Author("Author"),
		PlayTime("Playing Time"),
		Rerecords("Rerecords");

		private final String name;

		private CreditFields(String name) {
			this.name = name;
		}

		@Override
		public String toString() {
			return name;
		}
	}

	@Override
	public PlaybackMetadata onStore() {
		PlaybackMetadata metadata = new PlaybackMetadata(this);
		metadata.setValue(CreditFields.Title, title);
		metadata.setValue(CreditFields.Author, authors);
		metadata.setValue(CreditFields.PlayTime, playtime);
		metadata.setValue(CreditFields.Rerecords, Integer.toString(rerecords));
		return metadata;
	}

	@Override
	public void onLoad(PlaybackMetadata metadata) {
		title = getOrDefault(metadata.getValue(CreditFields.Title), title);
		authors = getOrDefault(metadata.getValue(CreditFields.Author), authors);
		playtime = getOrDefault(metadata.getValue(CreditFields.PlayTime), playtime);
		try {
			rerecords = Integer.parseInt(getOrDefault(metadata.getValue(CreditFields.Rerecords), Integer.toString(rerecords)));
		} catch (NumberFormatException e) {
			rerecords = 0;
			throw new PlaybackLoadException(e);
		}
	}

	protected String getOrDefault(String value, String defaultVal) {
		return value != null ? value : defaultVal;
	}

	@Override
	public void onClear() {
		title = "Insert TAS category here";
		authors = "Insert author here";
		playtime = "00:00.0";
		rerecords = 0;
		creditsPrinted = false;
	}

	@Override
	public void onPlaybackJoinedWorld(TASstate state) {
		LOGGER.trace(LoggerMarkers.Playback, "Printing credits");
		if (state == TASstate.PLAYBACK && !creditsPrinted) {
			creditsPrinted = true;
			printMessage(title, ChatFormatting.GOLD);
			printMessage("", null);
			printMessage("by " + authors, ChatFormatting.AQUA);
			printMessage("", null);
			printMessage("in " + playtime, null);
			printMessage("", null);
			printMessage("Rerecords: " + rerecords, null);
		}
	}

	protected void printMessage(String msg, ChatFormatting format) {
		String formatString = "";
		if (format != null)
			formatString = format.toString();

		// Chat API changed in 26.3
		// Minecraft.getInstance().gui.getChat().addMessage(Component.literal(formatString + msg));
	}

	@Override
	public void onControllerStateChange(TASstate newstate, TASstate oldstate) {
		if (newstate == TASstate.PLAYBACK) { // Reset creditsPrinted when a new playback is started
			creditsPrinted = false;
		}
	}
}

