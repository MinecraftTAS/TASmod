package com.minecrafttas.tasmod.commands;

import com.minecrafttas.tasmod.TASmod;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

import java.io.File;
import java.io.FileFilter;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class CommandRestartAndPlay {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("restartandplay")
                .then(Commands.argument("filename", StringArgumentType.string())
                    .suggests(CommandRestartAndPlay::suggestFilenames)
                    .executes(CommandRestartAndPlay::execute)
                )
        );
    }

    private static int execute(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        if (!checkPermission(source, 2)) {
            source.sendFailure(Component.literal("You don't have permission to use this command").withStyle(ChatFormatting.RED));
            return 0;
        }
        String filename = StringArgumentType.getString(context, "filename");
        TASmod.playbackControllerServer.restartAndPlay(filename);
        context.getSource().sendSuccess(() -> Component.literal("Restart and play: " + filename).withStyle(ChatFormatting.GREEN), false);
        return 1;
    }

    private static CompletableFuture<Suggestions> suggestFilenames(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        List<String> filenames = getFilenames();
        for (String filename : filenames) {
            builder.suggest(filename);
        }
        return builder.buildFuture();
    }

    public static List<String> getFilenames() {
        List<String> tab = new ArrayList<>();
        // This is a client-side path, but we'll provide an empty list server-side
        // The actual tab completion will be handled client-side via the networking
        return tab;
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