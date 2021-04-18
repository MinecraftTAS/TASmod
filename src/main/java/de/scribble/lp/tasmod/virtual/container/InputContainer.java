package de.scribble.lp.tasmod.virtual.container;

import java.io.File;
import java.io.IOException;

import com.dselent.bigarraylist.BigArrayList;

import de.scribble.lp.tasmod.virtual.VirtualKeyboard;
import de.scribble.lp.tasmod.virtual.VirtualMouse;
import de.scribble.lp.tasmod.virtual.VirtualSubticks;
import net.minecraft.client.Minecraft;

public class InputContainer {
	private boolean playback = false;

	private boolean recording = false;

	private int index;

	private VirtualKeyboard keyboard = new VirtualKeyboard();

	private VirtualMouse mouse = new VirtualMouse();

	private VirtualSubticks subticks = new VirtualSubticks();

	public final File directory = new File(Minecraft.getMinecraft().mcDataDir.getAbsolutePath() + File.separator + "saves" + File.separator + "tasfiles");

	private BigArrayList<TickInputContainer> inputs = new BigArrayList(directory + File.separator + "temp");

	public boolean isPlayingback() {
		return playback;
	}

	public boolean isRecording() {
		return recording;
	}

	public void toggleRecording() {
		if (playback)
			return;
		recording = !recording;
		if (recording) {
			inputs = new BigArrayList(directory + File.separator + "temp");
			index = 0;
		}
	}

	public void togglePlayback() {
		if (recording)
			return;
		playback = !playback;
		if (playback) {
			index = 0;
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
			} else {
				TickInputContainer tickcontainer = inputs.get(index);
				this.keyboard = tickcontainer.getKeyboard();
				this.mouse = tickcontainer.getMouse();
				this.subticks = tickcontainer.getSubticks();
			}
		}
	}

	public long size() {
		return inputs.size();
	}

	public int index() {
		return index;
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
}
