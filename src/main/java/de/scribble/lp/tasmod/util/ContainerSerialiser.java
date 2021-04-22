package de.scribble.lp.tasmod.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import de.pfannekuchen.killtherng.utils.EntityRandom;
import de.pfannekuchen.killtherng.utils.ItemRandom;
import de.scribble.lp.tasmod.recording.FileThread;
import de.scribble.lp.tasmod.virtual.container.InputContainer;
import net.minecraft.client.Minecraft;

public class ContainerSerialiser {
	
	public String startPosition;
	
	public void saveToFileV1(File file, InputContainer container) throws FileNotFoundException {
		FileThread fileThread=new FileThread(file, false);
		
		fileThread.addLine("################################################# TASFile ###################################################\n"
				 + "#							This file was generated using the Minecraft TASMod								#\n"
				 + "#																											#\n"
				 + "#	If you make a mistake in this file, the mod will notify you via the console, so it's best to keep the	#\n"
				 + "#										console open at all times											#\n"
				 + "#																											#\n"
				 + "#------------------------------------------------ Header ---------------------------------------------------#\n"
				 + "#Author:" + container.getAuthors()+ "\n"
				 + "#																											#\n"
				 + "#Title:" + container.getTitle()+"\n"
				 + "#																											#\n"
				 + "#Playing Time:" + container.getPlaytime() + "\n"
				 + "#																											#\n"
				 + "#Rerecords:"+container.getRerecords()
				 + "#																											#\n"
				 + "#----------------------------------------------- Settings --------------------------------------------------#\n"
				 + "#Entity Seed:" + EntityRandom.currentSeed.get() + "\n"
				 + "#Item Seed:" + ItemRandom.currentSeed.get() + "\n"
				 + "#StartPosition:"
				 + "#############################################################################################################\n");
	}
	
	private static String getStartLocation() {
		Minecraft mc= Minecraft.getMinecraft();
		String pos = mc.player.getPositionVector().toString();
		pos = pos.replace("(", "");
		pos = pos.replace(")", "");
		pos = pos.replace(" ", "");
		String pitch = Float.toString(mc.player.rotationPitch);
		String yaw = Float.toString(mc.player.rotationYaw);
		return pos + "," + yaw + "," + pitch;
	}
}
