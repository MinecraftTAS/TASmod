package de.scribble.lp.tasmod.tickratechanger;

import java.util.List;

import com.google.common.collect.ImmutableList;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;

public class CommandTickrate extends CommandBase{
	private final List<String> aliases = ImmutableList.of("ticks", "tickratechanger", "trc", "settickrate");
	@Override
	public String getName() {
		return "tickrate";
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return "/tickrate [ticks per second] [all/server/client/playername]";
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		float tickrate= Float.parseFloat(args[0]);
		TickrateChangerServer.changeServerTickrate(tickrate);
		TickrateChangerServer.changeClientTickrate(tickrate);
	}

}
