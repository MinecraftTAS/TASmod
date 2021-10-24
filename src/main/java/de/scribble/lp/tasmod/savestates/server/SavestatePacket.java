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

}
