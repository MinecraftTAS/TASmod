package com.minecrafttas.common;

import net.minecraft.command.CommandHandler;
import net.minecraft.command.ICommand;
import net.minecraft.server.MinecraftServer;

public class CommandRegistry {
	
	public static void registerServerCommand(ICommand command, MinecraftServer server) {
		CommandHandler ch = (CommandHandler) server.getCommandManager();
		ch.registerCommand(command);
	}
	
}
