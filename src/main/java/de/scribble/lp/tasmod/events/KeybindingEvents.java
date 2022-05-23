package de.scribble.lp.tasmod.events;

import de.scribble.lp.tasmod.ClientProxy;
import de.scribble.lp.tasmod.CommonProxy;
import de.scribble.lp.tasmod.TASmod;
import de.scribble.lp.tasmod.externalGui.InputContainerView;
import de.scribble.lp.tasmod.savestates.server.LoadstatePacket;
import de.scribble.lp.tasmod.savestates.server.SavestatePacket;
import de.scribble.lp.tasmod.tickratechanger.TickrateChangerClient;
import de.scribble.lp.tasmod.util.TASstate;
import de.scribble.lp.tasmod.virtual.VirtualKeybindings;
import net.minecraft.client.Minecraft;

/**
 * The class where all keybinding functionality is executed
 * @author ScribbleLP
 *
 */
public class KeybindingEvents {

	public static void fireKeybindingsEvent() {
		
		if (VirtualKeybindings.isKeyDownExceptTextfield(ClientProxy.savestateSaveKey)) {

			CommonProxy.NETWORK.sendToServer(new SavestatePacket());

		} else if (VirtualKeybindings.isKeyDownExceptTextfield(ClientProxy.savestateLoadKey)) {

			CommonProxy.NETWORK.sendToServer(new LoadstatePacket());

		} else if (VirtualKeybindings.isKeyDownExceptTextfield(ClientProxy.bufferViewKey)) {
			
			InputContainerView.startBufferView();
			
		} else if (VirtualKeybindings.isKeyDownExceptTextfield(ClientProxy.infoGuiKey)) {
			
			Minecraft.getMinecraft().displayGuiScreen(ClientProxy.hud);
			
		} else if (VirtualKeybindings.isKeyDown(ClientProxy.stopkey)) {
			
			TASstate.setOrSend(TASstate.NONE);
			
		} else if (VirtualKeybindings.isKeyDown(ClientProxy.tickratezeroKey)) {
			
			TickrateChangerClient.togglePause();
			
		} else if (VirtualKeybindings.isKeyDown(ClientProxy.tickAdvance)) {
			
			TickrateChangerClient.advanceTick();
			
		} else if (VirtualKeybindings.isKeyDown(ClientProxy.testingKey)) {
			
			TASstate.setOrSend(ClientProxy.virtual.getContainer().togglePause());
			
		} else if (ClientProxy.ktrngKey!=null && VirtualKeybindings.isKeyDown(ClientProxy.ktrngKey)) {
			
			TASmod.ktrngHandler.toggleChangeSeed();
			
		}
	}
}
