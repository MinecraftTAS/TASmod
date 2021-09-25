package de.scribble.lp.tasmod.savestates.server.motion;

import de.scribble.lp.tasmod.CommonProxy;
import de.scribble.lp.tasmod.savestates.server.GuiSavestateSavingScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class RequestMotionPacketHandler implements IMessageHandler<RequestMotionPacket, IMessage> {

	@Override
	public IMessage onMessage(RequestMotionPacket message, MessageContext ctx) {
		if (ctx.side.isClient()) {
			workaround();
		}
		return null;
	}
	
	
	@SideOnly(Side.CLIENT)
	//For some reason the packet still tries to execute this on the server even tho ctx.side = CLIENT
	private void workaround() {
		EntityPlayerSP player = Minecraft.getMinecraft().player;
		if (player != null) {
			if (!(Minecraft.getMinecraft().currentScreen instanceof GuiSavestateSavingScreen)) {
				Minecraft.getMinecraft().displayGuiScreen(new GuiSavestateSavingScreen());
			}
			CommonProxy.NETWORK.sendToServer(new MotionPacket(player.motionX, player.motionY, player.motionZ, player.moveForward, player.moveVertical, player.moveStrafing, player.isSprinting(), player.jumpMovementFactor));
		}
	}
}
