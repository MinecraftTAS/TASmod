package de.scribble.lp.tasmod.playback;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;

import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;

public class CommandFolder extends CommandBase{

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "folder";
	}

	@Override
	public String getUsage(ICommandSender sender) {
		// TODO Auto-generated method stub
		return "/folder";
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		openWorkFolder();
		
	}
	public static void openWorkFolder() {
        try {
            Desktop.getDesktop().open(new File(Minecraft.getMinecraft().mcDataDir, "saves" + File.separator +
                    "tasfiles"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
