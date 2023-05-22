package com.minecrafttas.tasmod.util;

import org.lwjgl.input.Keyboard;

import com.minecrafttas.common.events.client.EventClientGameLoop;
import com.minecrafttas.tasmod.TASmod;
import com.minecrafttas.tasmod.TASmodClient;
import com.minecrafttas.tasmod.externalGui.InputContainerView;
import com.minecrafttas.tasmod.playback.PlaybackController.TASstate;
import com.minecrafttas.tasmod.playback.server.TASstateClient;
import com.minecrafttas.tasmod.savestates.server.LoadstatePacket;
import com.minecrafttas.tasmod.savestates.server.SavestatePacket;
import com.minecrafttas.tasmod.virtual.VirtualKeybindings;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;

/**
 * 
 * The class where all keybinding functionality is executed
 * @author Scribble
 *
 */
public class KeybindingHandler implements EventClientGameLoop {

	public KeyBinding tickratezeroKey = new KeyBinding("Tickrate 0 Key", Keyboard.KEY_F8, "TASmod");

	public KeyBinding tickAdvance = new KeyBinding("Advance Tick", Keyboard.KEY_F9, "TASmod");

	public KeyBinding stopkey = new KeyBinding("Recording/Playback Stop", Keyboard.KEY_F10, "TASmod");

	public KeyBinding savestateSaveKey = new KeyBinding("Create Savestate", Keyboard.KEY_J, "TASmod");

	public KeyBinding savestateLoadKey = new KeyBinding("Load Latest Savestate", Keyboard.KEY_K, "TASmod");

	public KeyBinding testingKey = new KeyBinding("Various Testing", Keyboard.KEY_F12, "TASmod");

	public KeyBinding infoGuiKey = new KeyBinding("Open InfoGui Editor", Keyboard.KEY_F6, "TASmod");
	
	public KeyBinding bufferViewKey = new KeyBinding("Buffer View", Keyboard.KEY_NUMPAD0, "TASmod");
	
	@Override
	public void onRunClientGameLoop(Minecraft mc) {
		
		if (VirtualKeybindings.isKeyDownExceptTextfield(savestateSaveKey)) {

			TASmodClient.packetClient.sendToServer(new SavestatePacket());

		} else if (VirtualKeybindings.isKeyDownExceptTextfield(savestateLoadKey)) {

			TASmodClient.packetClient.sendToServer(new LoadstatePacket());

		} else if (VirtualKeybindings.isKeyDownExceptTextfield(bufferViewKey)) {
			
			InputContainerView.startBufferView();
			
		} else if (VirtualKeybindings.isKeyDownExceptTextfield(infoGuiKey)) {
			
			Minecraft.getMinecraft().displayGuiScreen(TASmodClient.hud);
			
		} else if (VirtualKeybindings.isKeyDown(stopkey)) {
			
			TASstateClient.setOrSend(TASstate.NONE);
			
		} else if (VirtualKeybindings.isKeyDown(tickratezeroKey)) {
			
			TASmodClient.tickratechanger.togglePause();
			
		} else if (VirtualKeybindings.isKeyDown(tickAdvance)) {
			
			TASmodClient.tickratechanger.advanceTick();
			
		} else if (VirtualKeybindings.isKeyDown(testingKey)) {
			
			TASmod.tickSchedulerServer.add(() -> {
				try {
					Thread.sleep(1000L);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			});
			
		}
	}

}
