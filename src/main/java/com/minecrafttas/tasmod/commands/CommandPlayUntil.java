package com.minecrafttas.tasmod.commands;

import com.minecrafttas.tasmod.TASmod;
import com.minecrafttas.tasmod.networking.TASmodBufferBuilder;
import com.minecrafttas.tasmod.registries.TASmodPackets;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

import java.lang.reflect.Method;

public class CommandPlayUntil {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("playuntil")
                .then(Commands.argument("ticks", IntegerArgumentType.integer(0))
                    .executes(CommandPlayUntil::execute)
                )
                .executes(CommandPlayUntil::showUsage)
        );
    }

    private static int execute(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        if (!checkPermission(source, 2)) {
            source.sendFailure(Component.literal("You don't have permission to use this command").withStyle(ChatFormatting.RED));
            return 0;
        }
        int ticks = IntegerArgumentType.getInteger(context, "ticks");
        try {
            TASmod.server.sendToAll(new TASmodBufferBuilder(TASmodPackets.PLAYBACK_PLAYUNTIL).writeInt(ticks));
            context.getSource().sendSuccess(() -> Component.literal("Play until tick: " + ticks).withStyle(ChatFormatting.GREEN), false);
        } catch (Exception e) {
            TASmod.LOGGER.error("Failed to send playuntil packet", e);
            context.getSource().sendFailure(Component.literal("Failed to send playuntil packet: " + e.getMessage()).withStyle(ChatFormatting.RED));
        }
        return 1;
    }

    private static int showUsage(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        if (!checkPermission(source, 2)) {
            source.sendFailure(Component.literal("You don't have permission to use this command").withStyle(ChatFormatting.RED));
            return 0;
        }
        context.getSource().sendFailure(Component.literal("Stops the next playback one tick before the specified tick and lets you record from there:\n\n/playuntil 10, runs the playback until tick 9 and will record from there. Useful when you can't savestate").withStyle(ChatFormatting.RED));
        return 0;
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