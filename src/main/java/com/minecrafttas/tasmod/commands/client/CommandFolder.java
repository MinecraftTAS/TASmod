package com.minecrafttas.tasmod.commands.client;

import static com.minecrafttas.tasmod.TASmod.LOGGER;

import java.awt.Desktop;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import com.minecrafttas.tasmod.TASmod;
import com.minecrafttas.tasmod.TASmodClient;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.gui.Gui;

import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

public class CommandFolder extends ClientCommandBase {

	@Override
	public String getName() {
		return "folder";
	}

	@Override
	public String getExtensionName() {
		return "folder";
	}

	@Override
	public String getUsage() {
		return "/folder <type>";
	}

	@Override
	public int getRequiredPermissionLevel() {
		return 0;
	}

	@Override
	public void execute(LocalPlayer player, String[] args) {
		if (args.length == 1) {
			if (args[0].equalsIgnoreCase("savestates")) {
				openSavestates();
			} else if (args[0].equalsIgnoreCase("tasfiles")) {
				openTASFolder();
			}
		}
	}

	@Override
	public List<String> getTabCompletions(LocalPlayer player, String[] args) {
		List<String> tab = new ArrayList<>();
		if (args.length == 1) {
			String lastWord = args[args.length - 1];
			for (String option : new String[] { "savestates", "tasfiles" }) {
				if (option.startsWith(lastWord)) {
					tab.add(option);
				}
			}
		}
		return tab;
	}

	private void openTASFolder() {
		Path file = TASmodClient.tasfiledirectory;
		try {
			TASmodClient.createTASfileDir();
			Desktop.getDesktop().open(file.toFile());
		} catch (IOException e) {
			LOGGER.error("Something went wrong while opening {}", file);
			LOGGER.catching(e);
		}
	}

	private void openSavestates() {
		Path file = TASmodClient.getSavestateDirectory();
		if (TASmod.getServerInstance() != null) {
			file = TASmod.savestateHandlerServer.getCurrentSavestateDir();
		}

		if (!Files.exists(file)) {
			Minecraft mc = Minecraft.getInstance();
			if (mc.player != null) {
				mc.player.sendSystemMessage(Component.literal("Can't open savestates, as the directory doesn't exist").withStyle(ChatFormatting.RED));
			}
			return;
		}

		try {
			Desktop.getDesktop().open(file.toFile());
		} catch (IOException e) {
			LOGGER.error("Something went wrong while opening {}", file);
			LOGGER.catching(e);
		}
	}
}
