package de.scribble.lp.tasmod.virtual.container;

import java.io.File;

import com.dselent.bigarraylist.BigArrayList;

import de.scribble.lp.tasmod.virtual.VirtualKeyboard;
import de.scribble.lp.tasmod.virtual.VirtualMouse;
import de.scribble.lp.tasmod.virtual.VirtualSubticks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.util.text.TextFormatting;

public class InputContainer {
	private boolean playback = false;

	private boolean recording = false;

	private int index;

	private VirtualKeyboard keyboard = new VirtualKeyboard();

	private VirtualMouse mouse = new VirtualMouse();

	private VirtualSubticks subticks = new VirtualSubticks();

	public final File directory = new File(Minecraft.getMinecraft().mcDataDir.getAbsolutePath() + File.separator + "saves" + File.separator + "tasfiles");

	private BigArrayList<TickInputContainer> inputs = new BigArrayList(directory + File.separator + "temp");

	// =====================================================================================================

	private String authors = "Insert author here";

	private String title = "Insert TAS category here";

	private int rerecords = 0;

	private String playtime = "00:00.0";

	private String startLocation = "";

	// =====================================================================================================

	public boolean isPlayingback() {
		return playback;
	}

	public boolean isRecording() {
		return recording;
	}

	public String setRecording(boolean enabled) {
		if (playback)
			return TextFormatting.RED + "A playback is already running";
		recording = enabled;
		if (recording) {
			if (Minecraft.getMinecraft().player != null) {
				startLocation = getStartLocation(Minecraft.getMinecraft().player);
			}
			return TextFormatting.GREEN + "Starting the recording";
		} else {
			return TextFormatting.GREEN + "Stopping the recording";
		}
	}

	public String setPlayback(boolean enabled) {
		if (recording)
			return TextFormatting.RED + "A recording is already running";
		playback = enabled;
		if (playback) {
			index = 0;
			if (Minecraft.getMinecraft().player != null && !startLocation.isEmpty()) {
				tpPlayer(startLocation);
			}
			return TextFormatting.GREEN + "Starting playback";
		} else {
			return TextFormatting.GREEN + "Aborting playback";
		}
	}

	public VirtualKeyboard addKeyboardToContainer(VirtualKeyboard keyboard) {
		if (recording) {
			this.keyboard = keyboard.clone();
		} else if (playback) {
			keyboard = this.keyboard.clone();
		}
		return keyboard;
	}

	public VirtualMouse addMouseToContainer(VirtualMouse mouse) {
		if (recording) {
			this.mouse = mouse.clone();
		} else if (playback) {
			mouse = this.mouse.clone();
		}
		return mouse;
	}

	public VirtualSubticks addSubticksToContainer(VirtualSubticks subticks) {
		if (recording) {
			this.subticks = subticks.clone();
		} else if (playback) {
			subticks = this.subticks.clone();
		}
		return subticks;
	}

	public void nextTick() {
		if (recording) {
			index++;
			inputs.add(new TickInputContainer(index, keyboard.clone(), mouse.clone(), subticks.clone()));
		} else if (playback) {
			index++;
			if (index == inputs.size()) {
				this.keyboard = new VirtualKeyboard();
				this.mouse = new VirtualMouse();
			} else if (index > inputs.size()) {
				playback = false;
				index--;
			} else {
				TickInputContainer tickcontainer = inputs.get(index);
				this.keyboard = tickcontainer.getKeyboard();
				this.mouse = tickcontainer.getMouse();
				this.subticks = tickcontainer.getSubticks();
			}
		}
	}

	public int size() {
		return (int) inputs.size();
	}

	public boolean isEmpty() {
		return inputs.isEmpty();
	}

	public int index() {
		return index;
	}

	public BigArrayList<TickInputContainer> getInputs() {
		return inputs;
	}

	public void setIndex(int index) {
		this.index = index;
		if (playback) {
			TickInputContainer tickcontainer = inputs.get(index);
			this.keyboard = tickcontainer.getKeyboard();
			this.mouse = tickcontainer.getMouse();
			this.subticks = tickcontainer.getSubticks();
		}
	}

	public TickInputContainer get(int index) {
		TickInputContainer tickcontainer = null;
		try {
			tickcontainer = inputs.get(index);
		} catch (IndexOutOfBoundsException e) {
			return null;
		}
		return tickcontainer;
	}

	@Override
	public String toString() {
		if (inputs.isEmpty()) {
			return "null";
		}
		String out = "";
		for (int i = 0; i < inputs.size(); i++) {
			out = out.concat(inputs.get(i).toString() + "\n");
		}
		return out;
	}

	public void clear() {
		inputs = new BigArrayList<TickInputContainer>(directory + File.separator + "temp");
		index = 0;
	}

	public String getAuthors() {
		return authors;
	}

	public void setAuthors(String authors) {
		this.authors = authors;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public int getRerecords() {
		return rerecords;
	}

	public void setRerecords(int rerecords) {
		this.rerecords = rerecords;
	}

	public String getPlaytime() {
		return playtime;
	}

	public void setPlaytime(String playtime) {
		this.playtime = playtime;
	}

	public void setSavestates(String playtime) {
		this.playtime = playtime;
	}

	public void fixTicks() {
		for (int i = 0; i < inputs.size(); i++) {
			inputs.get(i).setTick(i + 1);
		}
	}

	public String getStartLocation() {
		return startLocation;
	}

	public void setStartLocation(String startLocation) {
		this.startLocation = startLocation;
	}

	private String getStartLocation(EntityPlayerSP player) { // TODO Can easily break...
		String pos = player.getPositionVector().toString();
		pos = pos.replace("(", "");
		pos = pos.replace(")", "");
		pos = pos.replace(" ", "");
		String pitch = Float.toString(player.rotationPitch);
		String yaw = Float.toString(player.rotationYaw);
		return pos + "," + yaw + "," + pitch;
	}

	private void tpPlayer(String startLocation) {
		String[] section = startLocation.split(",");
		Minecraft.getMinecraft().player.sendChatMessage("/tp " + section[0] + " " + section[1] + " " + section[2] + " " + section[3] + " " + section[4]); // I don't care anymore

	}
}
