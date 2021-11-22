package de.scribble.lp.tasmod.tickratechanger;

import java.util.List;

import com.google.common.collect.ImmutableList;

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
		return "/tickrate <ticks per second>";
	}

	@Override
	public int getRequiredPermissionLevel() {
		return 2;
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		if (args.length == 0) {
			sender.sendMessage(new TextComponentString("Current tickrate: " + TickrateChangerServer.ticksPerSecond));
			return;
		}
		float tickrate;
		try {
			tickrate = Float.parseFloat(args[0]);
		} catch (NumberFormatException e) {
			throw new CommandException("Invalid tickrate: " + args[0], new Object[] {});
		}
		TickrateChangerServer.changeTickrate(tickrate);
	}

}
