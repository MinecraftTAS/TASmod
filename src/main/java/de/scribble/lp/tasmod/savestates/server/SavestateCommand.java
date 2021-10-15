package de.scribble.lp.tasmod.savestates.server;

import java.io.IOException;
import java.util.List;

import de.scribble.lp.tasmod.TASmod;
import de.scribble.lp.tasmod.savestates.server.exceptions.LoadstateException;
import de.scribble.lp.tasmod.savestates.server.exceptions.SavestateDeleteException;
import de.scribble.lp.tasmod.savestates.server.exceptions.SavestateException;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;

public class SavestateCommand extends CommandBase {

	@Override
	public String getName() {
		return "savestate";
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return "/savestate <save|load|delete|info> [index]";
	}

	@Override
	public int getRequiredPermissionLevel() {
		return 2;
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		if (args.length == 0) {
			int currentIndex = TASmod.savestateHandler.getCurrentIndex();
			sender.sendMessage(new TextComponentString(String.format("The current savestate index is %s", currentIndex)));
			sender.sendMessage(new TextComponentString(String.format("Available indexes are%s", TASmod.savestateHandler.getIndexesAsString())));
			sender.sendMessage(new TextComponentString(" "));
			sender.sendMessage(new TextComponentString("/savestate <save|load|delete|info> [index]"));
			sender.sendMessage(new TextComponentString("/savestate save - Make a savestate at the next index"));
			sender.sendMessage(new TextComponentString("/savestate save <index> - Make a savestate at the specified index"));
			sender.sendMessage(new TextComponentString("/savestate load - Load the savestate at the current index"));
			sender.sendMessage(new TextComponentString("/savestate load <index> - Load the savestate at the specified index"));
			sender.sendMessage(new TextComponentString("/savestate delete <index> - Delete the savestate at the specified index"));
			sender.sendMessage(new TextComponentString("/savestate delete <from> <to> - Delete the savestates from the first to the second index"));
			sender.sendMessage(new TextComponentString(""));
			sender.sendMessage(new TextComponentString("Instead of <index> you can use ~ to specify an index relative to the current one e.g. ~-1 will load " + (currentIndex - 1)));
		} else if (args.length >= 1) {
			if ("save".equals(args[0])) {
				if (args.length == 1) {
					try {
						TASmod.savestateHandler.saveState();
					} catch (SavestateException e) {
						throw new CommandException(e.getMessage(), new Object[] {});
					} catch (IOException e) {
						e.printStackTrace();
						throw new CommandException(e.getMessage(), new Object[] {});
					} finally {
						TASmod.savestateHandler.state = SavestateState.NONE;
					}
				} else if (args.length == 2) {
					try {
						TASmod.savestateHandler.saveState(processIndex(args[1]));
					} catch (SavestateException e) {
						throw new CommandException(e.getMessage(), new Object[] {});
					} catch (IOException e) {
						e.printStackTrace();
						throw new CommandException(e.getMessage(), new Object[] {});
					} finally {
						TASmod.savestateHandler.state = SavestateState.NONE;
					}
				} else {
					throw new CommandException("Too many arguments!", new Object[] {});
				}
			} else if ("load".equals(args[0])) {
				if (args.length == 1) {
					try {
						TASmod.savestateHandler.loadState();
					} catch (LoadstateException e) {
						throw new CommandException(e.getMessage(), new Object[] {});
					} catch (IOException e) {
						e.printStackTrace();
						throw new CommandException(e.getMessage(), new Object[] {});
					} finally {
						TASmod.savestateHandler.state = SavestateState.NONE;
					}
				} else if (args.length == 2) {
					try {
						TASmod.savestateHandler.loadState(processIndex(args[1]));
					} catch (LoadstateException e) {
						throw new CommandException(e.getMessage(), new Object[] {});
					} catch (IOException e) {
						e.printStackTrace();
						throw new CommandException(e.getMessage(), new Object[] {});
					} finally {
						TASmod.savestateHandler.state = SavestateState.NONE;
					}
				} else {
					throw new CommandException("Too many arguments!", new Object[] {});
				}
			} else if ("delete".equals(args[0])) {
				if (args.length == 2) {
					try {
						TASmod.savestateHandler.deleteIndex(processIndex(args[1]));
					} catch (SavestateDeleteException e) {
						throw new CommandException(e.getMessage(), new Object[] {});
					}
				} else if (args.length == 3) {
					try {
						TASmod.savestateHandler.deleteIndex(processIndex(args[1]), processIndex(args[2]));
					} catch (SavestateDeleteException e) {
						throw new CommandException(e.getMessage(), new Object[] {});
					}
				} else {
					throw new CommandException("Too many arguments!", new Object[] {});
				}
			} else if ("info".equals(args[0])) {
				sender.sendMessage(new TextComponentString(String.format("The current savestate index is %s", TASmod.savestateHandler.getCurrentIndex())));
				sender.sendMessage(new TextComponentString(String.format("Available indexes are%s", TASmod.savestateHandler.getIndexesAsString())));
			}
		}
	}

	private int processIndex(String arg) throws CommandException {
		if ("~".equals(arg)) {
			return TASmod.savestateHandler.getCurrentIndex();
		} else if (arg.matches("~-?\\d")) {
			arg = arg.replace("~", "");
			int i = Integer.parseInt(arg);
			return TASmod.savestateHandler.getCurrentIndex() + i;
		} else {
			int i = 0;
			try {
				i = Integer.parseInt(arg);
			} catch (NumberFormatException e) {
				throw new CommandException("The specified index is not a number: %s", arg);
			}
			return i;
		}
	}

	@Override
	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, BlockPos targetPos) {
		if (args.length == 1) {
			return getListOfStringsMatchingLastWord(args, new String[] { "save", "load", "delete", "info" });
		} else if (args.length == 2 && !"info".equals(args[0])) {
			sender.sendMessage(new TextComponentString("Available indexes: " + TASmod.savestateHandler.getIndexesAsString()));
		}
		return super.getTabCompletions(server, sender, args, targetPos);
	}
}
