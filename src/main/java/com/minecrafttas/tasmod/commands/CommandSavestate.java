package com.minecrafttas.tasmod.commands;

import com.minecrafttas.tasmod.TASmod;
import com.minecrafttas.tasmod.networking.TASmodBufferBuilder;
import com.minecrafttas.tasmod.registries.TASmodPackets;
import com.minecrafttas.tasmod.savestates.SavestateHandlerServer.SavestateCallback;
import com.minecrafttas.tasmod.savestates.SavestateIndexer.ErrorRunnable;
import com.minecrafttas.tasmod.savestates.SavestateIndexer.FailedSavestate;
import com.minecrafttas.tasmod.savestates.SavestateIndexer.Savestate;
import com.minecrafttas.tasmod.savestates.exceptions.LoadstateException;
import com.minecrafttas.tasmod.savestates.exceptions.SavestateDeleteException;
import com.minecrafttas.tasmod.savestates.exceptions.SavestateException;
import com.minecrafttas.tasmod.util.TASComponent;
import com.minecrafttas.tasmod.util.TASComponent.CClickEvent;
import com.minecrafttas.tasmod.util.TASComponent.CHoverEvent;
import com.minecrafttas.tasmod.util.I18n;
import com.minecrafttas.tasmod.util.LoggerMarkers;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.ChatFormatting;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

import java.lang.reflect.Method;

public class CommandSavestate {

    private static final SimpleCommandExceptionType USAGE_EXCEPTION = new SimpleCommandExceptionType(Component.literal("/savestate save|load|delete|reload|rename|info"));
    private static boolean once = true;

    private static boolean checkPermission(CommandSourceStack source, int level) {
        try {
            Method method = CommandSourceStack.class.getMethod("hasPermission", int.class);
            return (boolean) method.invoke(source, level);
        } catch (Exception e) {
            return false;
        }
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal("savestate")
            .executes(context -> {
                CommandSourceStack source = context.getSource();
                if (!checkPermission(source, 2)) {
                    source.sendFailure(Component.literal("You don't have permission to use this command").withStyle(ChatFormatting.RED));
                    return 0;
                }
                return info(context);
            });

        // Info subcommand
        root.then(Commands.literal("info")
            .executes(context -> info(context))
            .then(Commands.argument("index", IntegerArgumentType.integer())
                .executes(ctx -> infoIndex(ctx, IntegerArgumentType.getInteger(ctx, "index")))
                .then(Commands.argument("amount", IntegerArgumentType.integer())
                    .executes(ctx -> infoIndexAmount(ctx, IntegerArgumentType.getInteger(ctx, "index"), IntegerArgumentType.getInteger(ctx, "amount")))
                )
            )
            .then(Commands.literal("all")
                .executes(context -> infoAll(context))
            )
        );

        // Save subcommand
        root.then(Commands.literal("save")
            .executes(CommandSavestate::saveNew)
            .then(Commands.argument("index", IntegerArgumentType.integer())
                .executes(ctx -> saveIndex(ctx, IntegerArgumentType.getInteger(ctx, "index")))
                .then(Commands.argument("name", StringArgumentType.greedyString())
                    .executes(ctx -> saveIndexName(ctx, IntegerArgumentType.getInteger(ctx, "index"), StringArgumentType.getString(ctx, "name")))
                )
            )
            .then(Commands.argument("name", StringArgumentType.greedyString())
                .executes(ctx -> saveName(ctx, StringArgumentType.getString(ctx, "name")))
            )
        );

        // Load subcommand
        root.then(Commands.literal("load")
            .executes(CommandSavestate::loadRecent)
            .then(Commands.argument("index", IntegerArgumentType.integer())
                .executes(ctx -> loadIndex(ctx, IntegerArgumentType.getInteger(ctx, "index")))
            )
        );

        // Delete subcommand
        root.then(Commands.literal("delete")
            .then(Commands.argument("indexFrom", IntegerArgumentType.integer())
                .executes(ctx -> delete(ctx, IntegerArgumentType.getInteger(ctx, "indexFrom")))
                .then(Commands.argument("indexTo", IntegerArgumentType.integer())
                    .executes(ctx -> deleteMore(ctx, IntegerArgumentType.getInteger(ctx, "indexFrom"), IntegerArgumentType.getInteger(ctx, "indexTo")))
                    .then(Commands.literal("force")
                        .executes(ctx -> deleteDis(ctx, IntegerArgumentType.getInteger(ctx, "indexFrom"), IntegerArgumentType.getInteger(ctx, "indexTo")))
                    )
                )
            )
        );

        // Reload subcommand
        root.then(Commands.literal("reload")
            .executes(CommandSavestate::reload)
        );

        // Rename subcommand
        root.then(Commands.literal("rename")
            .then(Commands.argument("index", IntegerArgumentType.integer())
                .then(Commands.argument("name", StringArgumentType.greedyString())
                    .executes(ctx -> rename(ctx, IntegerArgumentType.getInteger(ctx, "index"), StringArgumentType.getString(ctx, "name")))
                )
            )
        );

        // Default (no subcommand) - show info
        root.executes(CommandSavestate::info);

        dispatcher.register(root);
    }

    private static int info(CommandContext<CommandSourceStack> context) {
        infoIndexAmount(context.getSource(), null, null);
        return 1;
    }

    private static int infoIndex(CommandContext<CommandSourceStack> context, Integer index) {
        infoIndexAmount(context.getSource(), index, null);
        return 1;
    }

    private static int infoIndexAmount(CommandContext<CommandSourceStack> context, Integer indexToDisplay, Integer amount) {
        return infoIndexAmount(context.getSource(), indexToDisplay, amount);
    }

    private static int infoIndexAmount(CommandSourceStack sender, Integer indexToDisplay, Integer amount) {
        TASmod.LOGGER.trace(LoggerMarkers.Savestate, "Command InfoIndexAmount {}|{}", indexToDisplay, amount);

        int currentIndex = TASmod.savestateHandlerServer.getCurrentIndex();
        int size = TASmod.savestateHandlerServer.size();

        if (size == 0) {
            sendHelp(sender);
            return 1;
        }

        if (indexToDisplay == null) {
            indexToDisplay = currentIndex;
        }
        if (amount == null) {
            amount = 10;
        }

        sender.sendSystemMessage(TASComponent.literal("").build());

        String format = "MM/dd/yyyy hh:mm:ss a";
        if (!sender.getServer().isDedicatedServer()) {
            format = I18n.format("msg.tasmod.savestate.dateformat");
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat(format);

        List<Savestate> savestateList = TASmod.savestateHandlerServer.getSavestateInfo(indexToDisplay, amount);

        if (savestateList.size() < size && once) {
            sender.sendSystemMessage(TASComponent.translatable("msg.tasmod.savestate.omitted", "/savestate info all").withStyle(ChatFormatting.RED, ChatFormatting.ITALIC).build());
            once = false;
        }

        for (Savestate savestate : savestateList) {
            String index = savestate.getIndex() == null ? "" : Integer.toString(savestate.getIndex());
            boolean isCurrentIndex = savestate.getIndex() == currentIndex;
            String name = savestate.getName() == null ? "" : savestate.getName();
            String date = savestate.getDate() == null ? "" : dateFormat.format(savestate.getDate());

            ChatFormatting indexColor = isCurrentIndex ? ChatFormatting.AQUA : ChatFormatting.BLUE;
            ChatFormatting nameColor = isCurrentIndex ? ChatFormatting.WHITE : ChatFormatting.GRAY;
            ChatFormatting dateColor = isCurrentIndex ? ChatFormatting.AQUA : ChatFormatting.DARK_AQUA;
            ChatFormatting saveColor = isCurrentIndex ? ChatFormatting.LIGHT_PURPLE : ChatFormatting.DARK_PURPLE;
            ChatFormatting deleteColor = isCurrentIndex ? ChatFormatting.RED : ChatFormatting.DARK_RED;
            ChatFormatting renameColor = isCurrentIndex ? ChatFormatting.YELLOW : ChatFormatting.GOLD;
            ChatFormatting loadColor = isCurrentIndex ? ChatFormatting.GREEN : ChatFormatting.DARK_GREEN;

            UnaryOperator<Style> hover = t -> 
                t.withHoverEvent(CHoverEvent.create(HoverEvent.Action.SHOW_TEXT, TASComponent.literal(date).withStyle(dateColor)));

            TASComponent msg = null;

            if (savestate instanceof FailedSavestate) {
                FailedSavestate failedSavestate = (FailedSavestate) savestate;
                msg = TASComponent.translatable("%s: %s%s",
                        TASComponent.literal(index).withStyle(indexColor), 
                        TASComponent.literal(name).withStyle(nameColor),
                        TASComponent.translatable("msg.tasmod.savestate.info.error", failedSavestate.getError().getMessage())
                    .withStyle(ChatFormatting.RED))
                    .withStyle(t -> 
                        t.withHoverEvent(
                                CHoverEvent.create(HoverEvent.Action.SHOW_TEXT, TASComponent.literal(date).withStyle(ChatFormatting.GOLD)
                        )));
            } else {
                if (false) { // TODO: Add server config
                    msg = TASComponent.translatable("%s: %s", 
                            TASComponent.literal(index).withStyle(indexColor), 
                            TASComponent.literal(name).withStyle(nameColor))
                            .withStyle(hover);
                } else {
                    TASComponent saveComponent = TASComponent.translatable("msg.tasmod.savestate.save.clickable").withStyle(saveColor)
                            .withStyle(t->
                            t.withHoverEvent(
                                    CHoverEvent.create(HoverEvent.Action.SHOW_TEXT, TASComponent.translatable("msg.tasmod.savestate.save.hover", name).withStyle(saveColor)))
                            )
                            .withStyle(t->
                                t.withClickEvent(
                                        CClickEvent.create(ClickEvent.Action.SUGGEST_COMMAND, String.format("/savestate save %s", index)))
                            );

                    TASComponent deleteComponent = TASComponent.translatable("msg.tasmod.savestate.delete.clickable").withStyle(deleteColor)
                            .withStyle(t->
                                t.withClickEvent(CClickEvent.create(ClickEvent.Action.SUGGEST_COMMAND, String.format("/savestate delete %s", index)))
                            )
                            .withStyle(t->
                                t.withHoverEvent(CHoverEvent.create(HoverEvent.Action.SHOW_TEXT, TASComponent.translatable("msg.tasmod.savestate.delete.hover", name).withStyle(deleteColor)))
                            );

                    TASComponent renameComponent = TASComponent.translatable("msg.tasmod.savestate.rename.clickable").withStyle(renameColor)
                            .withStyle(t->
                                t.withClickEvent(CClickEvent.create(ClickEvent.Action.SUGGEST_COMMAND, String.format("/savestate rename %s", index)))
                            )
                            .withStyle(t->
                                t.withHoverEvent(CHoverEvent.create(HoverEvent.Action.SHOW_TEXT, TASComponent.translatable("msg.tasmod.savestate.rename.hover", name).withStyle(renameColor)))
                            );

                    TASComponent loadComponent = TASComponent.translatable("msg.tasmod.savestate.load.clickable").withStyle(loadColor)
                            .withStyle(t->
                                t.withClickEvent(CClickEvent.create(ClickEvent.Action.SUGGEST_COMMAND, String.format("/savestate load %s", index)))
                            )
                            .withStyle(t->
                                t.withHoverEvent(CHoverEvent.create(HoverEvent.Action.SHOW_TEXT, TASComponent.translatable("msg.tasmod.savestate.load.hover", name).withStyle(loadColor)))
                            );

                    msg = TASComponent.translatable("%s: %s     %s %s %s %s",
                            TASComponent.literal(index).withStyle(indexColor), 
                            TASComponent.literal(name).withStyle(nameColor),
                            TASComponent.wrap(saveComponent, nameColor),
                            TASComponent.wrap(deleteComponent, nameColor),
                            TASComponent.wrap(renameComponent, nameColor),
                            TASComponent.wrap(loadComponent, nameColor)
                        ).withStyle(hover);
                }
            }

            sender.sendSystemMessage(msg.build());
        }
        return 1;
    }

    private static void sendHelp(CommandSourceStack sender) {
        UnaryOperator<Style> hover = t -> t.withHoverEvent(CHoverEvent.create(HoverEvent.Action.SHOW_TEXT, TASComponent.translatable("Click me!").withStyle(ChatFormatting.AQUA)));
        UnaryOperator<Style> click;

        sender.sendSystemMessage(TASComponent.translatable("You currently do not have any savestates!").withStyle(ChatFormatting.RED).build());
        sender.sendSystemMessage(TASComponent.literal("").build());
        click = t -> t.withClickEvent(CClickEvent.create(ClickEvent.Action.SUGGEST_COMMAND, "/savestate save My first savestate!"));
        sender.sendSystemMessage(TASComponent.translatable("Use %s to create one", TASComponent.literal("/savestate save [name]").withStyle(hover).withStyle(click).withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD)).withStyle(ChatFormatting.YELLOW).build());
        click = t -> t.withClickEvent(CClickEvent.create(ClickEvent.Action.SUGGEST_COMMAND, "/savestate load"));
        sender.sendSystemMessage(TASComponent.translatable("then use %s to load it.", TASComponent.literal("/savestate load").withStyle(hover).withStyle(click).withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD)).withStyle(ChatFormatting.YELLOW).build());
        sender.sendSystemMessage(TASComponent.translatable("This can also be done with the hotkeys J and K respectively.").withStyle(ChatFormatting.YELLOW).build());
        click = t -> t.withClickEvent(CClickEvent.create(ClickEvent.Action.SUGGEST_COMMAND, "/savestate"));
        sender.sendSystemMessage(TASComponent.translatable("Running %s will display all savestates", TASComponent.literal("/savestate").withStyle(hover).withStyle(click).withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD)).withStyle(ChatFormatting.YELLOW).build());
        sender.sendSystemMessage(TASComponent.translatable("(You can click on the %s!)", TASComponent.translatable("commands").withStyle(ChatFormatting.AQUA)).withStyle(ChatFormatting.GREEN).build());
    }

    private static int infoAll(CommandContext<CommandSourceStack> context) {
        infoIndexAmount(context.getSource(), -1, 0);
        return 1;
    }

    private static int saveNew(CommandContext<CommandSourceStack> context) {
        CommandSourceStack sender = context.getSource();
        TASmod.LOGGER.trace(LoggerMarkers.Savestate, "Command SaveNew");

        SavestateCallback cb = createChatMessageCallback(sender, "msg.tasmod.savestate.save.end");

        TASmod.gameLoopSchedulerServer.add(() -> {
            try {
                TASmod.savestateHandlerServer.saveState(cb);
            } catch (SavestateException e) {
                onFailure(sender, e);
                try {
                    TASmod.server.sendToAll(new TASmodBufferBuilder(TASmodPackets.TICKRATE_0_WARN));
                } catch (Exception e1) {
                    TASmod.LOGGER.catching(e);
                }
            }
        });
        return 1;
    }

    private static int saveIndex(CommandContext<CommandSourceStack> context, int index) {
        CommandSourceStack sender = context.getSource();
        TASmod.LOGGER.trace(LoggerMarkers.Savestate, "Command SaveIndex {}", index);

        SavestateCallback cb = createChatMessageCallback(sender, "msg.tasmod.savestate.save.end");

        if (index == 0) {
            onFailure(sender, new SavestateException("msg.tasmod.savestate.save.error.zero"));
            return 0;
        } else if (index < 0) {
            sender.sendSystemMessage(TASComponent.translatable("msg.tasmod.savestate.save.negative").withStyle(ChatFormatting.YELLOW).build());
        }

        TASmod.gameLoopSchedulerServer.add(() -> {
            try {
                TASmod.savestateHandlerServer.saveState(index, cb);
            } catch (SavestateException e) {
                onFailure(sender, e);
                try {
                    TASmod.server.sendToAll(new TASmodBufferBuilder(TASmodPackets.TICKRATE_0_WARN));
                } catch (Exception e1) {
                    TASmod.LOGGER.catching(e);
                }
            }
        });
        return 1;
    }

    private static int saveIndexName(CommandContext<CommandSourceStack> context, int index, String name) {
        CommandSourceStack sender = context.getSource();
        TASmod.LOGGER.trace(LoggerMarkers.Savestate, "Command SaveNameIndex {}|{}", index, name);

        if (index == 0) {
            onFailure(sender, new SavestateException("msg.tasmod.savestate.save.error.zero"));
            return 0;
        } else if (index < 0) {
            sender.sendSystemMessage(TASComponent.translatable("msg.tasmod.savestate.save.negative").withStyle(ChatFormatting.YELLOW).build());
        }

        SavestateCallback cb = createChatMessageCallback(sender, "msg.tasmod.savestate.save.end");

        TASmod.gameLoopSchedulerServer.add(() -> {
            try {
                TASmod.savestateHandlerServer.saveState(index, name, cb);
            } catch (SavestateException e) {
                onFailure(sender, e);
                try {
                    TASmod.server.sendToAll(new TASmodBufferBuilder(TASmodPackets.TICKRATE_0_WARN));
                } catch (Exception e1) {
                    TASmod.LOGGER.catching(e);
                }
            }
        });
        return 1;
    }

    private static int saveName(CommandContext<CommandSourceStack> context, String name) {
        CommandSourceStack sender = context.getSource();
        TASmod.LOGGER.trace(LoggerMarkers.Savestate, "Command SaveName {}", name);

        SavestateCallback cb = createChatMessageCallback(sender, "msg.tasmod.savestate.save.end");

        TASmod.gameLoopSchedulerServer.add(() -> {
            try {
                TASmod.savestateHandlerServer.saveState(name, cb);
            } catch (SavestateException e) {
                onFailure(sender, e);
                try {
                    TASmod.server.sendToAll(new TASmodBufferBuilder(TASmodPackets.TICKRATE_0_WARN));
                } catch (Exception e1) {
                    TASmod.LOGGER.catching(e);
                }
            }
        });
        return 1;
    }

    private static int loadRecent(CommandContext<CommandSourceStack> context) {
        CommandSourceStack sender = context.getSource();
        TASmod.LOGGER.trace(LoggerMarkers.Savestate, "Command LoadRecent");

        SavestateCallback cb = createChatMessageCallback(sender, "msg.tasmod.savestate.load.end");

        TASmod.gameLoopSchedulerServer.add(() -> {
            try {
                TASmod.savestateHandlerServer.loadState(cb);
            } catch (LoadstateException e) {
                onFailure(sender, e);
                try {
                    TASmod.server.sendToAll(new TASmodBufferBuilder(TASmodPackets.TICKRATE_0_WARN));
                } catch (Exception e1) {
                    TASmod.LOGGER.catching(e);
                }
            }
        });
        return 1;
    }

    private static int loadIndex(CommandContext<CommandSourceStack> context, int index) {
        CommandSourceStack sender = context.getSource();
        TASmod.LOGGER.trace(LoggerMarkers.Savestate, "Command LoadIndex {}", index);

        if (index < 0) {
            sender.sendSystemMessage(TASComponent.translatable("msg.tasmod.savestate.load.negative").withStyle(ChatFormatting.YELLOW).build());
        }

        SavestateCallback cb = createChatMessageCallback(sender, "msg.tasmod.savestate.load.end");

        TASmod.gameLoopSchedulerServer.add(() -> {
            try {
                TASmod.savestateHandlerServer.loadState(index, cb);
            } catch (LoadstateException e) {
                onFailure(sender, e);
                try {
                    TASmod.server.sendToAll(new TASmodBufferBuilder(TASmodPackets.TICKRATE_0_WARN));
                } catch (Exception e1) {
                    TASmod.LOGGER.catching(e);
                }
            }
        });
        return 1;
    }

    private static int delete(CommandContext<CommandSourceStack> context, int index) {
        CommandSourceStack sender = context.getSource();
        TASmod.LOGGER.trace(LoggerMarkers.Savestate, "Command Delete {}", index);

SavestateCallback cb = (paths) -> {
            sender.getServer().getPlayerList().broadcastSystemMessage(
                    TASComponent.translatable("msg.tasmod.savestate.delete", 
                            TASComponent.literal(Integer.toString(paths.getSavestate().getIndex()))
                                .withStyle(ChatFormatting.AQUA)
                        ).withStyle(ChatFormatting.GREEN).build(), false);
        };

        try {
            TASmod.savestateHandlerServer.deleteSavestate(index, cb);
        } catch (SavestateDeleteException e) {
            onFailure(sender, e);
        }
        return 1;
    }

    private static int deleteMore(CommandContext<CommandSourceStack> context, int indexFrom, int indexTo) {
        CommandSourceStack sender = context.getSource();
        TASmod.LOGGER.trace(LoggerMarkers.Savestate, "Command DeleteMore {}|{}", indexFrom, indexTo);
        int count = (indexTo + 1) - indexFrom;

        if (count < 0) {
            onFailure(sender, new SavestateDeleteException("msg.tasmod.savestate.deleteMore.error.negative", count));
            return 0;
        }

        String translationKey = "msg.tasmod.savestate.deleteMore" + (count == 1 ? ".singular" : ".plural");

        TASComponent countComponent = TASComponent.literal(Integer.toString(count)).withStyle(ChatFormatting.RED);
        
        TASComponent confirmationComponent = TASComponent.wrap(TASComponent.translatable("msg.tasmod.savestate.deleteMore.clickable", true)
                .withStyle(
                        style -> style
                            .withClickEvent(
                                    CClickEvent.create(ClickEvent.Action.RUN_COMMAND, String.format("/savestate delete %s %s force", indexFrom, indexTo))
                            )
                            .withHoverEvent(
                                    CHoverEvent.create(HoverEvent.Action.SHOW_TEXT, TASComponent.translatable("msg.tasmod.savestate.deleteMore.hover").withStyle(ChatFormatting.DARK_RED)))
                    )).withStyle(ChatFormatting.GREEN);

        sender.sendSystemMessage(
            TASComponent.translatable(translationKey, countComponent, confirmationComponent).withStyle(ChatFormatting.YELLOW).build()
        );
        return 1;
    }

    private static int deleteDis(CommandContext<CommandSourceStack> context, int indexFrom, int indexTo) {
        CommandSourceStack sender = context.getSource();
        TASmod.LOGGER.trace(LoggerMarkers.Savestate, "Command DeleteDis {}|{}", indexFrom, indexTo);

        SavestateCallback cb = (paths) -> {
            sender.getServer().getPlayerList().broadcastSystemMessage(TASComponent.translatable("msg.tasmod.savestate.delete", paths.getSavestate().getIndex()).withStyle(ChatFormatting.GREEN).build(), false);
        };

        ErrorRunnable onErr = (exception) -> {
            onFailure(sender, exception);
        };

        try {
            TASmod.savestateHandlerServer.deleteSavestate(indexFrom, indexTo, cb, onErr);
        } catch (SavestateDeleteException e) {
            onFailure(sender, e);
        }
        return 1;
    }

    private static int reload(CommandContext<CommandSourceStack> context) {
        CommandSourceStack sender = context.getSource();
        TASmod.LOGGER.trace(LoggerMarkers.Savestate, "Command Reload");

        sender.getServer().getPlayerList().broadcastSystemMessage(TASComponent.translatable("msg.tasmod.savestate.reload").withStyle(ChatFormatting.GREEN).build(), false);
        TASmod.savestateHandlerServer.reload();
        return 1;
    }

    private static int rename(CommandContext<CommandSourceStack> context, int index, String name) {
        CommandSourceStack sender = context.getSource();
        TASmod.LOGGER.trace(LoggerMarkers.Savestate, "Command Rename {}|{}", index, name);

SavestateCallback cb = (paths) -> {
            sender.getServer().getPlayerList().broadcastSystemMessage(
                    TASComponent.translatable("msg.tasmod.savestate.rename", 
                            TASComponent.literal(Integer.toString(paths.getSavestate().getIndex()))
                                .withStyle(ChatFormatting.AQUA),
                            TASComponent.literal(paths.getSavestate().getName())
                                .withStyle(ChatFormatting.YELLOW)
                        )
                        .withStyle(ChatFormatting.GREEN).build(), false);
        };

        TASmod.savestateHandlerServer.rename(index, name, cb);
        return 1;
    }

    private static void onFailure(CommandSourceStack sender, Throwable e) {
        sender.getServer().getPlayerList().broadcastSystemMessage(TASComponent.translatable(e.getMessage()).withStyle(ChatFormatting.RED).build(), false);
        TASmod.LOGGER.catching(e);
        TASmod.savestateHandlerServer.resetState();
    }

    public static SavestateCallback createChatMessageCallback(CommandSourceStack sender, String translationKey) {
        return (paths) -> {
            createClearScreenCallback(sender).invoke(paths);

sender.getServer().getPlayerList().broadcastSystemMessage(
                    TASComponent.translatable(translationKey, 
                            TASComponent.literal(paths.getSavestate().getName())
                                .withStyle(ChatFormatting.YELLOW),
                            TASComponent.literal(Integer.toString(paths.getSavestate().getIndex()))
                                .withStyle(ChatFormatting.AQUA)
                        )
                        .withStyle(ChatFormatting.GREEN).build(), false);
        };
    }

    public static SavestateCallback createClearScreenCallback(CommandSourceStack sender) {
        return (paths -> {
            if (sender.getEntity() instanceof ServerPlayer) {
                try {
                    TASmod.server.sendToAll(new TASmodBufferBuilder(TASmodPackets.SAVESTATE_CLEAR_SCREEN));
                } catch (Exception e) {
                    onFailure(sender, e);
                }
            }
        });
    }
}