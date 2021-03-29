package de.scribble.lp.tasmod.savestates;

import java.util.List;

import org.lwjgl.input.Keyboard;

import de.scribble.lp.tasmod.ClientProxy;
import de.scribble.lp.tasmod.CommonProxy;
import de.scribble.lp.tasmod.input.InputContainer;
import de.scribble.lp.tasmod.virtual.VirtualKeybindings;
import de.scribble.lp.tasmod.virtual.VirtualKeyboardEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class SavestateEvents {
	@SubscribeEvent
	public void onRender(TickEvent.RenderTickEvent ev) {
		if(VirtualKeybindings.isKeyDownExceptTextfield(ClientProxy.savestateSaveKey)){
			
			CommonProxy.NETWORK.sendToServer(new SavestatePacket());
			
		}else if(VirtualKeybindings.isKeyDownExceptTextfield(ClientProxy.savestateLoadKey)){
			
			CommonProxy.NETWORK.sendToServer(new LoadstatePacket());
			
		}else if(VirtualKeybindings.isKeyDownExceptTextfield(ClientProxy.testingKey)) {
			
		}
	}
}
