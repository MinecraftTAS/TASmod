package com.minecrafttas.tasmod.commands.client;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;

import com.minecrafttas.tasmod.mctcommon.registry.AbstractRegistry;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;

public class ClientCommandRegistry extends AbstractRegistry<ClientCommandBase> {
	public ClientCommandRegistry() {
		super("CLIENTCOMMAND_REGISTRY", new LinkedHashMap<>());
	}

	/**
	 * <p>Checks the chat message for client commands and runs them
	 * 
	 * @param chatMessage The chat message to check
	 * @return Boolean, whether the command execution should be canceled
	 */
	public boolean runClientCommands(String chatMessage) {
		if (!chatMessage.startsWith("/")) {
			return false;
		}
		chatMessage = chatMessage.substring(1);
		for (String commandName : REGISTRY.keySet()) {
			if (chatMessage.startsWith(commandName)) {
				Minecraft mc = Minecraft.getInstance();
				LocalPlayer player = mc.player;
				ClientCommandBase command = REGISTRY.get(commandName);

				String[] args = chatMessage.split(" ");
				args = dropFirstString(args);
				try {
					command.execute(player, args);
				} catch (Exception e) {
					// Try to send error message to chat
					if (player != null) {
						player.sendSystemMessage(Component.literal(e.getMessage()));
					}
				}
				return true;
			}
		}
		return false;
	}

	/**
	 * <p>Checks the tab completion request for client commands
	 * 
	 * @param chatMessage The chat message to check
	 * @return Boolean, whether the vanilla tab completion should be canceled
	 */
	public String[] runTabCompletions(String chatMessage) {
		if (!chatMessage.startsWith("/")) {
			return null;
		}

		chatMessage = chatMessage.substring(1);
		for (String commandName : REGISTRY.keySet()) {
			if (chatMessage.startsWith(commandName)) {
				Minecraft mc = Minecraft.getInstance();
				LocalPlayer player = mc.player;
				ClientCommandBase command = REGISTRY.get(commandName);

				String[] args = chatMessage.split(" ");
				args = dropFirstString(args);

				return command.getTabCompletions(player, args).toArray(new String[0]);
			}
		}
		return null;
	}

	private static String[] dropFirstString(String[] strings) {
		String[] strings2 = new String[strings.length - 1];
		System.arraycopy(strings, 1, strings2, 0, strings.length - 1);
		return strings2;
	}

	public Collection<ClientCommandBase> getClientCommandList() {
		return REGISTRY.values();
	}
}
