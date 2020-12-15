package de.scribble.lp.tasmod.playback;

import java.io.File;

import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public class PlaybackPacketHandler implements IMessageHandler<PlaybackPacket, IMessage>{

	@Override
	public IMessage onMessage(PlaybackPacket message, MessageContext ctx) {
		if(ctx.side==Side.CLIENT) {
			File file = new File(Minecraft.getMinecraft().mcDataDir, "saves" + File.separator +
	                "tasfiles" + File.separator + "startup.tas");
			if(file.exists()) {
				InputPlayback.startPlayback(file, "startup");
			}
		}
		return null;
	}

}
