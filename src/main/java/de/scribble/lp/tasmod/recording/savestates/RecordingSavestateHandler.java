package de.scribble.lp.tasmod.recording.savestates;

import java.io.File;
import java.io.IOException;

import de.scribble.lp.tasmod.ClientProxy;
import de.scribble.lp.tasmod.CommonProxy;
import de.scribble.lp.tasmod.savestates.exceptions.SavestateException;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Creating savestates of the recordings on the client<br>
 * Side: Client
 * @author ScribbleLP
 */
@SideOnly(Side.CLIENT)
public class RecordingSavestateHandler {
	
	private static File savestateDirectory=new File(ClientProxy.tasdirectory+File.separator+"savestates");
	
	/**
	 * Makes a copy of the recording that is currently running. Gets triggered when a savestate is made on the server <br>
	 * Side: Client
	 * @param nameOfSavestate coming from the server
	 * @throws SavestateException
	 * @throws IOException
	 */
	public static void savestateRecording(String nameOfSavestate) throws SavestateException, IOException {
		if(!ClientProxy.virtual.getContainer().isRecording()) {
			CommonProxy.logger.info("No recording savestate made since no recording is running");
			return;
		}
		
		if(nameOfSavestate.isEmpty()) {
			CommonProxy.logger.error("No recording savestate loaded since the name of savestate is empty");
			return;
		}
		
		createSavestateDirectory();
		
		File targetfile=new File(savestateDirectory, nameOfSavestate+".tas");
		
		ClientProxy.serialiser.saveToFileV1(targetfile, ClientProxy.virtual.getContainer());
	}
	
	private static void createSavestateDirectory() {
		if(!savestateDirectory.exists()) {
			savestateDirectory.mkdir();
		}
	}
	
	/**
	 * Makes replaces the current recording with the recording from the savestate. Gets triggered when a savestate is loaded on the server<br>
	 * Side: Client
	 * @param nameOfSavestate
	 * @throws IOException
	 */
	public static void loadRecording(String nameOfSavestate) throws IOException {
		
		if(!ClientProxy.virtual.getContainer().isRecording()) {
			CommonProxy.logger.info("No recording savestate made since no recording is running");
			return;
		}
		
		if(nameOfSavestate.isEmpty()) {
			CommonProxy.logger.error("No recording savestate loaded since the name of savestate is empty");
			return;
		}
		
		createSavestateDirectory();
		
		File targetfile=new File(savestateDirectory, nameOfSavestate+".tas");
		
		if(!targetfile.exists()) {
			ClientProxy.virtual.getContainer().clear();
		}
		else {
			ClientProxy.virtual.setContainer(ClientProxy.serialiser.fromEntireFileV1(targetfile));
			ClientProxy.virtual.getContainer().setIndexToSize();
			ClientProxy.virtual.getContainer().fixTicks();
			ClientProxy.virtual.getContainer().setRecording(true);
		}
	}
}
