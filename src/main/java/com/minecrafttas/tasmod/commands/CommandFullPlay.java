package com.minecrafttas.tasmod.commands;

import com.minecrafttas.tasmod.TASmod;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

import java.lang.reflect.Method;

public class CommandFullPlay {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("fullplay")
                .executes(CommandFullPlay::execute)
        );
    }

    private static int execute(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        if (!checkPermission(source, 2)) {
            source.sendFailure(Component.literal("You don't have permission to use this command").withStyle(ChatFormatting.RED));
            return 0;
        }
        TASmod.playbackControllerServer.fullPlay();
        context.getSource().sendSuccess(() -> Component.literal("Full play started").withStyle(ChatFormatting.GREEN), false);
        return 1;
    }

    private static boolean checkPermission(CommandSourceStack source, int level) {
        try {
            Method method = source.getClass().getMethod("hasPermission", int.class);
            return (Boolean) method.invoke(source, level);
        } catch (Exception e) {
            try {
                Method method = source.getClass().getMethod("getPermission");
                return (Integer) method.invoke(source) >= level;
            } catch (Exception ex) {
                return true; // Skip permission check if methods not found
            }
        }
    }
}