package de.scribble.lp.tasmod.savestates;

import java.util.Set;

import de.scribble.lp.tasmod.ClientProxy;
import de.scribble.lp.tasmod.CommonProxy;
import de.scribble.lp.tasmod.ModLoader;
import de.scribble.lp.tasmod.monitoring.Monitor;
import de.scribble.lp.tasmod.savestates.chunkloading.SavestatesChunkControl;
import de.scribble.lp.tasmod.savestates.playerloading.NoPortalTeleporter;
import de.scribble.lp.tasmod.virtual.VirtualKeybindings;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityTrackerEntry;
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
			Set<EntityTrackerEntry> entries=(Set<EntityTrackerEntry>) Monitor.accessField(Minecraft.getMinecraft().getIntegratedServer().worlds[1].getEntityTracker(), "entries");
			entries.forEach(arg0->{
				System.out.println(arg0.getTrackedEntity().getName());
			});
		}
	}
}
