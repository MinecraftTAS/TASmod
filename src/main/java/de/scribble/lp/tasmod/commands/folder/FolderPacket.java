package de.scribble.lp.tasmod.commands.folder;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class FolderPacket implements IMessage{
	int command;
	public FolderPacket() {
	}
	
	public FolderPacket(int command) {
		this.command=command;
	}
	
	@Override
	public void fromBytes(ByteBuf buf) {
		command=buf.readInt();
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(command);
	}
	
	public int getCommand() {
		return command;
	}
}
