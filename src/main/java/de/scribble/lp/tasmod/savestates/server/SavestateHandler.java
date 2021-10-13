package de.scribble.lp.tasmod.savestates.server;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;

import de.scribble.lp.tasmod.CommonProxy;
import de.scribble.lp.tasmod.TASmod;
import de.scribble.lp.tasmod.savestates.client.InputSavestatesHandler;
import de.scribble.lp.tasmod.savestates.client.InputSavestatesPacket;
import de.scribble.lp.tasmod.savestates.server.chunkloading.SavestatesChunkControl;
import de.scribble.lp.tasmod.savestates.server.exceptions.LoadstateException;
import de.scribble.lp.tasmod.savestates.server.exceptions.SavestateException;
import de.scribble.lp.tasmod.savestates.server.motion.ClientMotionServer;
import de.scribble.lp.tasmod.savestates.server.playerloading.SavestatePlayerLoading;
import de.scribble.lp.tasmod.tickratechanger.TickrateChangerServer;
import de.scribble.lp.tasmod.util.FileThread;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.storage.AnvilChunkLoader;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Creates and loads savestates on both client and server without closing the world <br>
 * The old version that you may find in TASTools was heavily inspired by bspkrs' <a href="https://www.curseforge.com/minecraft/mc-mods/worldstatecheckpoints">WorldStateCheckpoints</a>,
 * but this new version is completely self written.
 * 
 * @author ScribbleLP
 *
 */
public class SavestateHandler {
	private MinecraftServer server;
	private File savestateDirectory;
	
	public static SavestateState state=SavestateState.NONE;
	
	public SavestateHandler(MinecraftServer server) {
		this.server=server;
		createSavestateDirectory();
		refreshSavestateMap();
		loadCurrentIndex();
	}
	
	/**
	 * Creates a copy of the currently played world and saves it in .minecraft/saves/savestates/worldname <br>
	 * Called in {@link SavestatePacketHandler}<br>
	 * <br>
	 * Side: Server
	 * @param savestateIndex The index where the mod will save the savestate -1 if it should load the latest
	 * @throws SavestateException
	 * @throws IOException
	 */
	public void saveState(int savestateIndex) throws SavestateException, IOException {
		if(state==SavestateState.SAVING) {
			throw new SavestateException("A savestating operation is already being carried out");
		}
		if(state==SavestateState.LOADING) {
			throw new SavestateException("A loadstate operation is being carried out");
		}
		//Lock savestating and loadstating
		state=SavestateState.SAVING;
		
		//Create a directory just in case
		createSavestateDirectory();
		
		increaseCurrentIndex();
		saveCurrentIndex();
		
		//Enable tickrate 0
		TickrateChangerServer.changeServerTickrate(0);
		TickrateChangerServer.changeClientTickrate(0);
		
		//Update the server variable
		server=TASmod.getServerInstance();
		
		//Get the motion from the client
		ClientMotionServer.requestMotionFromClient();
		
		//Save the world!
		server.getPlayerList().saveAllPlayerData();
		server.saveAllWorlds(true);
		
		
		//Get the current and target directory for copying
		String worldname=server.getFolderName();
		File currentfolder=new File(savestateDirectory,".."+File.separator+worldname);
		File targetfolder=getNextSaveFolderLocation(worldname);
		
		//Send the name of the world to all players. This will make a savestate of the recording on the client with that name
		CommonProxy.NETWORK.sendToAll(new InputSavestatesPacket(true, nameWhenSaving(worldname)));
		
		//Wait for the chunkloader to save the game
		for(WorldServer world:server.worlds) {
			AnvilChunkLoader chunkloader=(AnvilChunkLoader)world.getChunkProvider().chunkLoader;
			while(chunkloader.getPendingSaveCount()>0) {
			}
		}
		
		//Copy the directory
		FileUtils.copyDirectory(currentfolder, targetfolder);
		
		//Incrementing info file
		SavestateTrackerFile tracker = new SavestateTrackerFile(new File(savestateDirectory, worldname+"-info.txt"));
		tracker.increaseSavestates();
		tracker.saveFile();
		
		//Close the GuiSavestateScreen on the client
		CommonProxy.NETWORK.sendToAll(new SavestatePacket());
		
		//Unlock savestating
		state=SavestateState.NONE;
	}
	
	/**
	 * Searches through the savestate folder to look for the next possible savestate foldername <br>
	 * Savestate equivalent to {@link SavestateHandler#getLatestSavestateLocation(String)}
	 * @param worldname The worldname of the current world
	 * @return targetsavefolder The file where the savestate should be copied to
	 * @throws SavestateException if the found savestates count is greater or equal than 300
	 */
	@Deprecated
	private File getNextSaveFolderLocation(String worldname) throws SavestateException {
		int i = 1;
		int limit=300;
		File targetsavefolder=null;
		while (i <= limit) {
			if (i >= limit) {
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
	
	/**
	 * Get's the correct string of the savestate, used in {@linkplain InputSavestatesHandler#savestate(String)}
	 * 
	 * @param worldname the name of the world currently on the server
	 * @return The correct name of the next savestate
	 */
	@Deprecated
	private String nameWhenSaving(String worldname) {
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
	
	/**
	 * Loads the latest savestate it can find in .minecraft/saves/savestates/worldname-Savestate
	 * 
	 * Side: Server
	 * @throws LoadstateException
	 * @throws IOException
	 */
	public void loadState() throws LoadstateException, IOException {
		if(state==SavestateState.SAVING) {
			throw new LoadstateException("A savestating operation is already being carried out");
		}
		if(state==SavestateState.LOADING) {
			throw new LoadstateException("A loadstate operation is being carried out");
		}
		//Lock savestating and loadstating
		state=SavestateState.LOADING;
		
		//Create a directory just in case
		createSavestateDirectory();
		
		//Enable tickrate 0
		TickrateChangerServer.changeServerTickrate(0);
		TickrateChangerServer.changeClientTickrate(0);
		
		
		//Update the server instance
		server=TASmod.getServerInstance();
		
		//Get the current and target directory for copying
		String worldname=server.getFolderName();
		File currentfolder=new File(savestateDirectory,".."+File.separator+worldname);
		File targetfolder=getLatestSavestateLocation(worldname);
		
		//Load savestate on the client
		CommonProxy.NETWORK.sendToAll(new InputSavestatesPacket(false, nameWhenLoading(worldname)));
		
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
		
		//Incrementing info file
		SavestateTrackerFile tracker = new SavestateTrackerFile(new File(savestateDirectory, worldname+"-info.txt"));
		tracker.increaseRerecords();
		tracker.saveFile();
		
		//Send a notification that the savestate has been loaded
		server.getPlayerList().sendMessage(new TextComponentString(TextFormatting.GREEN+"Savestate loaded"));
		
		WorldServer[] worlds = DimensionManager.getWorlds();
        
        for(WorldServer world : worlds) {
            world.tick();
        }
		
		//Unlock loadstating
		state=SavestateState.WASLOADING;
	}
	
	/**
	 * Searches through the savestate folder to look for the latest savestate<br>
	 * Loadstate equivalent to {@link SavestateHandler#getNextSaveFolderLocation(String)}
	 * @param worldname
	 * @return targetsavefolder
	 * @throws LoadstateException if there is no savestate or more than 300 savestates
	 */
	private File getLatestSavestateLocation(String worldname) throws LoadstateException {
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
	
	/**
	 * Get's the correct string of the loadstate, used in {@linkplain InputSavestatesHandler#loadstate(String)}
	 * 
	 * @param worldname the name of the world currently on the server
	 * @return The correct name of the next loadstate
	 */
	private String nameWhenLoading(String worldname) throws LoadstateException {
		int i=1;
		int limit=300;
		String name="";
		File targetsavefolder=null;
		while(i<=300) {
			targetsavefolder = new File(savestateDirectory, worldname+"-Savestate"+Integer.toString(i));
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
	private void createSavestateDirectory() {
		if(!server.isDedicatedServer()) {
			savestateDirectory=new File(server.getDataDirectory()+File.separator+"saves"+File.separator+"savestates"+File.separator);
		}else {
			savestateDirectory=new File(server.getDataDirectory()+File.separator+"savestates"+File.separator);
		}
		if(!savestateDirectory.exists()) {
			savestateDirectory.mkdir();
		}
	}
	
	private Map<Integer, File> savestateMap=new HashMap<Integer, File>();
	
	private int nextFreeIndex=0;
	
	private int currentIndex;
	
	public void refreshSavestateMap() {
		savestateMap.clear();
		File[] files=savestateDirectory.listFiles(new FileFilter() {
			
			@Override
			public boolean accept(File pathname) {
				return pathname.getName().startsWith(server.getFolderName()+"-Savestate");
			}
		});
		int index=0;
		for(File file:files) {
			try {
				index=Integer.parseInt(file.getName().substring(file.getName().length()-1));
			} catch (NumberFormatException e) {
				TASmod.logger.warn(String.format("Could not process the savestate %s", e.getMessage()));
			}
			savestateMap.put(index, file);
		}
		nextFreeIndex=index+1;
	}
	
	public void saveCurrentIndex() {
		File tasmodDir=new File(savestateDirectory, "../"+server.getFolderName()+"/tasmod/");
		if(!tasmodDir.exists()) {
			tasmodDir.mkdir();
		}
		File savestateDat=new File(tasmodDir, "savestate.data");
		List<String> lines=new ArrayList<String>();
		lines.add("currentIndex="+currentIndex);
		try {
			FileUtils.writeLines(savestateDat, lines);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void loadCurrentIndex() {
		int index = -1;
		List<String> lines = new ArrayList<String>();
		File tasmodDir = new File(savestateDirectory, "../" + server.getFolderName() + "/tasmod/");
		if (!tasmodDir.exists()) {
			tasmodDir.mkdir();
		}
		File savestateDat = new File(tasmodDir, "savestate.data");
		try {
			lines = FileUtils.readLines(savestateDat, StandardCharsets.UTF_8);
		} catch (IOException e) {
			TASmod.logger.warn("No savestate.data file found in current world folder, ignoring it");
		}
		if (!lines.isEmpty()) {
			for (String line : lines) {
				if (line.startsWith("currentIndex=")) {
					try {
						index = Integer.parseInt(line.split("=")[1]);
					} catch (NumberFormatException e) {
						e.printStackTrace();
					}
				}
			}
		}
		setCurrentIndex(index);;
	}

	private void setCurrentIndex(int index) {
		if(index<0) {
			currentIndex=nextFreeIndex-1;
		}else {
			currentIndex=index;
		}
		TASmod.logger.info("Setting the savestate index to {}", currentIndex);
	}
	
	public void increaseCurrentIndex() {
		setCurrentIndex(currentIndex+1);
	}
	
	private String getSavestateNameWithIndex(int index) {
		return server.getFolderName()+File.separator+"Savestate"+index;
	}
	
	/**
	 * Event, that gets executed after a loadstate operation was carried out, get's called on the server side
	 */
	public static void playerLoadSavestateEventServer() {
		PlayerList playerList=TASmod.getServerInstance().getPlayerList();
		EntityPlayerMP player=playerList.getPlayers().get(0);
		NBTTagCompound nbttagcompound = playerList.getPlayerNBT(player);
		//TODO Make this multiplayer compatible ffs
		SavestatePlayerLoading.reattachEntityToPlayer(nbttagcompound, player.getServerWorld(), player);
	}
	
	@SideOnly(Side.CLIENT)
	public static void playerLoadSavestateEventClient() {
		SavestatesChunkControl.addPlayerToChunk(Minecraft.getMinecraft().player);
	}
	
}