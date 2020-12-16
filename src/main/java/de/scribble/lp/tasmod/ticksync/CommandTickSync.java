package de.scribble.lp.tasmod.ticksync;

import java.util.List;

import de.scribble.lp.tasmod.CommonProxy;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

public class CommandTickSync extends CommandBase{

	@Override
	public String getName() {
		return "ticksync";
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return "/ticksync [reset]";
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		if(args.length==0) {
			TickSyncServer.sync(!TickSyncServer.isEnabled());
			TickSyncServer.resetTickCounter();
			CommonProxy.NETWORK.sendToAll(new TickSyncPackage(TickSyncServer.getServertickcounter(),true,!TickSyncServer.isEnabled()));
		}else if(args[0].equalsIgnoreCase("reset")&&args.length==1) {
			TickSyncServer.resetTickCounter();
			CommonProxy.NETWORK.sendToAll(new TickSyncPackage(TickSyncServer.getServertickcounter(),true,TickSyncServer.isEnabled()));
		}
	}
	@Override
	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args,
			BlockPos targetPos) {
		return getListOfStringsMatchingLastWord(args, "reset");
	}

}
