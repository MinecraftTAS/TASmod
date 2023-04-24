package com.minecrafttas.tasmod.commands.playuntil;

import com.minecrafttas.tasmod.TASmod;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;

public class CommandPlayUntil extends CommandBase{

	@Override
	public String getName() {
		return "playuntil";
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return "/playuntil <ticks>";
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		if(args.length==1) {
			int i = 0;
			try {
				i = Integer.parseInt(args[0]);
			} catch (NumberFormatException e) {
				throw new CommandException("{} is not a number", args[0]);
			}
			TASmod.packetServer.sendToAll(new PlayUntilPacket(i));
		}
	}

}
