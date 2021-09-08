package de.scribble.lp.tasmod.savestates.server;

import java.awt.EventQueue;

import javax.swing.WindowConstants;

import de.scribble.lp.tasmod.ClientProxy;
import de.scribble.lp.tasmod.CommonProxy;
import de.scribble.lp.tasmod.monitoring.BufferView;
import de.scribble.lp.tasmod.playback.PlaybackPacket;
import de.scribble.lp.tasmod.virtual.VirtualKeybindings;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class SavestateEvents {
	public static boolean lagServer;

	@SubscribeEvent
	public void onRender(TickEvent.RenderTickEvent ev) {
		if (VirtualKeybindings.isKeyDownExceptTextfield(ClientProxy.savestateSaveKey)) {

			CommonProxy.NETWORK.sendToServer(new SavestatePacket());

		} else if (VirtualKeybindings.isKeyDownExceptTextfield(ClientProxy.savestateLoadKey)) {

			CommonProxy.NETWORK.sendToServer(new LoadstatePacket());

		} else if (VirtualKeybindings.isKeyDownExceptTextfield(ClientProxy.testingKey)) {
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
		} else if (VirtualKeybindings.isKeyDownExceptTextfield(ClientProxy.infoGuiKey)) { // Sorry..
			Minecraft.getMinecraft().displayGuiScreen(ClientProxy.hud);
		} else if (VirtualKeybindings.isKeyDown(ClientProxy.stopkey)) {
			CommonProxy.NETWORK.sendToServer(new PlaybackPacket(false));
		}
	}
}
