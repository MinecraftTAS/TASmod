package de.scribble.lp.tasmod.commands.fullplay;

import de.scribble.lp.tasmod.CommonProxy;
import de.scribble.lp.tasmod.TASmod;
import de.scribble.lp.tasmod.savestates.server.SavestateState;
import de.scribble.lp.tasmod.savestates.server.exceptions.LoadstateException;
import de.scribble.lp.tasmod.util.TASstate;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

public class CommandFullPlay extends CommandBase{

	@Override
	public String getName() {
		return "fullplay";
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return "/fullplay";
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		try {
			TASmod.savestateHandler.loadState(0, false);
		} catch (LoadstateException e) {
			sender.sendMessage(new TextComponentString(TextFormatting.RED+"Failed to load a savestate: "+e.getMessage()));
			return;
		} catch (Exception e) {
			sender.sendMessage(new TextComponentString(TextFormatting.RED+"Failed to load a savestate: "+e.getCause().toString()));
			e.printStackTrace();
			return;
		} finally {
			TASmod.savestateHandler.state=SavestateState.NONE;
		}
		TASmod.containerStateServer.setServerState(TASstate.PLAYBACK);
		CommonProxy.NETWORK.sendToAll(new FullPlayPacket());
	}

}
