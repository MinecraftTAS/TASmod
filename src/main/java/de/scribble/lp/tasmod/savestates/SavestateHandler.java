package de.scribble.lp.tasmod.savestates;

import java.io.File;

import de.scribble.lp.tasmod.CommonProxy;
import de.scribble.lp.tasmod.ModLoader;
import de.scribble.lp.tasmod.misc.SavestateException;
import net.minecraft.client.Minecraft;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;

/**
 * New and improved savestatehandler. Creates and loads savestates on both client and server without closing the world <br>
 * The old version of this was heavily inspired by bspkrs' <a href="https://www.curseforge.com/minecraft/mc-mods/worldstatecheckpoints">WorldStateCheckpoints</a>, but I can honestly say now
 * that I replaced everything that mod had with new things. This also makes savestates without closing the server...
 * 
 * @author ScribbleLP
 *
 */
public class SavestateHandler {
	private static MinecraftServer server=ModLoader.getServerInstance();
	private static final File savestateDirectory=new File(server.getDataDirectory()+File.separator+"saves"+File.separator+"savestates"+File.separator);
	
	private static boolean isSaving=false;
	
	private static boolean isLoading=false;
	
	public SavestateHandler() {
		if(!savestateDirectory.exists()) {
			savestateDirectory.mkdir();
		}
	}
	public static void saveState() throws SavestateException {
		
		File currentfolder=new File(server.getDataDirectory()+File.separator+server.getFolderName());
		File targetfolder=getSavestateFolder(server.getFolderName());
		server.getPlayerList().sendMessage(new TextComponentString(targetfolder.getAbsolutePath()));
	}
	
	private static File getSavestateFolder(String worldname) throws SavestateException {
		int i = 1;
		File targetsavefolder=null;
		isSaving=true;
		while (i <= 300) {
			if (i >= 300) {
				isSaving = false;
				throw new SavestateException("Savestatecount is greater or equal than 300");
			}
			targetsavefolder = new File(savestateDirectory,worldname + "-Savestate" + Integer.toString(i)+File.separator);
			
			if (!targetsavefolder.exists()) {
				break;
			}
			i++;
		}
		return targetsavefolder;
	}
}
