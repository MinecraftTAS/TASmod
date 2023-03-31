package com.minecrafttas.tasmod.commands.fullrecord;

import com.minecrafttas.tasmod.CommonProxy;
import com.minecrafttas.tasmod.TASmod;
import com.minecrafttas.tasmod.inputcontainer.TASstate;
import com.minecrafttas.tasmod.savestates.server.SavestateState;
import com.minecrafttas.tasmod.savestates.server.exceptions.SavestateException;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

public class CommandFullRecord extends CommandBase {

	@Override
	public String getName() {
		return "fullrecord";
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return "/fullrecord";
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		try {
			TASmod.savestateHandler.saveState(0, false);
		} catch (SavestateException e) {
			sender.sendMessage(new TextComponentString(TextFormatting.RED + "Failed to create a savestate: " + e.getMessage()));
			return;
		} catch (Exception e) {
			e.printStackTrace();
			sender.sendMessage(new TextComponentString(TextFormatting.RED + "Failed to create a savestate: " + e.getCause().toString()));
			return;
		} finally {
			TASmod.savestateHandler.state = SavestateState.NONE;
		}
		TASmod.containerStateServer.setServerState(TASstate.RECORDING);
		CommonProxy.NETWORK.sendToAll(new FullRecordPacket());
	}

}
