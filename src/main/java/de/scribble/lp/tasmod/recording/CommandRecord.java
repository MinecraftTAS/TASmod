package de.scribble.lp.tasmod.recording;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableList;

import de.scribble.lp.tasmod.CommonProxy;
import de.scribble.lp.tasmod.playback.InputPlayback;
import de.scribble.lp.tasmod.ticksync.TickSyncPackage;
import de.scribble.lp.tasmod.ticksync.TickSyncServer;
import net.minecraft.client.Minecraft;
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
		return "/record <true|false>";
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
			sender.sendMessage(new TextComponentString(TextFormatting.RED + "Too few arguments. "+getUsage(sender)));
		} else if(args.length==1) {
			if(args[0].equalsIgnoreCase("true")) {
				CommonProxy.NETWORK.sendToAll(new RecordingPacket(true));
			} else if(args[0].equalsIgnoreCase("false")) {
				CommonProxy.NETWORK.sendToAll(new RecordingPacket(false));
			} else {
				sender.sendMessage(new TextComponentString(TextFormatting.RED + "Couldn't process the argument "+args[0]+". Must be either true or false"));
			}
		} else if(args.length>1) {
			sender.sendMessage(new TextComponentString(TextFormatting.RED + "Too many arguments. "+getUsage(sender)));
		}
	}

}
