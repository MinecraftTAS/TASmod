package de.scribble.lp.tasmod.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

import org.apache.commons.io.FileUtils;

import com.dselent.bigarraylist.BigArrayList;

import de.pfannekuchen.killtherng.utils.EntityRandom;
import de.pfannekuchen.killtherng.utils.ItemRandom;
import de.scribble.lp.tasmod.recording.FileThread;
import de.scribble.lp.tasmod.virtual.container.InputContainer;
import de.scribble.lp.tasmod.virtual.container.TickInputContainer;
import net.minecraft.client.Minecraft;

public class ContainerSerialiser {
	
	public void saveToFileV1(File file, InputContainer container) throws FileNotFoundException {
		if(container.size()==0) {
			return;
		}
		FileThread fileThread=new FileThread(file, false);
		
		fileThread.start();
		
		fileThread.addLine("################################################# TASFile ###################################################\n"
				 + "#												Version:1													#\n"
				 + "#							This file was generated using the Minecraft TASMod								#\n"
				 + "#																											#\n"
				 + "#	If you make a mistake in this file, the mod will notify you via the console, so it's best to keep the	#\n"
				 + "#										console open at all times											#\n"
				 + "#																											#\n"
				 + "#------------------------------------------------ Header ---------------------------------------------------#\n"
				 + "#Author:" + container.getAuthors() + "\n"
				 + "#																											#\n"
				 + "#Title:" + container.getTitle() + "\n"
				 + "#																											#\n"
				 + "#Playing Time:" + container.getPlaytime() + "\n"
				 + "#																											#\n"
				 + "#Rerecords:"+container.getRerecords() + "\n"
				 + "#																											#\n"
				 + "#----------------------------------------------- Settings --------------------------------------------------#\n"
				 + "#Entity Seed:" + EntityRandom.currentSeed.get() + "\n"
				 + "#Item Seed:" + ItemRandom.currentSeed.get() + "\n"
				 + "#StartPosition:\n"
				 + "#############################################################################################################\n");
		
		BigArrayList<TickInputContainer> ticks= container.getInputs();
		for (int i = 0; i < ticks.size(); i++) {
			TickInputContainer tick=ticks.get(i);
			fileThread.addLine(tick.toString()+"\n");
		}
		fileThread.close();
	}

	public int getFileVersion(File file) throws IOException {
		List<String> lines = FileUtils.readLines(file, Charset.defaultCharset());
		for (String line : lines) {
			if (line.contains("Version")) {
				String trimmed = line.replaceAll("#| ", "");
				return Integer.parseInt(trimmed.split(":")[1]);
			}
		}
		return 0;
	}

	public InputContainer fromFileV1(File file) {
		return null;
	}

	private static String getStartLocation() {
		Minecraft mc = Minecraft.getMinecraft();
		String pos = mc.player.getPositionVector().toString();
		pos = pos.replace("(", "");
		pos = pos.replace(")", "");
		pos = pos.replace(" ", "");
		String pitch = Float.toString(mc.player.rotationPitch);
		String yaw = Float.toString(mc.player.rotationYaw);
		return pos + "," + yaw + "," + pitch;
	}
}
