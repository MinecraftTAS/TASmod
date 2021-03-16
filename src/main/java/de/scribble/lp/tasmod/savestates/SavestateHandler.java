package de.scribble.lp.tasmod.savestates;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import javax.annotation.Nullable;

import org.apache.commons.io.FileUtils;

import com.google.common.io.Files;

import de.scribble.lp.tasmod.CommonProxy;
import de.scribble.lp.tasmod.ModLoader;
import de.scribble.lp.tasmod.recording.savestates.RecordingSavestatePacket;
import de.scribble.lp.tasmod.savestates.chunkloading.SavestatesChunkControl;
import de.scribble.lp.tasmod.savestates.exceptions.LoadstateException;
import de.scribble.lp.tasmod.savestates.exceptions.SavestateException;
import de.scribble.lp.tasmod.savestates.playerloading.SavestatePlayerLoading;
import de.scribble.lp.tasmod.tickratechanger.TickrateChangerServer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.storage.AnvilChunkLoader;

/**
 * Creates and loads savestates on both client and server without closing the world <br>
 * The old version that you may find in TASTools was heavily inspired by bspkrs' <a href="https://www.curseforge.com/minecraft/mc-mods/worldstatecheckpoints">WorldStateCheckpoints</a>,
 * but this new version is completely self written.
 * 
 * @author ScribbleLP
 *
 */
public class SavestateHandler {
	private static MinecraftServer server=ModLoader.getServerInstance();
	private static final File savestateDirectory=new File(server.getDataDirectory()+File.separator+"saves"+File.separator+"savestates"+File.separator);
	
	public static boolean isSaving=false;
	
	public static boolean isLoading=false;
	public static boolean wasLoading=false;
	
	/**
	 * Creates a copy of the currently played world and saves it in .minecraft/saves/savestates/worldname <br>
	 * Called in {@link SavestatePacketHandler}<br>
	 * <br>
	 * Side: Server
	 * @throws SavestateException
	 * @throws IOException
	 */
	public static void saveState() throws SavestateException, IOException {
		if(isSaving) {
			throw new SavestateException("A savestating operation is already being carried out");
		}
		if(isLoading) {
			throw new SavestateException("A loadstate operation is being carried out");
		}
		//Lock savestating and loadstating
		isSaving=true;
		
		//Create a directory just in case
		createSavestateDirectory();
		
		//Enable tickrate 0
		TickrateChangerServer.changeServerTickrate(0);
		TickrateChangerServer.changeClientTickrate(0);
		
		//Update the server variable
		server=ModLoader.getServerInstance();
		
		//Save the world!
		server.getPlayerList().saveAllPlayerData();
		server.saveAllWorlds(true);
		
		//Display the loading screen on the client
		CommonProxy.NETWORK.sendToAll(new SavestatePacket());
		
		//Get the current and taget directory for copying
		String worldname=server.getFolderName();
		File currentfolder=new File(server.getDataDirectory(),"saves"+File.separator+worldname);
		File targetfolder=getNextSaveFolderLocation(worldname);
		
		CommonProxy.NETWORK.sendToAll(new RecordingSavestatePacket(true, nameWhenSaving(worldname)));
		
		incrementSavestates(worldname);
		
		//Wait for the chunkloader to save the game
		for(WorldServer world:server.worlds) {
			AnvilChunkLoader chunkloader=(AnvilChunkLoader)world.getChunkProvider().chunkLoader;
			while(chunkloader.getPendingSaveCount()>0) {
			}
		}
		
		//Copy the directory
		FileUtils.copyDirectory(currentfolder, targetfolder);
		
		//Close the GuiSavestateScreen
		CommonProxy.NETWORK.sendToAll(new SavestatePacket());
		
		//Unlock savestating
		isSaving=false;
	}
	
	/**
	 * Searches through the savestate folder to look for the next possible savestate foldername <br>
	 * Savestate equivalent to {@link SavestateHandler#getLatestSavestateLocation(String)}
	 * @param worldname
	 * @return targetsavefolder
	 * @throws SavestateException if the found savestates count is greater or equal than 300
	 */
	private static File getNextSaveFolderLocation(String worldname) throws SavestateException {
		int i = 1;
		int limit=300;
		File targetsavefolder=null;
		isSaving=true;
		while (i <= limit) {
			if (i >= limit) {
				isSaving = false;
				throw new SavestateException("Savestatecount is greater or equal than "+limit);
			}
			targetsavefolder = new File(savestateDirectory,worldname + "-Savestate" + Integer.toString(i)+File.separator);
			
			if (!targetsavefolder.exists()) {
				break;
			}
			i++;
		}
		return targetsavefolder;
	}
	
	private static String nameWhenSaving(String worldname) {
		int i = 1;
		int limit=300;
		File targetsavefolder=null;
		String name="";
		while (i <= limit) {
			if (i >= limit) {
				break;
			}
			name=worldname + "-Savestate" + Integer.toString(i);
			targetsavefolder = new File(savestateDirectory,name+File.separator);
			
			if (!targetsavefolder.exists()) {
				break;
			}
			i++;
		}
		return name;
	}
	
	@Deprecated
	private static void incrementSavestates(String worldname) {
		int[] incr = getInfoValues(getInfoFile(worldname));
		if (incr[0] == 0) {
			saveInfo(getInfoFile(worldname), null);
		} else {
			incr[0]++;
			saveInfo(getInfoFile(worldname), incr);
		}
	}
	
	@Deprecated
	private static File getInfoFile(String worldname) {
		File file = new File(savestateDirectory, worldname + "-info.txt");
		return file;
	}
	
	@Deprecated
	private static int[] getInfoValues(File file){
    	int[] out = {0,0};
    	if (file.exists()){
			try {
				BufferedReader buff = new BufferedReader(new FileReader(file));
				String s;
				int i = 0;
				while (i < 100) {
					s = buff.readLine();
					if (s.equalsIgnoreCase("END")) {
						break;
					} else if (s.startsWith("#")) {
						continue;
					} else if (s.startsWith("Total Savestates")) {
						String[] valls = s.split("=");
						out[0] = Integer.parseInt(valls[1]);
					} else if (s.startsWith("Total Rerecords")) {
						String[] valls = s.split("=");
						out[1] = Integer.parseInt(valls[1]);
					}
					i++;
				}
				buff.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
    	}
    	return out;
    }
	
	@Deprecated
	private static void saveInfo(File file,@Nullable int[] values) {
    	StringBuilder output= new StringBuilder();
    	output.append("#This file was generated by TASTools and diplays info about the usage of savestates!\n\n");
    	if(values==null) {
    		output.append("Total Savestates=1\nTotal Rerecords=0\nEND");
    	}else {
       		output.append("Total Savestates="+Integer.toString(values[0])+"\nTotal Rerecords="+Integer.toString(values[1])+"\nEND");
    	}
    	try{
    		Files.write(output.toString().getBytes(), file);
		} catch (IOException e) {
			e.printStackTrace();
    	}
    }
	
	/**
	 * Loads the latest savestate it can find in .minecraft/saves/savestates/worldname-Savestate
	 * 
	 * @Side Server
	 * @throws LoadstateException
	 * @throws IOException
	 */
	public static void loadState() throws LoadstateException, IOException {
		if(isSaving) {
			throw new LoadstateException("A savestating operation is already being carried out");
		}
		if(isLoading) {
			throw new LoadstateException("A loadstate operation is being carried out");
		}
		//Lock savestating and loadstating
		isLoading=true;
		
		//Create a directory just in case
		createSavestateDirectory();
		
		//Enable tickrate 0
		TickrateChangerServer.changeServerTickrate(0);
		TickrateChangerServer.changeClientTickrate(0);
		
		
		//Update the server instance
		server=ModLoader.getServerInstance();
		
		//Get the current and target directory for copying
		String worldname=server.getFolderName();
		File currentfolder=new File(server.getDataDirectory(),"saves"+File.separator+worldname);
		File targetfolder=getLatestSavestateLocation(worldname);
		
		CommonProxy.NETWORK.sendToAll(new RecordingSavestatePacket(false, nameWhenLoading(worldname)));
		
		//Disabeling level saving for all worlds in case the auto save kicks in during world unload
		for(WorldServer world: server.worlds) {
			world.disableLevelSaving=true;		
		}
		
		//Unload chunks on the client
		CommonProxy.NETWORK.sendToAll(new LoadstatePacket());
		
		//Unload chunks on the server
		SavestatesChunkControl.disconnectPlayersFromChunkMap();
		SavestatesChunkControl.unloadAllServerChunks();
		SavestatesChunkControl.flushSaveHandler();
		
		
		//Delete and copy directories
		FileUtils.deleteDirectory(currentfolder);
		FileUtils.copyDirectory(targetfolder, currentfolder);
		
		
		//Update the player and the client
		SavestatePlayerLoading.loadAndSendMotionToPlayer();
		//Update the session.lock file so minecraft behaves and saves the world
		SavestatesChunkControl.updateSessionLock();
		//Load the chunks and send them to the client
		SavestatesChunkControl.addPlayersToChunkMap();
		
		
		//Enable level saving again
		for(WorldServer world: server.worlds) {
			world.disableLevelSaving=false;
		}
		
		//Send a notification that the savestate has been loaded
		server.getPlayerList().sendMessage(new TextComponentString(TextFormatting.GREEN+"Savestate loaded"));
		
		//Sends a message to all players to start the recording again
		CommonProxy.NETWORK.sendToAll(new LoadstatePacket(true));
		
		//Unlock loadstating
		isLoading=false;
		wasLoading=true;
	}
	/**
	 * Searches through the savestate folder to look for the latest savestate<br>
	 * Loadstate equivalent to {@link SavestateHandler#getNextSaveFolderLocation(String)}
	 * @param worldname
	 * @return targetsavefolder
	 * @throws LoadstateException if there is no savestate or more than 300 savestates
	 */
	private static File getLatestSavestateLocation(String worldname) throws LoadstateException {
		int i=1;
		int limit=300;
		
		File targetsavefolder=null;
		while(i<=300) {
			targetsavefolder = new File(savestateDirectory,worldname+"-Savestate"+Integer.toString(i));
			if (!targetsavefolder.exists()) {
				if(i-1==0) {
					throw new LoadstateException("Couldn't find any savestates");
				}
				if(i>300) {
					throw new LoadstateException("Savestatecount is greater or equal than "+limit);
				}
				targetsavefolder = new File(savestateDirectory,worldname+"-Savestate"+Integer.toString(i-1));
				break;
			}
			i++;
		}
		return targetsavefolder;
	}
	
	private static String nameWhenLoading(String worldname) throws LoadstateException {
		int i=1;
		int limit=300;
		String name="";
		File targetsavefolder=null;
		while(i<=300) {
			targetsavefolder = new File(savestateDirectory,worldname+"-Savestate"+Integer.toString(i));
			if (!targetsavefolder.exists()) {
				if(i-1==0) {
					throw new LoadstateException("Couldn't find any savestates");
				}
				if(i>300) {
					throw new LoadstateException("Savestatecount is greater or equal than "+limit);
				}
				
				name=worldname+"-Savestate"+Integer.toString(i-1);
				break;
			}
			i++;
		}
		return name;
	}

	/**
	 * Creates the savestate directory in case the user deletes it between savestates
	 */
	private static void createSavestateDirectory() {
		if(!savestateDirectory.exists()) {
			savestateDirectory.mkdir();
		}
	}
	
	public static void playerLoadSavestateEvent() {
		MinecraftServer server=ModLoader.getServerInstance();
		EntityPlayerMP player=server.getPlayerList().getPlayers().get(0);
		NBTTagCompound nbttagcompound = ModLoader.getServerInstance().getPlayerList().getPlayerNBT(player);
		SavestatePlayerLoading.reattachEntityToPlayer(nbttagcompound, player.getServerWorld(), player);
	}
}