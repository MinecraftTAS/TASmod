package de.scribble.lp.tasmod.savestates.motion;

import de.scribble.lp.tasmod.CommonProxy;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;

public class MotionEvents {
	@SubscribeEvent
	public void onTick(TickEvent.ClientTickEvent ev) {
		if(ev.phase==Phase.START) {
			EntityPlayerSP player=Minecraft.getMinecraft().player;
			if(player!=null) {
				CommonProxy.NETWORK.sendToServer(new MotionPacket(player.motionX, player.motionY, player.motionZ, player.moveForward, player.moveVertical, player.moveStrafing));
			}
		}
	}
}
