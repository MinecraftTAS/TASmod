package com.minecrafttas.tasmod.commands;

import java.util.List;

import com.google.common.collect.ImmutableList;
import com.minecrafttas.tasmod.TASmod;

import net.minecraft.client.resources.I18n;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
/**
 * Command to change the tickrate of client and server
 * @author ScribbleLP
 *
 */
public class CommandTickrate extends CommandBase {
	
	@Override
	public String getName() {
		return "tickrate";
	}

	@Override
	public List<String> getAliases() {
		return ImmutableList.of("ticks", "tickratechanger", "trc", "settickrate");
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return I18n.format("tickratechanger.tasmod.command.usage"); // "/tickrate <ticks per second>"
	}

	@Override
	public int getRequiredPermissionLevel() {
		return 2;
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		if (args.length == 0) {
			sender.sendMessage(new TextComponentString( I18n.format("tickratechanger.tasmod.command.show", TASmod.tickratechanger.ticksPerSecond))); //"Current tickrate: "
			return;
		}
		float tickrate;
		try {
			tickrate = Float.parseFloat(args[0]);
		} catch (NumberFormatException e) {
			throw new CommandException("Invalid tickrate: " + args[0], new Object[] {});
		}
		TASmod.tickratechanger.changeTickrate(tickrate);
	}

}
