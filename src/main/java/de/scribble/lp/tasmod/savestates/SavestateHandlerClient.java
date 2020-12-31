package de.scribble.lp.tasmod.savestates;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import javax.annotation.Nullable;

import com.google.common.io.Files;

import de.scribble.lp.tasmod.CommonProxy;
import de.scribble.lp.tasmod.misc.MiscEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiIngameMenu;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.WorldSettings;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
/**
 * This code is heavily 'inspired' from <br> bspkrs on github <br> https://github.com/bspkrs/WorldStateCheckpoints/blob/master/src/main/java/bspkrs/worldstatecheckpoints/CheckpointManager.java <br>
 * but it's more fitted to quickly load and save the savestates and removes extra gui overview. Hey I changed this comment, and it actually supports multithreadding now...
 */
public class SavestateHandlerClient {
	Minecraft mc=Minecraft.getMinecraft();
	public static boolean isSaving=false;
	public static boolean isLoading=false;
	public static int savetimer;
	public static int loadtimer;
	
	private File currentworldfolder;
	private File targetsavefolder=null;
	private WorldSettings settings;
	private String foldername;
	private String worldname;
	private BufferedImage screenshot;
	private String screenshotname;
	private BufferedImage worldIcon;
	
	
	public void saveState() {
		if(!FMLCommonHandler.instance().getMinecraftServerInstance().isDedicatedServer()) {
			
			currentworldfolder = new File(Minecraft.getMinecraft().mcDataDir, "saves" + File.separator + Minecraft.getMinecraft().getIntegratedServer().getFolderName());
			targetsavefolder=null;
			worldname=Minecraft.getMinecraft().getIntegratedServer().getFolderName();
			
			List<EntityPlayerMP> players=FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayers();
			if (!isSaving && !isLoading) {
				isSaving = true;
				// Check for worlds in the savestate folder
				int i = 1;
				while (i <= 300) {
					if (i == 300) {
						CommonProxy.logger.error("Couldn't make a savestate, there are too many savestates in the target directory");
						isSaving = false;
						return;
					}
					if (i > 300) {
						CommonProxy.logger.error("Aborting saving due to savestate count being greater than 300 for safety reasons");
						isSaving = false;
						return;
					}
					targetsavefolder = new File(Minecraft.getMinecraft().mcDataDir,
							"saves" + File.separator + "savestates" + File.separator
									+ Minecraft.getMinecraft().getIntegratedServer().getFolderName() + "-Savestate"
									+ Integer.toString(i));
					if (!targetsavefolder.exists()) {
						screenshotname="Savestate "+i+".png";	//Setting the name of the savestate as the ScreenshotName
						break;
					}
					i++;
				}
				// Save the info file
				try {
					int[] incr = getInfoValues(getInfoFile(worldname));
					if (incr[0] == 0) {
						saveInfo(getInfoFile(worldname), null);
					} else {
						incr[0]++;
						saveInfo(getInfoFile(worldname), incr);
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
				SavestateSaveEventsClient Saver = new SavestateSaveEventsClient();
				Saver.start();
			}else {
				CommonProxy.logger.error("Saving savestate is blocked by another action. If this is permanent, restart the game.");
			}
		}
	}
	
	public void loadLastSavestate() {
		if(!FMLCommonHandler.instance().getMinecraftServerInstance().isDedicatedServer()) {
			if(!isSaving&&!isLoading) {
				isLoading=true;
				currentworldfolder = new File(Minecraft.getMinecraft().mcDataDir, "saves" + File.separator + mc.getIntegratedServer().getFolderName());
				foldername=mc.getIntegratedServer().getFolderName();
				worldname=Minecraft.getMinecraft().getIntegratedServer().getFolderName();
				//getting latest savestate
				int i=1;
				while(i<=300) {
					targetsavefolder = new File(Minecraft.getMinecraft().mcDataDir, "saves" + File.separator+"savestates"+File.separator+Minecraft.getMinecraft().getIntegratedServer().getFolderName()+"-Savestate"+Integer.toString(i));
					if (!targetsavefolder.exists()) {
						if(i-1==0) {
							CommonProxy.logger.info("Couldn't find a valid savestate, abort loading the savestate!");
							isLoading=false;
							return;
						}
						if(i>300) {
							CommonProxy.logger.error("Too many savestates found. Aborting loading for safety reasons");
							isLoading=false;
							return;
						}
						targetsavefolder = new File(Minecraft.getMinecraft().mcDataDir, "saves" + File.separator+"savestates"+File.separator+Minecraft.getMinecraft().getIntegratedServer().getFolderName()+"-Savestate"+Integer.toString(i-1));
						break;
					}
					i++;
				}
				try {
					int[] incr=getInfoValues(getInfoFile(worldname));
					incr[1]++;
					saveInfo(getInfoFile(worldname), incr);
				} catch (IOException e) {
					e.printStackTrace();
					isLoading = false;
				}
				this.mc.ingameGUI.getChatGUI().clearChatMessages(true);
				FMLCommonHandler.instance().firePlayerLoggedOut(FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayers().get(0));

				mc.displayGuiScreen(new GuiSavestateLoadingScreen());
				
				MinecraftForge.EVENT_BUS.register(new SavestateBrake(mc));
			}else {
				CommonProxy.logger.error("Loading savestate is blocked by another action. If this is permanent, restart the game.");
			}
		}
	}
	
	/**
     * Copy directory from source to target location recursively, ignoring strings in the "ignore" array. Target location will be created if
     * needed. Source directory is not copied, only its contents.
     * 
     * @param sourceLocation source
     * @param targetLocation target
     * @param ignore array of ignored names (strings)
     * @throws IOException
     */
    protected void copyDirectory(File sourceLocation, File targetLocation, String[] ignore) throws IOException
    {
        if (sourceLocation.isDirectory())
        {
            if (!targetLocation.exists())
                targetLocation.mkdirs();

            String[] children = sourceLocation.list();
            for (int i = 0; i < children.length; i++)
            {
                boolean ignored = false;	
                for (String str : ignore)
                    if (str.equals(children[i]))
                    {
                        ignored = true;
                        break;
                    }

                if (!ignored)
                    copyDirectory(new File(sourceLocation, children[i]), new File(targetLocation, children[i]), ignore);
            }
        }
        else
        {
            boolean ignored = false;
            for (String str : ignore)
                if (str.equals(sourceLocation.getName()))
                {
                    ignored = true;
                    break;
                }

            if (!ignored)
            {
                InputStream in = new FileInputStream(sourceLocation);
                OutputStream out = new FileOutputStream(targetLocation);

                // Copy the bits from instream to outstream
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0)
                    out.write(buf, 0, len);

                in.close();
                out.close();
            }
        }
    }

    /**
     * Delete directory contents recursively. Leaves the specified starting directory empty. Ignores files / dirs listed in "ignore" array.
     * 
     * @param dir directory to delete
     * @param ignore ignored files
     * @return true on success
     */
    protected boolean deleteDirContents(File dir, String[] ignore){
    	
        if (dir.isDirectory())
        {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++)
            {
                boolean ignored = false;
                for (String str : ignore)
                    if (str.equals(children[i]))
                    {
                        ignored = true;
                        break;
                    }

                if (!ignored)
                {
                    boolean success = deleteDirContents(new File(dir, children[i]), ignore);
                    if (!success)
                        return false;
                }
            }
        }
        else
        {
            dir.delete();
        }
        return true; 
    }

    public File getInfoFile(String worldname) {
    	if(!new File(Minecraft.getMinecraft().mcDataDir, "saves" + File.separator+"savestates").exists()) new File(Minecraft.getMinecraft().mcDataDir, "saves" + File.separator+"savestates").mkdir();
    	
    	File file = new File(Minecraft.getMinecraft().mcDataDir, "saves" + File.separator+"savestates"+File.separator+worldname+"-info.txt");
    	return file;
    }
    
    public int[] getInfoValues(File file) throws IOException {
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
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
    	}
    	return out;
    }
    public void saveInfo(File file,@Nullable int[] values) {
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
    public void displayLoadingScreen() {
    	if (mc.currentScreen instanceof GuiSavestateSavingScreen) {
			mc.displayGuiScreen(null);
		} else {
			mc.displayGuiScreen(new GuiSavestateSavingScreen());
		}
    }

	public void displayIngameMenu() {
		mc.displayGuiScreen(new GuiIngameMenu());
	}

	private class SavestateSaveEventsClient extends Thread {
		@Override
		public void run() {
			try {
				currentThread().sleep(savetimer);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
			try {
				copyDirectory(currentworldfolder, targetsavefolder, new String[] { " " });
			} catch (IOException e) {
				CommonProxy.logger.error("Could not copy the directory " + currentworldfolder.getPath() + " to "
						+ targetsavefolder.getPath() + " for some reason (Savestate save)");
				e.printStackTrace();
			} finally {
				isSaving = false;
			}
		}
	}

	/**
	 * Subscribes to the forge event bus for a brief amount of time to keep up a guiscreen. This is needed for the loadstate functions since errors occur when mc is closed when no guiscreen is up.
	 * 
	 * @author ScribbleLP
	 *
	 */
	private class SavestateBrake{
		int cooldown=loadtimer;
		Minecraft mc;
		public SavestateBrake(Minecraft mc) {
			this.mc=mc;
		}
		@SubscribeEvent
		public void event(TickEvent.RenderTickEvent ev) {
			if(ev.phase==Phase.START) {
				if (cooldown<=0) {
					this.mc.world.sendQuittingDisconnectingPacket();
					this.mc.loadWorld((WorldClient)null);
		            
		            SavestateLoadEventsClient Loader=new SavestateLoadEventsClient();
		            Loader.setName("Savestate Loader");
		            try {
		            	Loader.start();
						Loader.join();
					} catch (InterruptedException e) {
						e.printStackTrace();
						isLoading = false;
					} catch (Exception e){
						e.printStackTrace();
					}
		            
		            MiscEvents.ignorerespawntimerClient=true; //Make it so the Player is vulnerable after a savestate
		            
		            isLoading = false;
		            MinecraftForge.EVENT_BUS.unregister(this);
		            FMLClientHandler.instance().getClient().launchIntegratedServer(foldername, worldname, null);
				}
				cooldown--;
			}
		}
	}
	private class SavestateLoadEventsClient extends Thread {
		@Override
		public void run() {
			while (mc.isIntegratedServerRunning()) {
				try {
					Thread.sleep(2L);
				} catch (InterruptedException e) {
					e.printStackTrace();
					isLoading = false;
				}
			}
			deleteDirContents(currentworldfolder, new String[] { " " });
			try {
				copyDirectory(targetsavefolder, currentworldfolder, new String[] { " " });
			} catch (IOException e) {
				CommonProxy.logger.error("Could not copy the directory " + currentworldfolder.getPath() + " to "
						+ targetsavefolder.getPath() + " for some reason (Savestate load)");
				e.printStackTrace();
				return;
			} finally {
				isLoading = false;
			}
		}
	}
}
