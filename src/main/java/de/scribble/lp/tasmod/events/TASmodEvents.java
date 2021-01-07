package de.scribble.lp.tasmod.events;

import java.io.FileNotFoundException;

import de.scribble.lp.tasmod.CommonProxy;
import de.scribble.lp.tasmod.playback.PlaybackPacket;
import de.scribble.lp.tasmod.recording.InputRecorder;
import de.scribble.lp.tasmod.tickratechanger.TickrateChangerClient;
import de.scribble.lp.tasmod.tickratechanger.TickrateChangerServer;
import de.scribble.lp.tasmod.ticksync.TickSyncPackage;
import de.scribble.lp.tasmod.ticksync.TickSyncServer;
import de.scribble.lp.tasmod.virtual.VirtualMouseAndKeyboard;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedOutEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;

public class TASmodEvents {
	@SubscribeEvent
	public void playerLogin(PlayerLoggedInEvent ev) {
		TickSyncServer.resetTickCounter();
		CommonProxy.NETWORK.sendToAll(new TickSyncPackage(TickSyncServer.getServertickcounter(), true, TickSyncServer.isEnabled()));
		
		CommonProxy.NETWORK.sendToAll(new PlaybackPacket());
		
		if(TickrateChangerClient.TICKS_PER_SECOND==0) {
			TickrateChangerServer.changeServerTickrate(0F);
		}
		PlayerActivateEvent event= new PlayerActivateEvent(6, ev.player);
		MinecraftForge.EVENT_BUS.register(event);
	}
	@SubscribeEvent
	public void playerLogout(PlayerLoggedOutEvent ev) {
		if(TickrateChangerServer.TICKS_PER_SECOND==0) {
			TickrateChangerServer.changeServerTickrate(20F);
		}
	}
	public void firePlayerActivateEvent(EntityPlayer player) {
		if(InputRecorder.isRewind()) {
			try {
				InputRecorder.appendRecording(null);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
	}
	class PlayerActivateEvent{
		int countdown;
		EntityPlayer player;
		
		public PlayerActivateEvent(int activate, EntityPlayer player) {
			countdown=activate;
			this.player=player;
		}
		@SubscribeEvent
		public void onTick(TickEvent.ClientTickEvent ev) {
			if(ev.phase==Phase.START) {
				if(countdown==0) {
					firePlayerActivateEvent(player);
					MinecraftForge.EVENT_BUS.unregister(this);
				}
				countdown--;
			}
		}
	}
}
