package de.scribble.lp.tasmod.ticksync;


import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public class TickSyncPacketHandler implements IMessageHandler<TickSyncPackage, IMessage>{

	@Override
	public IMessage onMessage(TickSyncPackage message, MessageContext ctx) {
		if(ctx.side==Side.CLIENT) {
			Minecraft.getMinecraft().addScheduledTask(()->{
				if(message.isShouldreset()) {
					TickSync.resetTickCounter();
				}else TickSync.setServerTickcounter(message.getTicks());
				
				if(TickSync.isEnabled()!=message.isShouldstop()) {
					TickSync.sync(message.isShouldstop());
				}
			});
		}
		return null;
	}

}
