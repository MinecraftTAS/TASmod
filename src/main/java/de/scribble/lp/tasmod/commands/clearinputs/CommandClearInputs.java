package de.scribble.lp.tasmod.commands.clearinputs;

import de.scribble.lp.tasmod.CommonProxy;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;

public class CommandClearInputs extends CommandBase{

	@Override
	public String getName() {
		return "clearinputs";
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return "/clearinputs";
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		if(sender instanceof EntityPlayer) {
			CommonProxy.NETWORK.sendToAll(new ClearInputsPacket());
		}
	}
	@Override
	public int getRequiredPermissionLevel() {
		return 2;
	}

}
