package com.minecrafttas.tasmod.commands;

import com.minecrafttas.tasmod.TASmod;
import com.minecrafttas.tasmod.networking.TASmodBufferBuilder;
import com.minecrafttas.tasmod.registries.TASmodPackets;
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
import java.util.List;
import java.util.concurrent.TimeoutException;

public class CommandSaveTAS {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("save")
                .executes(CommandSaveTAS::execute)
                .then(Commands.argument("filename", StringArgumentType.string())
                    .suggests((context, builder) -> {
                        CommandSourceStack source = context.getSource();
                        if (source.getEntity() instanceof Player player) {
                            try {
                                List<String> files = TASmod.tabCompletionUtils.getTASfileList(player.getName().getString());
                                for (String file : files) {
                                    builder.suggest(file);
                                }
                            } catch (TimeoutException e) {
                                TASmod.LOGGER.catching(e);
                            } catch (Exception e) {
                                TASmod.LOGGER.catching(e);
                            }
                        }
                        return builder.buildFuture();
                    })
                    .executes(CommandSaveTAS::execute)
                    .then(Commands.argument("flavor", StringArgumentType.string())
                        .suggests((context, builder) -> {
                            CommandSourceStack source = context.getSource();
                            if (source.getEntity() instanceof Player player) {
                                try {
                                    List<String> flavors = TASmod.tabCompletionUtils.getFlavorList(player.getName().getString());
                                    for (String flavor : flavors) {
                                        builder.suggest(flavor);
                                    }
                                } catch (TimeoutException e) {
                                    TASmod.LOGGER.catching(e);
                                } catch (Exception e) {
                                    TASmod.LOGGER.catching(e);
                                }
                            }
                            return builder.buildFuture();
                        })
                        .executes(CommandSaveTAS::executeWithFlavor)
                    )
                )
        );
    }

    private static int execute(CommandContext<CommandSourceStack> context) {
        return executeImpl(context, "");
    }

    private static int executeWithFlavor(CommandContext<CommandSourceStack> context) {
        String flavor = StringArgumentType.getString(context, "flavor");
        return executeImpl(context, flavor);
    }

    private static int executeImpl(CommandContext<CommandSourceStack> context, String flavor) {
        CommandSourceStack source = context.getSource();
        if (!checkPermission(source, 2)) {
            source.sendFailure(Component.literal("You don't have permission to use this command").withStyle(ChatFormatting.RED));
            return 0;
        }
        if (!(source.getEntity() instanceof Player)) {
            source.sendFailure(Component.literal("Only players can use this command").withStyle(ChatFormatting.RED));
            return 0;
        }

        String filename = StringArgumentType.getString(context, "filename");
        try {
            TASmod.server.sendToAll(new TASmodBufferBuilder(TASmodPackets.PLAYBACK_SAVE).writeString(filename).writeString(flavor));
            source.sendSuccess(() -> Component.literal("Saved TAS: " + filename).withStyle(ChatFormatting.GREEN), false);
        } catch (Exception e) {
            TASmod.LOGGER.error("Failed to save TAS", e);
            source.sendFailure(Component.literal("Failed to save TAS: " + e.getMessage()).withStyle(ChatFormatting.RED));
        }
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