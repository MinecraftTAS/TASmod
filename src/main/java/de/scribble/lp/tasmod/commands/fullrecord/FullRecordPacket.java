package de.scribble.lp.tasmod.commands.fullrecord;

import de.scribble.lp.tasmod.events.OpenGuiEvents;
import de.scribble.lp.tasmod.util.TASstate;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class FullRecordPacket implements IMessage {

	public FullRecordPacket() {
	}

	@Override
	public void fromBytes(ByteBuf buf) {
	}

	@Override
	public void toBytes(ByteBuf buf) {
	}

	public static class FullRecordPacketHandler implements IMessageHandler<FullRecordPacket, IMessage> {

		public FullRecordPacketHandler() {
		}

		@Override
		public IMessage onMessage(FullRecordPacket message, MessageContext ctx) {
			if (ctx.side == Side.CLIENT) {
				Minecraft mc = Minecraft.getMinecraft();
				mc.addScheduledTask(() -> {
					workaround(mc);
				});
			}
			return null;
		}

		@SideOnly(Side.CLIENT)
		private void workaround(Minecraft mc) {
			OpenGuiEvents.stateWhenOpened = TASstate.RECORDING;
			mc.world.sendQuittingDisconnectingPacket();
			mc.loadWorld((WorldClient) null);
			mc.displayGuiScreen(new net.minecraft.client.gui.GuiMainMenu());
		}
	}
}
