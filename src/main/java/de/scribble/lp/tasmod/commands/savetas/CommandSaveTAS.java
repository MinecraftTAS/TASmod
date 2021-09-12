package de.scribble.lp.tasmod.commands.savetas;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableList;

import de.scribble.lp.tasmod.CommonProxy;
import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

public class CommandSaveTAS extends CommandBase {

	private boolean check = false;

	@Override
	public String getName() {
		return "save";
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return "/save <filename>";
	}

	@Override
	public List<String> getAliases() {
		return ImmutableList.of("saveTAS");
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		if (sender instanceof EntityPlayer) {
			if (sender.canUseCommand(2, "save")) {
				if (args.length < 1) {
					sender.sendMessage(new TextComponentString(TextFormatting.RED + "Please add a filename, " + getUsage(sender)));
				} else {
					String name = "";
					String spacer = " ";
					for (int i = 0; i < args.length; i++) {
						if (i == args.length - 1) {
							spacer = "";
						}
						name = name.concat(args[i] + spacer);
					}
					CommonProxy.NETWORK.sendToAll(new SaveTASPacket(name));
				}
			} else {
				sender.sendMessage(new TextComponentString(TextFormatting.RED + "You have no permission to use this command"));
			}
		}
	}

	@Override
	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, BlockPos targetPos) {
		List<String> tab;
		if (args.length == 1) {
			if (!check) {
				sender.sendMessage(new TextComponentString(TextFormatting.BOLD + "" + TextFormatting.RED + "WARNING!" + TextFormatting.RESET + TextFormatting.RED + " Existing filenames will be overwritten! /fail to abort the recording if you accidentally started one"));
				check = true;
			}
			tab = getFilenames();
			if (tab.isEmpty()) {
				sender.sendMessage(new TextComponentString(TextFormatting.RED + "No files in directory"));
				return super.getTabCompletions(server, sender, args, targetPos);
			}
			return getListOfStringsMatchingLastWord(args, tab);
		} else
			return super.getTabCompletions(server, sender, args, targetPos);
	}

	public List<String> getFilenames() {
		List<String> tab = new ArrayList<String>();
		File folder = new File(Minecraft.getMinecraft().mcDataDir, "saves" + File.separator + "tasfiles");
		File[] listOfFiles = folder.listFiles();
		for (int i = 0; i < listOfFiles.length; i++) {
			tab.add(listOfFiles[i].getName().replaceAll("\\.tas", ""));
		}
		return tab;
	}

	@Override
	public int getRequiredPermissionLevel() {
		return 2;
	}
}
