package de.scribble.lp.tasmod.misc;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.scribble.lp.tasmod.CommonProxy;
import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

public class CommandFolder extends CommandBase{

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
		if(args.length==1) {
			if(args[0].equalsIgnoreCase("savestates")) {
				File file = new File(Minecraft.getMinecraft().mcDataDir, "saves" + File.separator + "savestates");
				try {
					if(!file.exists())file.mkdir();
					Desktop.getDesktop().open(file);
				} catch (IOException e) {
					CommonProxy.logger.fatal("Something went wrong while opening ", new File(Minecraft.getMinecraft().mcDataDir, "saves" + File.separator + "savestates").getPath());
					e.printStackTrace();
				}
			}else if(args[0].equalsIgnoreCase("tasfiles")){
				File file = new File(Minecraft.getMinecraft().mcDataDir, "saves" + File.separator + "tasfiles");
				try {
					if(!file.exists())file.mkdir();
					Desktop.getDesktop().open(file);
				} catch (IOException e) {
					CommonProxy.logger.fatal("Something went wrong while opening ", new File(Minecraft.getMinecraft().mcDataDir, "saves" + File.separator + "savestates").getPath());
					e.printStackTrace();
				}
			}
		}
	}
	@Override
	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args,
			BlockPos targetPos) {
		List<String> tab=new ArrayList<String>();
		if(args.length==1) {
			tab.addAll(getListOfStringsMatchingLastWord(args, new String[] {"savestates","tasfiles"}));
		}else {
			tab.clear();
		}
		return tab;
	}

}
