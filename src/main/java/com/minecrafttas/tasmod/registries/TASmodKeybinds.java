package com.minecrafttas.tasmod.registries;

import org.lwjgl.glfw.GLFW;

import com.minecrafttas.tasmod.mctcommon.KeybindManager.IsKeyDownFunc;
import com.minecrafttas.tasmod.mctcommon.KeybindManager.Keybind;
import com.minecrafttas.tasmod.mctcommon.KeybindManager.KeybindID;
import com.minecrafttas.tasmod.TASmodClient;
import com.minecrafttas.tasmod.networking.TASmodBufferBuilder;
import com.minecrafttas.tasmod.playback.PlaybackControllerClient.TASstate;
import com.minecrafttas.tasmod.virtual.VirtualKeybindings;

import net.minecraft.client.Minecraft;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.Screen;

public enum TASmodKeybinds implements KeybindID {
	TICKRATE_0("Tickrate 0 Key", "TASmod", GLFW.GLFW_KEY_F8, () -> TASmodClient.tickratechanger.togglePause(), VirtualKeybindings::isKeyDown),
	TICKRATE_ADVANCE("Advance Tick", "TASmod", GLFW.GLFW_KEY_F9, () -> TASmodClient.tickratechanger.advanceTick(), VirtualKeybindings::isKeyDown),
	TICKRATE_INCREASE("Increase Tickrate", "TASmod", GLFW.GLFW_KEY_PERIOD, () -> TASmodClient.tickratechanger.increaseTickrate(), VirtualKeybindings::isKeyDownExceptTextfield),
	TICKRATE_DECREASE("Decrease Tickrate", "TASmod", GLFW.GLFW_KEY_COMMA, () -> TASmodClient.tickratechanger.decreaseTickrate(), VirtualKeybindings::isKeyDownExceptTextfield),
	PLAYBACK_STOP("Recording/Playback Stop", "TASmod", GLFW.GLFW_KEY_F10, () -> TASmodClient.controller.setTASState(TASstate.NONE), VirtualKeybindings::isKeyDown),
	SAVESTATE_SAVE("Create Savestate", "TASmod", GLFW.GLFW_KEY_J, () -> {
		try {
			TASmodClient.client.send(new TASmodBufferBuilder(TASmodPackets.SAVESTATE_SAVE).writeInt(-1));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}),
	SAVESTATE_LOAD("Load Latest Savestate", "TASmod", GLFW.GLFW_KEY_K, () -> {
		try {
			TASmodClient.client.send(new TASmodBufferBuilder(TASmodPackets.SAVESTATE_LOAD).writeInt(-1));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}),
	INFO_GUI("Open InfoGui Editor", "TASmod", GLFW.GLFW_KEY_F6, () -> {
		// Screen handling API changed in 26.3
		// Minecraft mc = Minecraft.getInstance();
		// if (mc.screen == null) {
		// 	mc.setScreen(TASmodClient.hud);
		// }
	}),
	TURN_LEFT("Rotate 45 degrees left", "TASmod", GLFW.GLFW_KEY_UNKNOWN, () -> {
		TASmodClient.virtual.CAMERA_ANGLE.updateNextCameraAngle(0, -45);
	}),
	TURN_RIGHT("Rotate 45 degrees right", "TASmod", GLFW.GLFW_KEY_UNKNOWN, () -> {
		TASmodClient.virtual.CAMERA_ANGLE.updateNextCameraAngle(0, 45);
	}),
	TEST1("Various Testing", "TASmod", GLFW.GLFW_KEY_F12, () -> {
		// Minecraft.getInstance().setScreen(null);
	}, VirtualKeybindings::isKeyDown),
	TEST2("Various Testing2", "TASmod", GLFW.GLFW_KEY_F7, () -> {
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

	public static KeyMapping[] valuesVanillaKeybind() {
		TASmodKeybinds[] tasmodkeybinds = values();
		KeyMapping[] keybinds = new KeyMapping[tasmodkeybinds.length];
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

