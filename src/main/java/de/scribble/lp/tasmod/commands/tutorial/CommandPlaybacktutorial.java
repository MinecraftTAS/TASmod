package de.scribble.lp.tasmod.commands.tutorial;

import de.scribble.lp.tasmod.ClientProxy;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;

public class CommandPlaybacktutorial extends CommandBase{

	@Override
	public String getName() {
		return "playbacktutorial";
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return "/playbacktutorial";
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		if(sender instanceof EntityPlayer) {
			if(!server.isDedicatedServer()) {
				TutorialHandler handler=ClientProxy.getPlaybackTutorial();
				if(handler.isTutorial()) {
					handler.setTutorial(false);
					ClientProxy.config.get("Tutorial","Enabled",true,"If the tutorial is enabled").set(false);
					ClientProxy.config.save();
				}else {
					handler.setState((short)1);
					handler.setTutorial(true);
				}
			}
		}
	}
}
