package com.minecrafttas.tasmod.playback.filecommands.builtin;

import java.io.Serializable;
import java.nio.file.Path;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.List;
import java.util.Locale;

import com.dselent.bigarraylist.BigArrayList;
import com.minecrafttas.tasmod.TASmodClient;
import com.minecrafttas.tasmod.events.EventPlaybackClient;
import com.minecrafttas.tasmod.playback.PlaybackControllerClient.InputContainer;
import com.minecrafttas.tasmod.playback.PlaybackControllerClient.TASstate;
import com.minecrafttas.tasmod.playback.filecommands.PlaybackFileCommand;
import com.minecrafttas.tasmod.playback.filecommands.PlaybackFileCommand.PlaybackFileCommandExtension;
import com.minecrafttas.tasmod.playback.filecommands.PlaybackFileCommand.SortedFileCommandContainer;
import com.minecrafttas.tasmod.playback.tasfile.exception.PlaybackLoadException;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.ChatFormatting;

/**
 * Stores the players position during recording and compares it with the
 * position during playback
 * 
 * @author Scribble
 */
public class DesyncMonitorFileCommandExtension extends PlaybackFileCommandExtension implements EventPlaybackClient.EventControllerStateChange, EventPlaybackClient.EventInputDelete {

	/**
	 * List containing {@link MonitorContainer MonitorContainers} in a TASfile 
	 */
	private BigArrayList<MonitorContainer> monitorContainer;

	/**
	 * The {@link MonitorContainer} for the current tick
	 */
	private MonitorContainer currentValues;

	public DesyncMonitorFileCommandExtension() {
		this("monitoring");
	}

	public DesyncMonitorFileCommandExtension(String tempDirName) {
		super(tempDirName);
		this.monitorContainer = new BigArrayList<MonitorContainer>(tempDir.toString());
		// Is enabled by default
		enabled = true;
	}

	public DesyncMonitorFileCommandExtension(Path tempDir) {
		super(tempDir);
		this.monitorContainer = new BigArrayList<>(tempDir.toString());
		enabled = true;
	}

	@Override
	public String getExtensionName() {
		return "tasmod_desyncMonitor@v1";
	}

	@Override
	public String[] getFileCommandNames() {
		return new String[] { "desyncMonitor" };
	}

	@Override
	public void onControllerStateChange(TASstate newstate, TASstate oldstate) {
		if (newstate == TASstate.RECORDING && monitorContainer.isEmpty()) {
			recordNull(0);
		}
		currentValues = null;
	}

	@Override
	public void onRecord(long tick, InputContainer inputContainer) {
		LocalPlayer player = Minecraft.getInstance().player;
		MonitorContainer values = null;
		if (player != null) {
			values = new MonitorContainer(tick, player.getX(), player.getY(), player.getZ(), player.getDeltaMovement().x, player.getDeltaMovement().y, player.getDeltaMovement().z);
		} else {
			values = new MonitorContainer(tick);
		}

		if (monitorContainer.size() <= tick) {
			monitorContainer.add(values);
		} else {
			monitorContainer.set(tick, values);
		}
	}

	@Override
	public void onDisable() {
		this.onClear();
	}

	@Override
	public SortedFileCommandContainer onSerialiseEndlineComment(long currentTick, InputContainer inputContainer) {
		SortedFileCommandContainer out = new SortedFileCommandContainer();
		MonitorContainer monitoredValues = monitorContainer.get(currentTick);
		PlaybackFileCommand command = new PlaybackFileCommand("desyncMonitor", monitoredValues.toStringArray());

		out.add("desyncMonitor", command);

		return out;
	}

	@Override
	public void onDeserialiseEndlineComment(long tick, InputContainer container, SortedFileCommandContainer fileCommandContainer) {
		List<PlaybackFileCommand> commandsEndline = fileCommandContainer.get("desyncMonitor");
		if (commandsEndline == null || commandsEndline.isEmpty()) {
			recordNull(tick);
			return;
		}

		PlaybackFileCommand command = commandsEndline.get(0);
		this.monitorContainer.add(loadFromFile(tick, command.getArgs()));
	}

	public void recordNull(long tick) {
		if (monitorContainer.size() <= tick) {
			monitorContainer.add(new MonitorContainer(tick));
		} else {
			monitorContainer.set(tick, new MonitorContainer(tick));
		}
	}

	@Override
	public void onPlayback(long tick, InputContainer inputContainer) {
		currentValues = get(tick - 1);
	}

	private MonitorContainer loadFromFile(long tick, String[] args) throws PlaybackLoadException {

		if (args.length != 6)
			throw new PlaybackLoadException("Could not load desyncMonitor file command in tick %s. The amount of arguments doesn't match: %s", tick, args.length);

		double x = 0;
		double y = 0;
		double z = 0;
		double mx = 0;
		double my = 0;
		double mz = 0;
		try {
			x = parseDouble(args[0]);
			y = parseDouble(args[1]);
			z = parseDouble(args[2]);
			mx = parseDouble(args[3]);
			my = parseDouble(args[4]);
			mz = parseDouble(args[5]);
		} catch (ParseException e) {
			throw new PlaybackLoadException(e);
		}

		return new MonitorContainer(tick, x, y, z, mx, my, mz);
	}

	public MonitorContainer get(long l) {
		try {
			return monitorContainer.get(l);
		} catch (IndexOutOfBoundsException e) {
			return null;
		}
	}

	private String lastStatus = ChatFormatting.GRAY + "Empty";

	public String getStatus(LocalPlayer player) {
		if (!TASmodClient.controller.isNothingPlaying()) {
			if (currentValues != null) {
double[] playervalues = new double[6];
			playervalues[0] = player.getX();
			playervalues[1] = player.getY();
			playervalues[2] = player.getZ();
			playervalues[3] = player.getDeltaMovement().x;
			playervalues[4] = player.getDeltaMovement().y;
			playervalues[5] = player.getDeltaMovement().z;
				DesyncStatus status = currentValues.getSeverity(playervalues);
				lastStatus = status.getFormat() + status.getText();
			} else {
				lastStatus = ChatFormatting.GRAY + "Empty";
			}
		}
		return lastStatus;
	}

	private String lastPos = "";

	public String getPos() {
		if (currentValues != null && TASmodClient.controller.isPlayingback()) {
			LocalPlayer player = Minecraft.getInstance().player;
			String[] values = new String[3];
			values[0] = getFormattedString(player.getX() - currentValues.values[0]);
			values[1] = getFormattedString(player.getY() - currentValues.values[1]);
			values[2] = getFormattedString(player.getZ() - currentValues.values[2]);

			lastPos = String.join(" ", values);
		}
		return lastPos;
	}

	private String lastMotion = "";

	public String getMotion() {
		if (currentValues != null && TASmodClient.controller.isPlayingback()) {
			LocalPlayer player = Minecraft.getInstance().player;
			String[] values = new String[3];
			values[0] = getFormattedString(player.getDeltaMovement().x - currentValues.values[3]);
			values[1] = getFormattedString(player.getDeltaMovement().y - currentValues.values[4]);
			values[2] = getFormattedString(player.getDeltaMovement().z - currentValues.values[5]);

			lastMotion = String.join(" ", values);
		}
		return lastMotion;
	}

	private String getFormattedString(double delta) {
		String out = "";
		if (delta != 0D) {
			DesyncStatus status = DesyncStatus.fromDelta(delta);
			if (status == DesyncStatus.EQUAL) {
				return "";
			}
			out = status.getFormat() + Double.toString(delta);
		}
		return out;
	}

	/**
	 * Storage class containing the position and velocity of a player at a given tick.
	 * 
	 * @author Scribble
	 */
	public class MonitorContainer implements Serializable {
		private static final long serialVersionUID = -3138791930493647885L;

		long index;

		double[] values = new double[6];

		public MonitorContainer(long index, double posx, double posy, double posz, double velx, double vely, double velz) {
			this.index = index;
			this.values[0] = posx;
			this.values[1] = posy;
			this.values[2] = posz;
			this.values[3] = velx;
			this.values[4] = vely;
			this.values[5] = velz;
		}

		public MonitorContainer(long index) {
			this(index, 0, 0, 0, 0, 0, 0);
		}

		public String[] toStringArray() {
			String[] out = new String[values.length];
			for (int i = 0; i < values.length; i++) {
				out[i] = String.format(Locale.ENGLISH, "%s", values[i]);
			}
			return out;
		}

		@Override
		public String toString() {
			return String.format(Locale.ENGLISH, "%d, %d, %d, %d, %d, %d", values[0], values[1], values[2], values[3], values[4], values[5]);
		}

		/**
		 * Compares the values in this {@link MonitorContainer} with other values
		 * @param playerValues The values to compare to
		 * @return The {@link DesyncStatus}, how severe the playback is currently drifting apart
		 */
		public DesyncStatus getSeverity(double[] playerValues) {

			DesyncStatus out = null;

			for (int i = 0; i < values.length; i++) {
				double delta = 0;
				try {
					delta = playerValues[i] - values[i];
				} catch (Exception e) {
					return DesyncStatus.ERROR;
				}
				DesyncStatus status = DesyncStatus.fromDelta(delta);
				if (out == null || status.getSeverity() > out.getSeverity()) {
					out = status;
				}
			}

			return out;
		}
	}

	public enum DesyncStatus {
		EQUAL(0, ChatFormatting.GREEN, "In sync", 0D),
		WARNING(1, ChatFormatting.YELLOW, "Slight desync", 0.00001D),
		MODERATE(2, ChatFormatting.RED, "Moderate desync", 0.01D),
		TOTAL(3, ChatFormatting.DARK_RED, "Total desync"),
		ERROR(3, ChatFormatting.DARK_PURPLE, "ERROR");

		private Double tolerance;
		private int severity;
		private String text;
		private ChatFormatting format;

		private DesyncStatus(int severity, ChatFormatting color, String text) {
			this.severity = severity;
			this.format = color;
			this.text = text;
			tolerance = null;
		}

		private DesyncStatus(int severity, ChatFormatting color, String text, double tolerance) {
			this(severity, color, text);
			this.tolerance = tolerance;
		}

		public static DesyncStatus fromDelta(double delta) {
			DesyncStatus out = TOTAL;
			for (DesyncStatus status : values()) {
				if (status.tolerance == null) {
					return status;
				}
				if (Math.abs(delta) < status.tolerance) {
					break;
				}
				if (Math.abs(delta) >= status.tolerance) {
					out = status;
				}
			}
			return out;
		}

		public ChatFormatting getFormat() {
			return format;
		}

		public int getSeverity() {
			return severity;
		}

		public String getText() {
			return text;
		}
	}

	private double parseDouble(String doublestring) throws ParseException {
		NumberFormat format = NumberFormat.getInstance(Locale.ENGLISH);
		Number number = format.parse(doublestring);
		return number.doubleValue();
	}

	@Override
	public void onClear() {
		currentValues = null;
		monitorContainer.clear();
		lastStatus = ChatFormatting.GRAY + "Empty";
		lastPos = "";
		lastMotion = "";
	}

	@Override
	public void onInputDelete(long index) {
		monitorContainer.remove(index);
	}
}

