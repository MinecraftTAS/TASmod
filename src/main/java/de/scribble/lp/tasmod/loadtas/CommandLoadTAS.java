package de.scribble.lp.tasmod.loadtas;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.scribble.lp.tasmod.ClientProxy;
import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

public class CommandLoadTAS extends CommandBase {

	@Override
	public String getName() {
		return "load";
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return "/load <filename>";
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		File file=new File(ClientProxy.tasdirectory + "/test.tas");
		int version=0;
		try {
			version=ClientProxy.serialiser.getFileVersion(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
		if(version==0) {
			sender.sendMessage(new TextComponentString(TextFormatting.RED+"Couldn't find the file version. Aborting"));
		}else if(version==1) {
			try {
				ClientProxy.virtual.setContainer(ClientProxy.serialiser.fromEntireFileV1(file));
			} catch (IOException e) {
				e.printStackTrace();
			}
			ClientProxy.virtual.getContainer().fixTicks();
			ClientProxy.virtual.getContainer().setIndexToLatest();
		}
	}
	
	@Override
	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, BlockPos targetPos) {
		List<String> tab;
		if (args.length == 1) {
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
}
