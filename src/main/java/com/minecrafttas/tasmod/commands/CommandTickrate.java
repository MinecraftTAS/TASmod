package com.minecrafttas.tasmod.commands;

import com.minecrafttas.tasmod.TASmod;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

import java.lang.reflect.Method;

public class CommandTickrate {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("tickrate")
                .executes(CommandTickrate::showTickrate)
                .then(Commands.argument("ticksPerSecond", FloatArgumentType.floatArg(0.01f))
                    .executes(CommandTickrate::setTickrate)
                )
        );
    }

    private static int showTickrate(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        if (!checkPermission(source, 2)) {
            source.sendFailure(Component.literal("You don't have permission to use this command").withStyle(ChatFormatting.RED));
            return 0;
        }
        source.sendSuccess(() -> Component.literal("Current tickrate: " + TASmod.tickratechanger.ticksPerSecond).withStyle(ChatFormatting.GREEN), false);
        return 1;
    }

    private static int setTickrate(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        float tickrate = FloatArgumentType.getFloat(context, "ticksPerSecond");
        TASmod.tickratechanger.changeTickrate(tickrate);
        context.getSource().sendSuccess(() -> Component.literal("Set tickrate to " + tickrate).withStyle(ChatFormatting.GREEN), false);
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
                return true;
            }
        }
    }
}