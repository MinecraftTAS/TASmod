package com.minecrafttas.tasmod.savestates.handlers;

import static com.minecrafttas.tasmod.registries.TASmodPackets.SAVESTATE_LOADING_SCREEN;
import static com.minecrafttas.tasmod.registries.TASmodPackets.SAVESTATE_RENAME_SCREEN;

import java.nio.ByteBuffer;

import com.minecrafttas.tasmod.mctcommon.networking.Client.Side;
import com.minecrafttas.tasmod.mctcommon.networking.exception.PacketNotImplementedException;
import com.minecrafttas.tasmod.mctcommon.networking.exception.WrongSideException;
import com.minecrafttas.tasmod.mctcommon.networking.interfaces.ClientPacketHandler;
import com.minecrafttas.tasmod.mctcommon.networking.interfaces.PacketID;
import com.minecrafttas.tasmod.TASmodClient;
import com.minecrafttas.tasmod.networking.TASmodBufferBuilder;
import com.minecrafttas.tasmod.registries.TASmodPackets;
import com.minecrafttas.tasmod.savestates.SavestateHandlerServer.SavestateState;
import com.minecrafttas.tasmod.savestates.gui.GuiSavestate;
import com.minecrafttas.tasmod.savestates.gui.GuiSavestateRename;
import com.minecrafttas.tasmod.util.TASComponent;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

/**
 * Handles displaying Gui screens for savestating on the client
 * 
 * @author Scribble
 */
public class SavestateGuiHandlerClient implements ClientPacketHandler {

	@Override
	public PacketID[] getAcceptedPacketIDs() {
		//@formatter:off
		return new PacketID[] {
			SAVESTATE_LOADING_SCREEN,
			SAVESTATE_RENAME_SCREEN
		};
		//@formatter:on
	}

	@Override
	public void onClientPacket(PacketID id, ByteBuffer buf, String username) throws PacketNotImplementedException, WrongSideException, Exception {
		TASmodPackets packet = (TASmodPackets) id;
		Minecraft mc = Minecraft.getInstance();

		switch (packet) {
			case SAVESTATE_LOADING_SCREEN:
				// Open Savestate screen
				SavestateState state = TASmodBufferBuilder.readEnum(SavestateState.class, buf);

				TASmodClient.gameLoopSchedulerClient.add(() -> {
					String msg = "";
					if (state == SavestateState.SAVING)
						msg = "gui.tasmod.savestate.save.start";
					else if (state == SavestateState.LOADING)
						msg = "gui.tasmod.savestate.load.start";
					// mc.setScreen(new GuiSavestate(Component.translatable(msg).withStyle(ChatFormatting.YELLOW))); // Stubbed
				});
				break;
			case SAVESTATE_RENAME_SCREEN:
				int index = TASmodBufferBuilder.readInt(buf);
				/*
				 * At the time of writing, the savestate rename screen
				 * is only opened when the savestate is triggered via a keybind
				 * 
				 * However opening the screen would desync a running recording
				 * by displacing the player by a few units.
				 * 
				 * The solution is to first clear the screen, then at the start of the next tick
				 * display the screen.
				 * 
				 * Apparently showing a screen has a tiny influence on the motion of the client...
				 */
				// mc.setScreen(null); // Stubbed
				TASmodClient.tickSchedulerClient.add(() -> {
					displayGuiRename(index);
				});
				break;
			default:
				throw new PacketNotImplementedException(packet, Side.CLIENT);
		}
	}

	private void displayGuiRename(int index) {
		Minecraft mc = Minecraft.getInstance();
		//@formatter:off
		// mc.setScreen(
		// 		new GuiSavestateRename(
		// 				Component.translatable("gui.tasmod.savestate.save.rename", 
		// 						Component.literal(Integer.toString(index)).withStyle(t->t.withColor(ChatFormatting.AQUA))
		// 				).withStyle(t->t.withColor(ChatFormatting.GREEN)),
		// 				index
		// 		)
		// );
		//@formatter:on
	}
}

