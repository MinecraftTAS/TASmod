package com.minecrafttas.tasmod.commands;

import com.minecrafttas.tasmod.TASmod;
import com.minecrafttas.tasmod.networking.TASmodBufferBuilder;
import com.minecrafttas.tasmod.networking.TASmodPackets;
import com.minecrafttas.tasmod.playback.PlaybackControllerClient.TASstate;
import com.minecrafttas.tasmod.savestates.SavestateHandlerServer.SavestateState;
import com.minecrafttas.tasmod.savestates.exceptions.LoadstateException;

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
			TASmod.savestateHandlerServer.loadState(0, false, false);
		} catch (LoadstateException e) {
			sender.sendMessage(new TextComponentString(TextFormatting.RED+"Failed to load a savestate: "+e.getMessage()));
			return;
		} catch (Exception e) {
			sender.sendMessage(new TextComponentString(TextFormatting.RED+"Failed to load a savestate: "+e.getCause().toString()));
			e.printStackTrace();
			return;
		} finally {
			TASmod.savestateHandlerServer.state=SavestateState.NONE;
		}
		TASmod.playbackControllerServer.setServerState(TASstate.PLAYBACK);
		try {
			TASmod.server.sendToAll(new TASmodBufferBuilder(TASmodPackets.PLAYBACK_FULLPLAY));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
