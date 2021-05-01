package de.scribble.lp.tasmod.folder;

import java.util.ArrayList;
import java.util.List;

import de.scribble.lp.tasmod.CommonProxy;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

public class CommandFolder extends CommandBase {

	@Override
	public String getName() {
		return "folder";
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return "/folder <type>";
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		if (args.length == 1) {
			if (args[0].equalsIgnoreCase("savestates")) {
				CommonProxy.NETWORK.sendTo(new FolderPacket(0), (EntityPlayerMP) sender);
			} else if (args[0].equalsIgnoreCase("tasfiles")) {
				CommonProxy.NETWORK.sendTo(new FolderPacket(1), (EntityPlayerMP) sender);
			}
		}
	}

	@Override
	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, BlockPos targetPos) {
		List<String> tab = new ArrayList<String>();
		if (args.length == 1) {
			tab.addAll(getListOfStringsMatchingLastWord(args, new String[] { "savestates", "tasfiles" }));
		} else {
			tab.clear();
		}
		return tab;
	}

}
