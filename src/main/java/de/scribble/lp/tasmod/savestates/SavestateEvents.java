package de.scribble.lp.tasmod.savestates;

import java.io.IOException;

import de.scribble.lp.tasmod.ClientProxy;
import de.scribble.lp.tasmod.CommonProxy;
import de.scribble.lp.tasmod.recording.InputRecorder;
import de.scribble.lp.tasmod.recording.RecordingPacket;
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
			CommonProxy.NETWORK.sendToServer(new SavestatePacket());
			
		}else if(VirtualKeybindings.isKeyDownExceptChat(ClientProxy.savestateLoadKey)){
//			if(InputRecorder.isRecording()) {
//				VirtualKeybindings.registerBlockedDuringRecordingKeyBinding(ClientProxy.savestateLoadKey);
//			}
//			if(TickrateChangerClient.TICKS_PER_SECOND==0) {
//				TickrateChangerServer.pauseUnpauseGame();
//			}
//			ClientProxy.getSaveHandler().loadLastSavestate();
			
			CommonProxy.NETWORK.sendToServer(new LoadstatePacket());
		}else if(VirtualKeybindings.isKeyDownExceptChat(ClientProxy.testingKey)) {
		}
	}
}
