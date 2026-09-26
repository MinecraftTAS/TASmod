package com.minecrafttas.tasmod.commands;

import com.minecrafttas.tasmod.TASmod;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.world.entity.player.Player;

public class CommandClearInputs {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("clearinputs")
                .executes(CommandClearInputs::execute)
        );
    }

    private static int execute(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        // Permission check - use reflection or try different method names
        try {
            java.lang.reflect.Method method = source.getClass().getMethod("hasPermission", int.class);
            if (!((Boolean) method.invoke(source, 2))) {
                source.sendFailure(Component.literal("You don't have permission to use this command").withStyle(ChatFormatting.RED));
                return 0;
            }
        } catch (Exception e) {
            // Fallback: try getPermission() >= 2
            try {
                java.lang.reflect.Method method = source.getClass().getMethod("getPermission");
                if (!((Integer) method.invoke(source) >= 2)) {
                    source.sendFailure(Component.literal("You don't have permission to use this command").withStyle(ChatFormatting.RED));
                    return 0;
                }
            } catch (Exception ex) {
                // Skip permission check if methods not found
            }
        }
        if (!(source.getEntity() instanceof Player)) {
            source.sendFailure(Component.literal("Only players can use this command").withStyle(ChatFormatting.RED));
            return 0;
        }

        TASmod.playbackControllerServer.clearInputs();
        source.sendSuccess(() -> Component.literal("Cleared inputs").withStyle(ChatFormatting.GREEN), false);
        return 1;
    }
}