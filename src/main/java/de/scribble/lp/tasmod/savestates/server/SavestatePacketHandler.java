package de.scribble.lp.tasmod.savestates.server;

import de.scribble.lp.tasmod.savestates.server.exceptions.SavestateException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Handles savestate networking
 * @author ScribbleLP
 * @see SavestatePacket
 */
public class SavestatePacketHandler implements IMessageHandler<SavestatePacket, IMessage>{

	public SavestatePacketHandler() {
	}
	
	@Override
	public IMessage onMessage(SavestatePacket message, MessageContext ctx) {
		if(ctx.side.isServer()) {
			ctx.getServerHandler().player.getServerWorld().addScheduledTask(()->{
				EntityPlayerMP player=ctx.getServerHandler().player;
				if (!player.canUseCommand(2, "tickrate")) {
					player.sendMessage(new TextComponentString(TextFormatting.RED+"You don't have permission to do that"));
					return;
				}
				try {
					SavestateHandler.saveState();
				} catch (SavestateException e) {
					player.sendMessage(new TextComponentString(TextFormatting.RED+"Failed to create a savestate: "+ e.getMessage()));
					
				} catch (Exception e) {
					e.printStackTrace();
					player.sendMessage(new TextComponentString(TextFormatting.RED+"Failed to create a savestate: "+ e.getCause().toString()));
				} finally {
					SavestateHandler.state=SavestateState.NONE;
				}
			});
		}else {
			net.minecraft.client.Minecraft mc=net.minecraft.client.Minecraft.getMinecraft();
			workaround(mc);
		}
		return null;
	}
	
	@SideOnly(Side.CLIENT)
	private void workaround(net.minecraft.client.Minecraft mc) {
		if(!(mc.currentScreen instanceof GuiSavestateSavingScreen)) {
			mc.displayGuiScreen(new GuiSavestateSavingScreen());
		}else {
			mc.displayGuiScreen(null);
		}
	}
}
