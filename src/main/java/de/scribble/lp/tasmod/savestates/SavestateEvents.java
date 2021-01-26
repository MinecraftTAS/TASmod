package de.scribble.lp.tasmod.savestates;

import java.util.List;

import de.scribble.lp.tasmod.ClientProxy;
import de.scribble.lp.tasmod.CommonProxy;
import de.scribble.lp.tasmod.ModLoader;
import de.scribble.lp.tasmod.duck.ChunkProviderDuck;
import de.scribble.lp.tasmod.savestates.playerloading.SavestatePlayerLoadingPacket;
import de.scribble.lp.tasmod.virtual.VirtualKeybindings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ChunkProviderClient;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.management.PlayerList;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class SavestateEvents {
	@SubscribeEvent
	public void onRender(TickEvent.RenderTickEvent ev) {
		if(VirtualKeybindings.isKeyDownExceptChat(ClientProxy.savestateSaveKey)){
			
			CommonProxy.NETWORK.sendToServer(new SavestatePacket());
			
		}else if(VirtualKeybindings.isKeyDownExceptChat(ClientProxy.savestateLoadKey)){
			
			CommonProxy.NETWORK.sendToServer(new LoadstatePacket());
			
		}else if(VirtualKeybindings.isKeyDownExceptChat(ClientProxy.testingKey)) {
			CommonProxy.NETWORK.sendToServer(new SavestatePlayerLoadingPacket());
		}
	}
}
