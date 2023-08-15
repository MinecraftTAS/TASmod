package com.minecrafttas.tasmod.commands;

import com.minecrafttas.common.server.ByteBufferBuilder;
import com.minecrafttas.tasmod.TASmod;
import com.minecrafttas.tasmod.networking.TASmodPackets;

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
			try {
				TASmod.server.sendToAll(new ByteBufferBuilder(TASmodPackets.PLAYBACK_PLAYUNTIL).writeInt(i));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		else {
			sender.sendMessage(new TextComponentString("Stops the next playback one tick before the specified tick and lets you record from there:\n\n/playuntil 10, runs the playback until tick 9 and will record from there. Useful when you can't savestate"));
		}
	}

}
