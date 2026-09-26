package com.minecrafttas.tasmod.commands.client;

import com.minecrafttas.tasmod.mctcommon.registry.Registerable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

import java.util.List;

public abstract class ClientCommandBase implements Registerable {

	public abstract String getName();

	public abstract String getUsage();

	public abstract int getRequiredPermissionLevel();

	public abstract void execute(LocalPlayer player, String[] args);

	public abstract List<String> getTabCompletions(LocalPlayer player, String[] args);
}
