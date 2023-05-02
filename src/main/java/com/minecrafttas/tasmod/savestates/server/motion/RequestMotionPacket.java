package com.minecrafttas.tasmod.savestates.server.motion;

import com.minecrafttas.tasmod.ClientProxy;
import com.minecrafttas.tasmod.networking.Packet;
import com.minecrafttas.tasmod.networking.PacketSide;
import com.minecrafttas.tasmod.savestates.client.gui.GuiSavestateSavingScreen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;

/**
 * Requests the motion from the client
 * @author Scribble
 *
 */
public class RequestMotionPacket implements Packet {

	/**
	 * Requests the motion from the client
	 */
	public RequestMotionPacket() {
	}
	
	@Override
	public void handle(PacketSide side, EntityPlayer playerz) {
		if(side.isClient()) {
			EntityPlayerSP player = (EntityPlayerSP) playerz;
			if (player != null) {
				if (!(Minecraft.getMinecraft().currentScreen instanceof GuiSavestateSavingScreen)) {
					Minecraft.getMinecraft().displayGuiScreen(new GuiSavestateSavingScreen());
				}
				ClientProxy.packetClient.sendToServer(new MotionPacket(player.motionX, player.motionY, player.motionZ, player.moveForward, player.moveVertical, player.moveStrafing, player.isSprinting(), player.jumpMovementFactor));
			}
		}
		
	}

	@Override
	public void serialize(PacketBuffer buf) {
		
	}

	@Override
	public void deserialize(PacketBuffer buf) {
		
	}

}
