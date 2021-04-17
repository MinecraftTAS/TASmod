package de.scribble.lp.tasmod.virtual.container;

import java.io.File;

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

	private BigArrayList<TickInputContainer> inputs = new BigArrayList(Minecraft.getMinecraft().mcDataDir.getAbsolutePath() + File.separator + "saves" + File.separator + "tasfiles"+ File.separator +"temp");

	public boolean isPlayingback() {
		return playback;
	}

	public boolean isRecording() {
		return recording;
	}

	public VirtualKeyboard addKeyboardToContainer(VirtualKeyboard keyboard) {
		this.keyboard = keyboard.clone();
		return keyboard;
	}

	public VirtualMouse addMouseToContainer(VirtualMouse mouse) {
		this.mouse = mouse.clone();
		return mouse;
	}

	public VirtualSubticks addSubticksToContainer(VirtualSubticks subticks) {
		this.subticks = subticks.clone();
		return subticks;
	}

	public void nextTick() {
		index++;
		inputs.add(new TickInputContainer(index, keyboard.clone(), mouse.clone(), subticks.clone()));
	}

	public long size() {
		return inputs.size();
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
