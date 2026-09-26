package com.minecrafttas.tasmod.commands;

import static com.minecrafttas.tasmod.registries.TASmodPackets.COMMAND_FILECOMMANDLIST;
import static com.minecrafttas.tasmod.registries.TASmodPackets.PLAYBACK_FILECOMMAND_ENABLE;

import com.minecrafttas.tasmod.mctcommon.networking.Client.Side;
import com.minecrafttas.tasmod.mctcommon.networking.exception.PacketNotImplementedException;
import com.minecrafttas.tasmod.mctcommon.networking.exception.WrongSideException;
import com.minecrafttas.tasmod.mctcommon.networking.interfaces.ClientPacketHandler;
import com.minecrafttas.tasmod.mctcommon.networking.interfaces.PacketID;
import com.minecrafttas.tasmod.mctcommon.networking.interfaces.ServerPacketHandler;
import com.minecrafttas.tasmod.TASmod;
import com.minecrafttas.tasmod.TASmodClient;
import com.minecrafttas.tasmod.networking.TASmodBufferBuilder;
import com.minecrafttas.tasmod.playback.filecommands.PlaybackFileCommand.PlaybackFileCommandExtension;
import com.minecrafttas.tasmod.registries.TASmodAPIRegistry;
import com.minecrafttas.tasmod.registries.TASmodPackets;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.lang.reflect.Method;

public class CommandFileCommand implements ClientPacketHandler, ServerPacketHandler {

    private static final SimpleCommandExceptionType USAGE_EXCEPTION = new SimpleCommandExceptionType(Component.literal("/filecommand <filecommandname>"));

    static CompletableFuture<List<String>> fileCommandList = null;

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("filecommand")
                .executes(CommandFileCommand::showList)
                .then(Commands.argument("name", StringArgumentType.string())
                    .suggests(CommandFileCommand::suggestFileCommands)
                    .executes(CommandFileCommand::toggleFileCommand)
                )
        );
    }

    private static int showList(CommandContext<CommandSourceStack> context) {
        CommandSourceStack sender = context.getSource();
        if (!checkPermission(sender, 2)) {
            sender.sendFailure(Component.literal("You don't have permission to use this command").withStyle(ChatFormatting.RED));
            return 0;
        }
        try {
            Map<String, Boolean> fileCommandNames = getExtensions(sender);
            sender.sendSuccess(() -> Component.literal(String.join(" ", getColoredNames(fileCommandNames))), false);
        } catch (Exception e) {
            sender.sendFailure(Component.literal(e.getMessage()).withStyle(ChatFormatting.RED));
        }
        return 1;
    }

    private static int toggleFileCommand(CommandContext<CommandSourceStack> context) {
        CommandSourceStack sender = context.getSource();
        if (!checkPermission(sender, 2)) {
            sender.sendFailure(Component.literal("You don't have permission to use this command").withStyle(ChatFormatting.RED));
            return 0;
        }
        String name = StringArgumentType.getString(context, "name");

        try {
            Map<String, Boolean> fileCommandNames = getExtensions(sender);
            Boolean enable = fileCommandNames.get(name);

            if (enable == null) {
                sender.sendFailure(Component.literal("The file command was not found: " + name).withStyle(ChatFormatting.RED));
                return 0;
            }

            TASmod.server.sendTo(sender.getPlayer().getName().getString(), new TASmodBufferBuilder(PLAYBACK_FILECOMMAND_ENABLE).writeString(name).writeBoolean(!enable));
            sender.sendSuccess(() -> Component.literal("Toggled file command: " + name).withStyle(ChatFormatting.GREEN), false);
        } catch (Exception e) {
            TASmod.LOGGER.error("Failed to toggle file command", e);
            sender.sendFailure(Component.literal("Failed to toggle file command: " + e.getMessage()).withStyle(ChatFormatting.RED));
        }
        return 1;
    }

    private static CompletableFuture<Suggestions> suggestFileCommands(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        CommandSourceStack sender = context.getSource();
        try {
            Map<String, Boolean> fileCommandNames = getExtensions(sender);
            for (String name : fileCommandNames.keySet()) {
                builder.suggest(name);
            }
        } catch (Exception e) {
            TASmod.LOGGER.error("Failed to get file command list", e);
        }
        return builder.buildFuture();
    }

    private static Map<String, Boolean> getExtensions(CommandSourceStack sender) throws InterruptedException, ExecutionException, TimeoutException {
        Map<String, Boolean> out = new LinkedHashMap<>();
        CompletableFuture<List<String>> future = new CompletableFuture<>();
        fileCommandList = future;

        try {
            String senderName = sender.getPlayer().getName().getString();
            TASmod.server.sendTo(senderName, new TASmodBufferBuilder(COMMAND_FILECOMMANDLIST));
        } catch (Exception e) {
            e.printStackTrace();
        }

        List<String> commands = future.get(2, TimeUnit.SECONDS);

        commands.forEach(element -> {
            Pattern pattern = Pattern.compile("^E_");
            Matcher matcher = pattern.matcher(element);
            if (matcher.find()) {
                element = matcher.replaceFirst("");
                out.put(element, true);
                return;
            }

            pattern = Pattern.compile("^D_");
            matcher = pattern.matcher(element);
            if (matcher.find()) {
                element = matcher.replaceFirst("");
                out.put(element, false);
                return;
            }
        });

        return out;
    }

    private static List<String> getColoredNames(Map<String, Boolean> list) {
        List<String> out = new ArrayList<>();
        list.forEach((name, enabled) -> {
            out.add(String.format("%s%s%s", enabled ? ChatFormatting.GREEN : ChatFormatting.RED, name, ChatFormatting.RESET));
        });
        return out;
    }

    @Override
    public PacketID[] getAcceptedPacketIDs() {
        return new PacketID[] { COMMAND_FILECOMMANDLIST, PLAYBACK_FILECOMMAND_ENABLE };
    }

    @Override
    public void onServerPacket(PacketID id, ByteBuffer buf, String username) throws PacketNotImplementedException, WrongSideException, Exception {
        TASmodPackets packet = (TASmodPackets) id;
        switch (packet) {
            case COMMAND_FILECOMMANDLIST:
                String filecommandnames = TASmodBufferBuilder.readString(buf);
                fileCommandList.complete(Arrays.asList(filecommandnames.split("\\|")));
                break;
            default:
                throw new WrongSideException(packet, Side.SERVER);
        }
    }

    // ========== Client

    @Override
    public void onClientPacket(PacketID id, ByteBuffer buf, String username) throws PacketNotImplementedException, WrongSideException, Exception {
        TASmodPackets packet = (TASmodPackets) id;
        switch (packet) {
            case COMMAND_FILECOMMANDLIST:
                String filecommandnames = String.join("|", getFileCommandNames(TASmodAPIRegistry.PLAYBACK_FILE_COMMAND.getAll()));
                TASmodClient.client.send(new TASmodBufferBuilder(COMMAND_FILECOMMANDLIST).writeString(filecommandnames));
                break;
            case PLAYBACK_FILECOMMAND_ENABLE:
                String filecommand = TASmodBufferBuilder.readString(buf);
                boolean enable = TASmodBufferBuilder.readBoolean(buf);
                boolean success = TASmodAPIRegistry.PLAYBACK_FILE_COMMAND.setEnabled(filecommand, enable);

                String msg = success ? String.format("%s%s file command: %s", ChatFormatting.GREEN, enable ? "Enabled" : "Disabled", filecommand) : String.format("%sFailed to %s file command: %s", ChatFormatting.RED, enable ? "enable" : "disable", filecommand);
                // Minecraft.getInstance().gui.getChat().addMessage(Component.literal(msg));
                break;
            default:
                break;
        }
    }

    private List<String> getFileCommandNames(List<PlaybackFileCommandExtension> fileCommands) {
        List<String> out = new ArrayList<>();
        fileCommands.forEach(element -> {
            out.add(String.format("%s_%s", element.isEnabled() ? "E" : "D", element.toString()));
        });
        return out;
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