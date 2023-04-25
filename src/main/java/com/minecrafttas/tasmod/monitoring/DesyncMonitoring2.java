package com.minecrafttas.tasmod.monitoring;

import java.io.File;
import java.io.Serializable;
import java.util.List;

import com.dselent.bigarraylist.BigArrayList;
import com.minecrafttas.tasmod.ClientProxy;
import com.minecrafttas.tasmod.TASmod;
import com.minecrafttas.tasmod.playback.PlaybackController;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;

/**
 * Stores the players position during recording and compares it with the position during playback
 * @author Scribble
 *
 */
public class DesyncMonitoring2 {
	
	private File tempDir = new File(Minecraft.getMinecraft().mcDataDir.getAbsolutePath() + File.separator + "saves" + File.separator + "tasfiles" + File.separator + "temp" + File.separator + "monitoring");
	
	private BigArrayList<MonitorContainer> container = new BigArrayList<MonitorContainer>(tempDir.toString());
	
	private MonitorContainer currentValues;
	
	private PlaybackController controller;
	
	/**
	 * Creates an empty desync monitor
	 */
	public DesyncMonitoring2() {
		controller = ClientProxy.virtual.getContainer();
	}
	
	/**
	 * Parses lines and fills the desync monitor
	 * @param monitorLines
	 */
	public DesyncMonitoring2(List<String> monitorLines) {
		this();
		container = loadFromFile(monitorLines);
	}
	
	public void recordMonitor(int index) {
		EntityPlayerSP player = Minecraft.getMinecraft().player;
		MonitorContainer values = null;
		if(player != null) {
			values = new MonitorContainer(player.posX, player.posY, player.posZ, player.motionX, player.motionY, player.motionZ, TASmod.ktrngHandler.getGlobalSeedClient());
		} else {
			values = new MonitorContainer(0D, 0D, 0D, 0D, 0D, 0D, 0L);
		}
		
		if(container.size()<=index) {
			container.add(values);
		} else {
			container.set(index, values);
		}
	}
	
	public void playMonitor(int index) {
		currentValues = get(index);
	}
	
	private BigArrayList<MonitorContainer> loadFromFile(List<String> monitorLines) {
		return null;
	}
	
	public MonitorContainer get(int index) {

		try {
			return container.get(index);
		} catch (IndexOutOfBoundsException e) {
			return null;
		}
	}
	
	public String compare() {
		if(currentValues!=null) {
			
		}
		return null;
	}
	
	public class MonitorContainer implements Serializable{
		private static final long serialVersionUID = -3138791930493647885L;
		
		double posx;
		double posy;
		double posz;
		
		double velx;
		double vely;
		double velz;
		
		long seed;
		
		public MonitorContainer(double posx, double posy, double posz, double velx, double vely, double velz, long seed) {
			this.posx = posx;
			this.posy = posy;
			this.posz = posz;
			this.velx = velx;
			this.vely = vely;
			this.velz = velz;
			this.seed = seed;
		}

		@Override
		public String toString() {
			return String.format("%s %s %s %s %s %s %s", posx, posy, posz, velx, vely, velz, seed);
		}
	}
}
