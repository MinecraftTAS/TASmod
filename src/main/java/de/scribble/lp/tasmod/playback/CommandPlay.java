package de.scribble.lp.tasmod.playback;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableList;

import de.scribble.lp.tasmod.CommonProxy;
import de.scribble.lp.tasmod.recording.InputRecorder;
import de.scribble.lp.tasmod.ticksync.TickSyncPackage;
import de.scribble.lp.tasmod.ticksync.TickSyncServer;
import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

public class CommandPlay extends CommandBase{
	private Minecraft mc = Minecraft.getMinecraft();


    public List<String> getFilenames() {
        List<String> tab = new ArrayList<String>();
        File folder = new File(Minecraft.getMinecraft().mcDataDir, "saves" + File.separator + "tasfiles");
        File[] listOfFiles = folder.listFiles();
        for (int i = 0; i < listOfFiles.length; i++) {
            tab.add(listOfFiles[i].getName().replaceAll("\\.tas", ""));
        }
        return tab;
    }

    public void sendMessage(String msg) {
        try {
            Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage(new TextComponentString(msg));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public String getName() {
        return "play";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/play <filename> (without .tas)";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 2;
    }

    @Override
    public List<String> getAliases() {
        return ImmutableList.of("p");
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (!(sender instanceof EntityPlayer)) {
            return;
        }
        if (!InputRecorder.isRecording() && !InputPlayback.isPlayingback()) {
            if (args.length == 0) {
                sender.sendMessage(new TextComponentString(TextFormatting.RED + "/play <filename> (without .tas)"));
                return;
            }
            File file = new File(Minecraft.getMinecraft().mcDataDir, "saves" + File.separator +
                    "tasfiles" + File.separator + args[0] + ".tas");
            if (file.exists()) {
                if (args.length == 1) {
                	TickSyncServer.resetTickCounter();
                	CommonProxy.NETWORK.sendToAll(new TickSyncPackage(TickSyncServer.getServertickcounter(),true,TickSyncServer.isEnabled()));
                    InputPlayback.startPlayback(file,args[0]);
                    return;
                } else if (args.length == 2 && !args[1].equalsIgnoreCase("load")) {
                    sender.sendMessage(new TextComponentString(TextFormatting.RED + "Wrong usage! /play <filename>"));
                    return;
                }
            } else {
                sender.sendMessage(new TextComponentString(TextFormatting.RED + "File '" + args[0] + "' does not exist"));
            }
            if (args.length > 2) sender.sendMessage(new TextComponentString(TextFormatting.RED + "Too many arguments"));
            return;
        } else if (InputRecorder.isRecording()) {
            sender.sendMessage(new TextComponentString(TextFormatting.RED + "A recording is running. /record or /fail to abort recording"));
            return;
        } else if(InputPlayback.isPlayingback()){
            InputPlayback.stopPlayback();
			
            //String[] sentences = new String[] {"I have finished my TAS", "Thats time!", "Playback done, where's the money?", "Im done, what's next?"};
			//Minecraft.getMinecraft().player.sendChatMessage(sentences[new Random().nextInt(sentences.length)]); // Uwot here he is using Random!!!
            // Sad Pfannekuchen right here ^
        }
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args,
                                          BlockPos targetPos) {
        List<String> tab;
        if (args.length == 1) {
            tab = getFilenames();
            if (tab.isEmpty()) {
                sender.sendMessage(new TextComponentString(TextFormatting.RED + "No files in directory"));
            }
            return getListOfStringsMatchingLastWord(args, tab);
        } else if (args.length == 2) {
            return getListOfStringsMatchingLastWord(args, "load");
        } else return super.getTabCompletions(server, sender, args, targetPos);
    }
}
