package com.minecrafttas.tasmod.commands.fullplay;

import com.minecrafttas.tasmod.TASmod;
import com.minecrafttas.tasmod.playback.PlaybackController.TASstate;
import com.minecrafttas.tasmod.savestates.server.SavestateHandler.SavestateState;
import com.minecrafttas.tasmod.savestates.server.exceptions.LoadstateException;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

public class CommandFullPlay extends CommandBase{

	@Override
	public String getName() {
		return "fullplay";
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return "/fullplay";
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		try {
			TASmod.savestateHandler.loadState(0, false, false);
		} catch (LoadstateException e) {
			sender.sendMessage(new TextComponentString(TextFormatting.RED+"Failed to load a savestate: "+e.getMessage()));
			return;
		} catch (Exception e) {
			sender.sendMessage(new TextComponentString(TextFormatting.RED+"Failed to load a savestate: "+e.getCause().toString()));
			e.printStackTrace();
			return;
		} finally {
			TASmod.savestateHandler.state=SavestateState.NONE;
		}
		TASmod.containerStateServer.setServerState(TASstate.PLAYBACK);
		TASmod.packetServer.sendToAll(new FullPlayPacket());
	}

}
