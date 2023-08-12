package com.minecrafttas.tasmod.savestates.server;

import com.minecrafttas.server.interfaces.PacketID;
import com.minecrafttas.tasmod.TASmod;
import com.minecrafttas.tasmod.savestates.client.gui.GuiSavestateSavingScreen;
import com.minecrafttas.tasmod.savestates.server.SavestateHandler.SavestateState;
import com.minecrafttas.tasmod.savestates.server.exceptions.SavestateException;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

public class SavestatePacket implements PacketID {

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
	public void handle(PacketSide side, EntityPlayer playerz) {
		if(side.isServer()) {
			EntityPlayerMP player = (EntityPlayerMP) playerz;
			player.getServerWorld().addScheduledTask(()->{
				if (!player.canUseCommand(2, "savestate")) {
					player.sendMessage(new TextComponentString(TextFormatting.RED+"You don't have permission to do that"));
					return;
				}
				try {
					TASmod.savestateHandler.saveState(index, true);
				} catch (SavestateException e) {
					player.sendMessage(new TextComponentString(TextFormatting.RED+"Failed to create a savestate: "+ e.getMessage()));
					
				} catch (Exception e) {
					e.printStackTrace();
					player.sendMessage(new TextComponentString(TextFormatting.RED+"Failed to create a savestate: "+ e.getCause().toString()));
				} finally {
					TASmod.savestateHandler.state=SavestateState.NONE;
				}
			});
			
		}
		else {
			Minecraft.getMinecraft().addScheduledTask(() -> {
				Minecraft mc = Minecraft.getMinecraft();
				if (!(mc.currentScreen instanceof GuiSavestateSavingScreen)) {
					mc.displayGuiScreen(new GuiSavestateSavingScreen());
				} else {
					mc.displayGuiScreen(null);
				}
			});
		}
	}

	@Override
	public void serialize(PacketBuffer buf) {
		buf.writeInt(index);
	}

	@Override
	public void deserialize(PacketBuffer buf) {
		index=buf.readInt();
	}

}
