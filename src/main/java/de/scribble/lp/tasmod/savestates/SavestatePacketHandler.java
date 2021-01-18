package de.scribble.lp.tasmod.savestates;

import de.scribble.lp.tasmod.savestates.exceptions.SavestateException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiIngameMenu;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class SavestatePacketHandler implements IMessageHandler<SavestatePacket, IMessage>{

	@Override
	public IMessage onMessage(SavestatePacket message, MessageContext ctx) {
		if(ctx.side.isServer()) {
			ctx.getServerHandler().player.getServerWorld().addScheduledTask(()->{
				if (!ctx.getServerHandler().player.canUseCommand(2, "tickrate")) {
					return;
				}
				try {
					SavestateHandler.saveState();
				} catch (SavestateException e) {
					ctx.getServerHandler().player.sendMessage(new TextComponentString(TextFormatting.RED+"Failed to create a savestate: "+e.getMessage()));
					
				} catch (Exception e) {
					ctx.getServerHandler().player.sendMessage(new TextComponentString(TextFormatting.RED+"Failed to create a savestate: "+e.getCause().toString()));
					e.printStackTrace();
				}finally {
					SavestateHandler.isSaving=false;
				}
			});
		}else {
			Minecraft mc=Minecraft.getMinecraft();
			mc.addScheduledTask(()->{
				if(!(mc.currentScreen instanceof GuiSavestateSavingScreen)) {
					mc.displayGuiScreen(new GuiSavestateSavingScreen());
				}else {
					mc.displayGuiScreen(new GuiIngameMenu());
				}
			});
		}
		return null;
	}

}
