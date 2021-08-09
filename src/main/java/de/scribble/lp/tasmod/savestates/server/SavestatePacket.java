package de.scribble.lp.tasmod.savestates.server;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

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

	public SavestatePacket() {
	}
	
	@Override
	public void fromBytes(ByteBuf buf) {
	}

	@Override
	public void toBytes(ByteBuf buf) {
	}

}
