package de.scribble.lp.tasmod.savestates;

import de.scribble.lp.tasmod.ClientProxy;
import de.scribble.lp.tasmod.tickratechanger.TickrateChangerClient;
import de.scribble.lp.tasmod.tickratechanger.TickrateChangerServer;
import de.scribble.lp.tasmod.virtual.VirtualKeybindings;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class SavestateEvents {
	@SubscribeEvent
	public void onRender(TickEvent.RenderTickEvent ev) {
		if(VirtualKeybindings.isKeyDown(ClientProxy.savestateSaveKey)){
			Minecraft.getMinecraft().displayGuiScreen(new GuiSavestateSavingScreen());
			if(TickrateChangerClient.TICKS_PER_SECOND==0) {
				TickrateChangerServer.pauseUnpauseGame();
			}
			ClientProxy.getSaveHandler().saveState();
		}else if(VirtualKeybindings.isKeyDown(ClientProxy.savestateLoadKey)){
			ClientProxy.getSaveHandler().loadLastSavestate();
		}
	}
}
