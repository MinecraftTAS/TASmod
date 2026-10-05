package com.minecrafttas.tasmod.registries;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import org.lwjgl.input.Keyboard;

import com.minecrafttas.mctcommon.KeybindManager.IsKeyDownFunc;
import com.minecrafttas.mctcommon.KeybindManager.Keybind;
import com.minecrafttas.mctcommon.KeybindManager.KeybindID;
import com.minecrafttas.tasmod.TASmod;
import com.minecrafttas.tasmod.TASmodClient;
import com.minecrafttas.tasmod.networking.TASmodBufferBuilder;
import com.minecrafttas.tasmod.playback.PlaybackControllerClient.TASstate;
import com.minecrafttas.tasmod.virtual.VirtualKeybindings;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;

public enum TASmodKeybinds implements KeybindID {
	TICKRATE_0("Tickrate 0 Key", "TASmod", Keyboard.KEY_F8, () -> TASmodClient.tickratechanger.togglePause(), VirtualKeybindings::isKeyDown),
	TICKRATE_ADVANCE("Advance Tick", "TASmod", Keyboard.KEY_F9, () -> TASmodClient.tickratechanger.advanceTick(), VirtualKeybindings::isKeyDown),
	TICKRATE_INCREASE("Increase Tickrate", "TASmod", Keyboard.KEY_PERIOD, () -> TASmodClient.tickratechanger.increaseTickrate(), VirtualKeybindings::isKeyDownExceptTextfield),
	TICKRATE_DECREASE("Decrease Tickrate", "TASmod", Keyboard.KEY_COMMA, () -> TASmodClient.tickratechanger.decreaseTickrate(), VirtualKeybindings::isKeyDownExceptTextfield),
	PLAYBACK_STOP("Recording/Playback Stop", "TASmod", Keyboard.KEY_F10, () -> TASmodClient.controller.setTASState(TASstate.NONE), VirtualKeybindings::isKeyDown),
	SAVESTATE_SAVE("Create Savestate", "TASmod", Keyboard.KEY_J, () -> {
		try {
			TASmodClient.client.send(new TASmodBufferBuilder(TASmodPackets.SAVESTATE_SAVE).writeInt(-1));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}),
	SAVESTATE_LOAD("Load Latest Savestate", "TASmod", Keyboard.KEY_K, () -> {
		try {
			TASmodClient.client.send(new TASmodBufferBuilder(TASmodPackets.SAVESTATE_LOAD).writeInt(-1));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}),
	INFO_GUI("Open InfoGui Editor", "TASmod", Keyboard.KEY_F6, () -> {
		Minecraft mc = Minecraft.getMinecraft();
		if (mc.currentScreen == null) {
			mc.displayGuiScreen(TASmodClient.hud);
		}
	}),
	TURN_LEFT("Rotate 45 degrees left", "TASmod", Keyboard.KEY_NONE, () -> {
		TASmodClient.virtual.CAMERA_ANGLE.updateNextCameraAngle(0, -45);
	}),
	TURN_RIGHT("Rotate 45 degrees right", "TASmod", Keyboard.KEY_NONE, () -> {
		TASmodClient.virtual.CAMERA_ANGLE.updateNextCameraAngle(0, 45);
	}),
	TEST1("Various Testing", "TASmod", Keyboard.KEY_F12, () -> {
		/*
		 * Yes, I am really generating TypeAdapter code here and replacing the class file.
		 * This is how we did it before AI kids.
		 */

		try {
			Files.write(Paths.get("../src/main/java/com/minecrafttas/tasmod/savestates/typeadapters/EntityAITypeAdapters2.java"), TASmod.generator.generateMulti(), StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}, VirtualKeybindings::isKeyDown),
	TEST2("Various Testing2", "TASmod", Keyboard.KEY_F7, () -> {
	}, VirtualKeybindings::isKeyDown);

	private Keybind keybind;

	private TASmodKeybinds(String name, String category, int defaultKey, Runnable onKeyDown, IsKeyDownFunc func) {
		this.keybind = new Keybind(name, category, defaultKey, onKeyDown, func);
	}

	private TASmodKeybinds(String name, String category, int defaultKey, Runnable onKeyDown) {
		this(name, category, defaultKey, onKeyDown, null);
	}

	public static Keybind[] valuesKeybind() {
		TASmodKeybinds[] tasmodkeybinds = values();
		Keybind[] keybinds = new Keybind[tasmodkeybinds.length];
		for (int i = 0; i < tasmodkeybinds.length; i++) {
			keybinds[i] = tasmodkeybinds[i].keybind;
		}
		return keybinds;
	}

	public static KeyBinding[] valuesVanillaKeybind() {
		TASmodKeybinds[] tasmodkeybinds = values();
		KeyBinding[] keybinds = new KeyBinding[tasmodkeybinds.length];
		for (int i = 0; i < tasmodkeybinds.length; i++) {
			keybinds[i] = tasmodkeybinds[i].keybind.vanillaKeyBinding;
		}
		return keybinds;
	}

	@Override
	public Keybind getKeybind() {
		return this.keybind;
	}
}
