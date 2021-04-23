package de.scribble.lp.tasmod.recording;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.mojang.realmsclient.gui.ChatFormatting;

import de.scribble.lp.tasmod.ClientProxy;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;

public class CommandSaveTAS extends CommandBase {

	@Override
	public String getName() {
		return "savetas";
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return "/savetas <filename>";
	}

	@Override
	public List<String> getAliases() {
		return ImmutableList.of("r", "rec");
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		if (sender instanceof EntityPlayer) {
			try {
				ClientProxy.serialiser.saveToFileV1(new File(ClientProxy.tasdirectory + "/test.tas"), ClientProxy.virtual.getContainer());
			} catch (FileNotFoundException e) {
				sender.sendMessage(new TextComponentString(ChatFormatting.RED + e.getMessage()));
			}
		}
	}

}
