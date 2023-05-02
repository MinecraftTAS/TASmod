package com.minecrafttas.tasmod.commands.playuntil;

import com.minecrafttas.tasmod.TASmod;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;

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
		else {
			sender.sendMessage(new TextComponentString("Stops the next playback one tick before the specified tick and lets you record from there:\n\n/playuntil 10, runs the playback until tick 9 and will record from there. Useful when you can't savestate"));
		}
	}

}
