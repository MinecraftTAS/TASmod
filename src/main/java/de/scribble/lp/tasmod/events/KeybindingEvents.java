package de.scribble.lp.tasmod.events;

import java.awt.EventQueue;

import de.scribble.lp.tasmod.ClientProxy;
import de.scribble.lp.tasmod.CommonProxy;
import de.scribble.lp.tasmod.commands.playback.PlaybackPacket;
import de.scribble.lp.tasmod.monitoring.BufferView;
import de.scribble.lp.tasmod.savestates.server.LoadstatePacket;
import de.scribble.lp.tasmod.savestates.server.SavestatePacket;
import de.scribble.lp.tasmod.virtual.VirtualKeybindings;
import net.minecraft.client.Minecraft;

public class KeybindingEvents {

	public static void fireKeybindingsEvent() {
		//TODO Move each of these in seperate classes
		VirtualKeybindings.increaseCooldowntimer();
		if (VirtualKeybindings.isKeyDownExceptTextfield(ClientProxy.savestateSaveKey)) {

			CommonProxy.NETWORK.sendToServer(new SavestatePacket());

		} else if (VirtualKeybindings.isKeyDownExceptTextfield(ClientProxy.savestateLoadKey)) {

			CommonProxy.NETWORK.sendToServer(new LoadstatePacket());

		} else if (VirtualKeybindings.isKeyDownExceptTextfield(ClientProxy.testingKey)) {
			//TODO Move this to BufferView
			EventQueue.invokeLater(new Runnable() {

				@Override
				public void run() {
					try {
						BufferView frame = new BufferView();
						frame.setVisible(true);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
		} else if (VirtualKeybindings.isKeyDownExceptTextfield(ClientProxy.infoGuiKey)) {
			Minecraft.getMinecraft().displayGuiScreen(ClientProxy.hud);
		} else if (VirtualKeybindings.isKeyDown(ClientProxy.stopkey)) {
			CommonProxy.NETWORK.sendToServer(new PlaybackPacket(false));
		}
	}
}
