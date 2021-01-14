package de.scribble.lp.tasmod.savestates;

import de.scribble.lp.tasmod.ClientProxy;
import de.scribble.lp.tasmod.CommonProxy;
import de.scribble.lp.tasmod.savestatesV2.SavestateChunkLoadingPacket;
import de.scribble.lp.tasmod.savestatesV2.SavestatesChunkControl;
import de.scribble.lp.tasmod.virtual.VirtualKeybindings;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class SavestateEvents {
	@SubscribeEvent
	public void onRender(TickEvent.RenderTickEvent ev) {
		if(VirtualKeybindings.isKeyDownExceptChat(ClientProxy.savestateSaveKey)){
//			Minecraft.getMinecraft().displayGuiScreen(new GuiSavestateSavingScreen());
//			if(InputRecorder.isRecording()) {
//				VirtualKeybindings.registerBlockedDuringRecordingKeyBinding(ClientProxy.savestateSaveKey);
//			}
//			if(TickrateChangerClient.TICKS_PER_SECOND==0) {
//				TickrateChangerServer.pauseUnpauseGame();
//			}
//			ClientProxy.getSaveHandler().saveState();
			CommonProxy.NETWORK.sendToServer(new SavestateChunkLoadingPacket(false));
			
		}else if(VirtualKeybindings.isKeyDownExceptChat(ClientProxy.savestateLoadKey)){
//			if(InputRecorder.isRecording()) {
//				VirtualKeybindings.registerBlockedDuringRecordingKeyBinding(ClientProxy.savestateLoadKey);
//			}
//			if(TickrateChangerClient.TICKS_PER_SECOND==0) {
//				TickrateChangerServer.pauseUnpauseGame();
//			}
//			ClientProxy.getSaveHandler().loadLastSavestate();
			CommonProxy.NETWORK.sendToServer(new SavestateChunkLoadingPacket());
			SavestatesChunkControl.unloadAllClientChunks();
		}
	}
}
