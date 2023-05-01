package com.minecrafttas.tasmod.monitoring;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;

import com.dselent.bigarraylist.BigArrayList;
import com.minecrafttas.killtherng.custom.CustomRandom;
import com.minecrafttas.tasmod.TASmod;
import com.minecrafttas.tasmod.playback.PlaybackController;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.util.text.TextFormatting;

/**
 * Stores the players position during recording and compares it with the position during playback
 * @author Scribble
 *
 */
public class DesyncMonitoring {
	
	private File tempDir = new File(Minecraft.getMinecraft().mcDataDir.getAbsolutePath() + File.separator + "saves" + File.separator + "tasfiles" + File.separator + "temp" + File.separator + "monitoring");
	
	private BigArrayList<MonitorContainer> container = new BigArrayList<MonitorContainer>(tempDir.toString());
	
	private MonitorContainer currentValues;
	
	private PlaybackController controller;
	
	/**
	 * Creates an empty desync monitor
	 * @param playbackController 
	 */
	public DesyncMonitoring(PlaybackController playbackController) {
		controller = playbackController;
	}
	
	/**
	 * Parses lines and fills the desync monitor
	 * @param playbackController 
	 * @param monitorLines
	 */
	public DesyncMonitoring(PlaybackController playbackController, List<String> monitorLines) throws IOException{
		this(playbackController);
		container = loadFromFile(monitorLines);
	}
	
	public void recordNull(int index) {
		if(container.size()<=index) {
			container.add(new MonitorContainer(index));
		} else {
			container.set(index, new MonitorContainer(index));
		}
	}
	
	public void recordMonitor(int index) {
		EntityPlayerSP player = Minecraft.getMinecraft().player;
		MonitorContainer values = null;
		if(player != null) {
			values = new MonitorContainer(index, player.posX, player.posY, player.posZ, player.motionX, player.motionY, player.motionZ, TASmod.ktrngHandler.getGlobalSeedClient());
		} else {
			values = new MonitorContainer(index, 0D, 0D, 0D, 0D, 0D, 0D, 0L);
		}
		
		if(container.size()<=index) {
			container.add(values);
		} else {
			container.set(index, values);
		}
	}
	
	public void playMonitor(int index) {
		currentValues = get(index-1);
	}
	
	private BigArrayList<MonitorContainer> loadFromFile(List<String> monitorLines) throws IOException {
		BigArrayList<MonitorContainer> out = new BigArrayList<MonitorContainer>(tempDir.toString());
		int linenumber = 0;
		for(String line : monitorLines) {
			linenumber++;
			String[] split = line.split(" ");
			double x = 0;
			double y = 0;
			double z = 0;
			double mx = 0;
			double my = 0;
			double mz = 0;
			long seed = 0;
			try {
				x = Double.parseDouble(split[0]);
				y = Double.parseDouble(split[1]);
				z = Double.parseDouble(split[2]);
				mx = Double.parseDouble(split[3]);
				my = Double.parseDouble(split[4]);
				mz = Double.parseDouble(split[5]);
				seed = Long.parseLong(split[6]);
			} catch (Exception e) {
				e.printStackTrace();
				throw new IOException("Error in monitoring section in line "+ linenumber + ". Some value is not a number");
			}
			out.add(new MonitorContainer(linenumber, x, y, z, mx, my, mz, seed));
		}
		return out;
	}
	
	public MonitorContainer get(int index) {
		try {
			return container.get(index);
		} catch (IndexOutOfBoundsException e) {
			return null;
		}
	}
	
	private String lastStatus = TextFormatting.GRAY + "Empty";
	
	public String getStatus(EntityPlayerSP player) {
		if (!controller.isNothingPlaying()) {
			if (currentValues != null) {
				double[] playervalues = new double[6];
				playervalues[0] = player.posX;
				playervalues[1] = player.posY;
				playervalues[2] = player.posZ;
				playervalues[3] = player.motionX;
				playervalues[4] = player.motionY;
				playervalues[5] = player.motionZ;
				DesyncStatus status = currentValues.getSeverity(controller.index(), playervalues, TASmod.ktrngHandler.getGlobalSeedClient());
				lastStatus = status.getFormat() + status.getText();
			} else {
				lastStatus = TextFormatting.GRAY + "Empty";
			}
		}
		return lastStatus;
	}
	
	private String lastPos = "";
	
	public String getPos() {
		if(currentValues!=null && !controller.isNothingPlaying()) {
			EntityPlayerSP player = Minecraft.getMinecraft().player;
			String[] values = new String[3];
			values[0]=getFormattedString(player.posX-currentValues.values[0]);
			values[1]=getFormattedString(player.posY-currentValues.values[1]);
			values[2]=getFormattedString(player.posZ-currentValues.values[2]);
			
			String out = "";
			for (String val : values) {
				if(val !=null) {
					out+=val+" ";
				}
			}
			lastPos=out;
		}
		return lastPos;
	}
	
	private String lastMotion = "";
	
	public String getMotion() {
		if(currentValues!=null && !controller.isNothingPlaying()) {
			EntityPlayerSP player = Minecraft.getMinecraft().player;
			String[] values = new String[3];
			values[0] = getFormattedString(player.motionX - currentValues.values[3]);
			values[1] = getFormattedString(player.motionY - currentValues.values[4]);
			values[2] = getFormattedString(player.motionZ - currentValues.values[5]);

			String out = "";
			for (String val : values) {
				if (val != null) {
					out+=val+" ";
				}
			}
			lastMotion = out;
		}
		return lastMotion;
	}
	
	private String lastSeed = "";
	
	public String getSeed() {
		if(currentValues!=null && !controller.isNothingPlaying()) {
			if(currentValues.seed == TASmod.ktrngHandler.getGlobalSeedClient()) {
				lastSeed = "";
			} else {
				if(TASmod.ktrngHandler.isLoaded()) {
					long distance = CustomRandom.distance(currentValues.seed, TASmod.ktrngHandler.getGlobalSeedClient());
					if(distance == 0L) {
						lastSeed = "";
					} else {
						lastSeed = DesyncStatus.SEED.format+Long.toString(distance);
					}
				} else {
					lastSeed = DesyncStatus.SEED.format+"TAS was recorded with KillTheRNG";
				}
			}
			
		}
		return lastSeed;
	}
	
	private String getFormattedString(double delta) {
		String out = "";
		if(delta != 0D) {
			DesyncStatus status = DesyncStatus.fromDelta(delta);
			if(status == DesyncStatus.EQUAL) {
				return "";
			}
			out = status.getFormat() + Double.toString(delta);
		}
		return out;
	}
	
	public class MonitorContainer implements Serializable{
		private static final long serialVersionUID = -3138791930493647885L;
		
		int index;

		double[] values = new double[6];
		
		long seed;
		
		
		public MonitorContainer(int index, double posx, double posy, double posz, double velx, double vely, double velz, long seed) {
			this.index = index;
			this.values[0] = posx;
			this.values[1] = posy;
			this.values[2] = posz;
			this.values[3] = velx;
			this.values[4] = vely;
			this.values[5] = velz;
			this.seed = seed;
		}

		public MonitorContainer(int index) {
			this(index, 0, 0, 0, 0, 0, 0, 0);
		}

		@Override
		public String toString() {
			return String.format("%s %s %s %s %s %s %s", values[0], values[1], values[2], values[3], values[4], values[5], seed);
		}
		
		public DesyncStatus getSeverity(int index, double[] playerValues, long seed) {
			
			if(this.seed != seed) {
				if(TASmod.ktrngHandler.isLoaded()) {
					if(CustomRandom.distance(this.seed, seed)!=1) {
						return DesyncStatus.SEED;
					}
				} else {
					return DesyncStatus.SEED;
				}
			}
			
			DesyncStatus out = null;
			
			for (int i = 0; i < values.length; i++) {
				double delta = 0;
				try {
					delta = playerValues[i] - values[i];
				} catch (Exception e) {
					return DesyncStatus.ERROR;
				}
				DesyncStatus status = DesyncStatus.fromDelta(delta);
				if(out==null || status.getSeverity() > out.getSeverity()) {
					out = status;
				}
			}
			
			return out;
		}
	}
	
	public enum DesyncStatus {
		EQUAL(0, TextFormatting.GREEN, "In sync", 0D),
		WARNING(1, TextFormatting.YELLOW, "Slight desync", 0.00001D),
		MODERATE(2, TextFormatting.RED, "Moderate desync", 0.01D),
		TOTAL(3, TextFormatting.DARK_RED, "Total desync"),
		SEED(3, TextFormatting.DARK_PURPLE, "RNG Seed desync"),
		ERROR(3, TextFormatting.DARK_PURPLE, "ERROR");
		
		private Double tolerance;
		private int severity;
		private String text;
		private TextFormatting format;
		
		private DesyncStatus(int severity, TextFormatting color, String text) {
			this.severity = severity;
			this.format = color;
			this.text = text;
			tolerance = null;
		}
		
		private DesyncStatus(int severity, TextFormatting color, String text, double tolerance) {
			this(severity, color, text);
			this.tolerance=tolerance;
		}
		
		public static DesyncStatus fromDelta(double delta) {
			DesyncStatus out = TOTAL;
			for(DesyncStatus status : values()) {
				if(status.tolerance == null) {
					return status;
				}
				if(Math.abs(delta)<status.tolerance) {
					break;
				}
				if(Math.abs(delta)>=status.tolerance) {
					out = status;
				}
			}
			return out;
		}
		
		public TextFormatting getFormat() {
			return format;
		}
		
		public int getSeverity() {
			return severity;
		}
		
		public String getText() {
			return text;
		}
	}

	public void clear() {
		currentValues=null;
		container = new BigArrayList<MonitorContainer>(tempDir.toString());
		lastStatus = TextFormatting.GRAY+"Empty";
		lastPos = "";
		lastMotion = "";
		lastSeed = "";
	}
}
