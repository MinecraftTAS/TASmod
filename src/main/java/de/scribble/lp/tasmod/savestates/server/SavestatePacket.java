package de.scribble.lp.tasmod.savestates.server;

import de.scribble.lp.tasmod.TASmod;
import de.scribble.lp.tasmod.savestates.client.gui.GuiSavestateSavingScreen;
import de.scribble.lp.tasmod.savestates.server.exceptions.SavestateException;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Simple networking packet to initiate things on client and server
 * 
 * OnClient: Displays GuiSavestatingScreen. <br>If that is already open, closes gui
 * OnServer: Initiates savestating
 * @author ScribbleLP
 * 
 * @see SavestatePacketHandler
 *
 */
public class SavestatePacket implements IMessage{

	public int index;
	
	/**
	 * Make a savestate at the next index
	 */
	public SavestatePacket() {
		index=-1;
	}
	
	/**
	 * Make a savestate at the specified index
	 * 
	 * @param index The index where to make a savestate
	 */
	public SavestatePacket(int index) {
		this.index=index;
	}
	
	@Override
	public void fromBytes(ByteBuf buf) {
		index=buf.readInt();
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(index);
	}

	public static class SavestatePacketHandler implements IMessageHandler<SavestatePacket, IMessage>{

		public SavestatePacketHandler() {
		}
		
		@Override
		public IMessage onMessage(SavestatePacket message, MessageContext ctx) {
			if(ctx.side.isServer()) {
				ctx.getServerHandler().player.getServerWorld().addScheduledTask(()->{
					EntityPlayerMP player=ctx.getServerHandler().player;
					if (!player.canUseCommand(2, "savestate")) {
						player.sendMessage(new TextComponentString(TextFormatting.RED+"You don't have permission to do that"));
						return;
					}
					try {
						TASmod.savestateHandler.saveState(message.index, true);
					} catch (SavestateException e) {
						player.sendMessage(new TextComponentString(TextFormatting.RED+"Failed to create a savestate: "+ e.getMessage()));
						
					} catch (Exception e) {
						e.printStackTrace();
						player.sendMessage(new TextComponentString(TextFormatting.RED+"Failed to create a savestate: "+ e.getCause().toString()));
					} finally {
						TASmod.savestateHandler.state=SavestateState.NONE;
					}
				});
			}else {
				net.minecraft.client.Minecraft mc=net.minecraft.client.Minecraft.getMinecraft();	//Forge will think this is executed on the server for some reason...
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
}
