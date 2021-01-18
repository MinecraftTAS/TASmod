package de.scribble.lp.tasmod.savestates;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

/**
 * Simple networking packet to initiate things on client and server
 * 
 * @OnClient Displays GuiSavestatingScreen. <br>If that is already open, it displays IngameMenu
 * @OnServer Initiates savestating
 * @author ScribbleLP
 * 
 * @see SavestatePacketHandler
 *
 */
public class SavestatePacket implements IMessage{

	@Override
	public void fromBytes(ByteBuf buf) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void toBytes(ByteBuf buf) {
		// TODO Auto-generated method stub
		
	}

}
