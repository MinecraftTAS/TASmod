package de.scribble.lp.tasmod.savestates.motion;

import de.scribble.lp.tasmod.CommonProxy;
import de.scribble.lp.tasmod.savestates.GuiSavestateSavingScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class RequestMotionPacketHandler implements IMessageHandler<RequestMotionPacket, IMessage>{

	@Override
	public IMessage onMessage(RequestMotionPacket message, MessageContext ctx) {
		if(ctx.side.isClient()) {
			Minecraft.getMinecraft().addScheduledTask(()->{
				EntityPlayerSP player=Minecraft.getMinecraft().player;
				if(player!=null) {
					if(!(Minecraft.getMinecraft().currentScreen instanceof GuiSavestateSavingScreen)) {
						Minecraft.getMinecraft().displayGuiScreen(new GuiSavestateSavingScreen());
					}
					CommonProxy.NETWORK.sendToServer(new MotionPacket(player.motionX, player.motionY, player.motionZ, player.moveForward, player.moveVertical, player.moveStrafing));
				}
			});
		}
		return null;
	}

}
