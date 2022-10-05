package de.scribble.lp.tasmod.events;

import org.lwjgl.input.Keyboard;

import de.scribble.lp.tasmod.ClientProxy;
import de.scribble.lp.tasmod.CommonProxy;
import de.scribble.lp.tasmod.externalGui.InputContainerView;
import de.scribble.lp.tasmod.inputcontainer.TASstate;
import de.scribble.lp.tasmod.savestates.server.LoadstatePacket;
import de.scribble.lp.tasmod.savestates.server.SavestatePacket;
import de.scribble.lp.tasmod.tickratechanger.TickrateChangerClient;
import de.scribble.lp.tasmod.virtual.VirtualKeybindings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;

/**
 * 
 * The class where all keybinding functionality is executed
 * @author Scribble
 *
 */
public class KeybindingEvents {

	public static KeyBinding tickratezeroKey = new KeyBinding("Tickrate 0 Key", Keyboard.KEY_F8, "TASmod");

	public static KeyBinding tickAdvance = new KeyBinding("Advance Tick", Keyboard.KEY_F9, "TASmod");

	public static KeyBinding stopkey = new KeyBinding("Recording/Playback Stop", Keyboard.KEY_F10, "TASmod");

	public static KeyBinding savestateSaveKey = new KeyBinding("Create Savestate", Keyboard.KEY_J, "TASmod");

	public static KeyBinding savestateLoadKey = new KeyBinding("Load Latest Savestate", Keyboard.KEY_K, "TASmod");

	public static KeyBinding testingKey = new KeyBinding("Various Testing", Keyboard.KEY_F12, "TASmod");

	public static KeyBinding infoGuiKey = new KeyBinding("Open InfoGui Editor", Keyboard.KEY_F6, "TASmod");
	
	public static KeyBinding bufferViewKey = new KeyBinding("Buffer View", Keyboard.KEY_NUMPAD0, "TASmod");
	
	public static KeyBinding ktrngKey = null;
	
	public static void fireKeybindingsEvent() {
		
		if (VirtualKeybindings.isKeyDownExceptTextfield(savestateSaveKey)) {

			CommonProxy.NETWORK.sendToServer(new SavestatePacket());

		} else if (VirtualKeybindings.isKeyDownExceptTextfield(savestateLoadKey)) {

			CommonProxy.NETWORK.sendToServer(new LoadstatePacket());

		} else if (VirtualKeybindings.isKeyDownExceptTextfield(bufferViewKey)) {
			
			InputContainerView.startBufferView();
			
		} else if (VirtualKeybindings.isKeyDownExceptTextfield(infoGuiKey)) {
			
			Minecraft.getMinecraft().displayGuiScreen(ClientProxy.hud);
			
		} else if (VirtualKeybindings.isKeyDown(stopkey)) {
			
			TASstate.setOrSend(TASstate.NONE);
			
		} else if (VirtualKeybindings.isKeyDown(tickratezeroKey)) {
			
			TickrateChangerClient.togglePause();
			
		} else if (VirtualKeybindings.isKeyDown(tickAdvance)) {
			
			TickrateChangerClient.advanceTick();
			
		} else if (VirtualKeybindings.isKeyDown(testingKey)) {
			
			CommonProxy.tickSchedulerServer.add(() -> {
				try {
					Thread.sleep(1000L);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			});
			
		} else if (ktrngKey!=null && VirtualKeybindings.isKeyDown(ktrngKey)) {
			
		}
	}
}
