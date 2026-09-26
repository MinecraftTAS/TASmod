package com.minecrafttas.tasmod.mctcommon;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;

public class CommandRegistry {

	public static void registerServerCommand(CommandDispatcher<CommandSourceStack> dispatcher, MinecraftServer server) {
		// Command registration is now handled via Fabric's CommandRegistrationCallback
		// This method is kept for compatibility but does nothing
	}

}
