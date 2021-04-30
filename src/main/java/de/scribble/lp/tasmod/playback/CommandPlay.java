package de.scribble.lp.tasmod.playback;

import java.util.List;

import com.google.common.collect.ImmutableList;

import de.scribble.lp.tasmod.ClientProxy;
import de.scribble.lp.tasmod.CommonProxy;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

public class CommandPlay extends CommandBase {

	@Override
	public String getName() {
		return "play";
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return "/play <true|false>";
	}

	@Override
	public int getRequiredPermissionLevel() {
		return 2;
	}

	@Override
	public List<String> getAliases() {
		return ImmutableList.of("p");
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		if (!(sender instanceof EntityPlayer)) {
			return;
		}
		if (ClientProxy.virtual.getContainer().isPlayingback()) {
			return;
		}
		if (args.length < 1) {
			CommonProxy.NETWORK.sendToServer(new PlaybackPacket(!ClientProxy.virtual.getContainer().isPlayingback()));
		} else if (args.length == 1) {
			if (args[0].equalsIgnoreCase("true")) {
				CommonProxy.NETWORK.sendToAll(new PlaybackPacket(true));
			} else if (args[0].equalsIgnoreCase("false")) {
				CommonProxy.NETWORK.sendToAll(new PlaybackPacket(false));
			} else {
				sender.sendMessage(new TextComponentString(TextFormatting.RED + "Couldn't process the argument " + args[0] + ". Must be either true or false"));
			}
		} else if (args.length > 1) {
			sender.sendMessage(new TextComponentString(TextFormatting.RED + "Too many arguments. " + getUsage(sender)));
		}

	}

	@Override
	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, BlockPos targetPos) {
		if (args.length == 1) {
			return getListOfStringsMatchingLastWord(args, ImmutableList.of("true", "false"));
		}
		return super.getTabCompletions(server, sender, args, targetPos);
	}
}
