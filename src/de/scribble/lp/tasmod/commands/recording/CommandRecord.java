package de.scribble.lp.tasmod.commands.recording;

import java.util.List;

import com.google.common.collect.ImmutableList;

import de.scribble.lp.tasmod.TASmod;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

public class CommandRecord extends CommandBase {

	@Override
	public String getName() {
		return "record";
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return "/record";
	}

	@Override
	public int getRequiredPermissionLevel() {
		return 2;
	}

	@Override
	public List<String> getAliases() {
		return ImmutableList.of("r");
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		if (!(sender instanceof EntityPlayer)) {
			return;
		}
		if (args.length < 1) {
			TASmod.containerStateServer.toggleRecording();
		} else if (args.length > 1) {
			sender.sendMessage(new TextComponentString(TextFormatting.RED + "Too many arguments. " + getUsage(sender)));
		}

	}

	@Override
	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, BlockPos targetPos) {
		return super.getTabCompletions(server, sender, args, targetPos);
	}
}
