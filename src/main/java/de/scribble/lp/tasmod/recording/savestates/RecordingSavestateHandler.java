package de.scribble.lp.tasmod.recording.savestates;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

import de.scribble.lp.tasmod.CommonProxy;
import de.scribble.lp.tasmod.recording.InputRecorder;
import de.scribble.lp.tasmod.savestates.exceptions.SavestateException;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Creating savestates on the client
 * @author ScribbleLP
 *
 */
@SideOnly(Side.CLIENT)
public class RecordingSavestateHandler {
	private static File savestateDirectory=new File(Minecraft.getMinecraft().mcDataDir, "saves"+File.separator+"tasfiles"+File.separator+"savestates");
	
	public static void savestateRecording(String nameOfSavestate) throws SavestateException, IOException {
		if(!InputRecorder.isRecording()) {
			CommonProxy.logger.debug("No recording savestate made since no recording is running");
			return;
		}
		if(nameOfSavestate.isEmpty()) {
			CommonProxy.logger.error("No savestate was made, name of savestate is empty");
			return;
		}
		InputRecorder.setPause(true);
		InputRecorder.saveFile();
		
		createSavestateDirectory();
		
		File targetfile=new File(savestateDirectory,InputRecorder.getFilename() + "-"+nameOfSavestate+".tas");
		File currentfile=InputRecorder.getFileLocation();
		
		FileUtils.copyFile(currentfile, targetfile);
		
		InputRecorder.setPause(false);
	}
	
	private static void createSavestateDirectory() {
		if(!savestateDirectory.exists()) {
			savestateDirectory.mkdir();
		}
	}
	
	public static void loadRecording(String nameOfSavestate) throws IOException {
		if(!InputRecorder.isRecording()) {
			CommonProxy.logger.debug("No recording savestate loaded since no recording is running");
			return;
		}
		if(nameOfSavestate.isEmpty()) {
			CommonProxy.logger.error("No recording savestate loaded since the name of savestate is empty");
			return;
		}
		InputRecorder.prepareForRewind();
		
		createSavestateDirectory();
		
		File targetfile=new File(savestateDirectory,InputRecorder.getFilename() + "-"+nameOfSavestate+".tas");
		
		if(!targetfile.exists()) {
			InputRecorder.startRecording(InputRecorder.getFilename());
		}else {
			File currentfolder=InputRecorder.getFileLocation();
			FileUtils.copyFile(targetfile, currentfolder);
			InputRecorder.appendRecording(InputRecorder.getFilename());
		}
	}
}
