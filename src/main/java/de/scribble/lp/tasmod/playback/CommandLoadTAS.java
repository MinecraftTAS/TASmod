package de.scribble.lp.tasmod.playback;

import java.io.File;
import java.io.IOException;

import de.scribble.lp.tasmod.ClientProxy;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

public class CommandLoadTAS extends CommandBase {

	@Override
	public String getName() {
		return "load";
	}

	@Override
	public String getUsage(ICommandSender sender) {
		// TODO Auto-generated method stub
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

}
