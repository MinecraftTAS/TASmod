package com.minecrafttas.tasmod.commands;

import com.minecrafttas.tasmod.TASmod;
import com.minecrafttas.tasmod.savestates.handlers.SavestateTempHandler;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.world.entity.player.Player;

import java.lang.reflect.Method;

public class CommandRecord {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("record")
                .executes(CommandRecord::execute)
                .then(Commands.literal("nosave")
                    .executes(CommandRecord::executeNoSave)
                )
        );
    }

    private static int execute(CommandContext<CommandSourceStack> context) {
        return executeImpl(context, false);
    }

    private static int executeNoSave(CommandContext<CommandSourceStack> context) {
        return executeImpl(context, true);
    }

    private static int executeImpl(CommandContext<CommandSourceStack> context, boolean noSave) {
        CommandSourceStack source = context.getSource();
        if (!checkPermission(source, 2)) {
            source.sendFailure(Component.literal("You don't have permission to use this command").withStyle(ChatFormatting.RED));
            return 0;
        }
        if (!(source.getEntity() instanceof Player)) {
            source.sendFailure(Component.literal("Only players can use this command").withStyle(ChatFormatting.RED));
            return 0;
        }

        SavestateTempHandler tempSavestateHandler = TASmod.savestateHandlerServer.getSavestateTemporaryHandler();
        tempSavestateHandler.setActive(true);
        tempSavestateHandler.setActive(!noSave);
        TASmod.playbackControllerServer.toggleRecording();

        source.sendSuccess(() -> Component.literal("Recording " + (noSave ? "without saving" : "started")).withStyle(ChatFormatting.GREEN), false);
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