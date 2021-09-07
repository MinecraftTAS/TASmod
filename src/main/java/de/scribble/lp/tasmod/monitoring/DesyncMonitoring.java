package de.scribble.lp.tasmod.monitoring;

import java.util.ArrayList;
import java.util.List;

import de.scribble.lp.tasmod.inputcontainer.InputContainer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.util.text.TextFormatting;

/**
 * Stores the players position during recording and compares it with the position during playback
 * @author ScribbleLP
 *
 */
public class DesyncMonitoring {
	private List<String> pos = new ArrayList<String>();
	
	private String lastDesync="";
	
	private String x;
	private String y;
	private String z;
	
	private String Mx;
	private String My;
	private String Mz;


	public void capturePosition() {
		EntityPlayerSP player = Minecraft.getMinecraft().player;
		if (player != null) {
			pos.add(player.posX + " " + player.posY + " " + player.posZ + " " + player.motionX + " " + player.motionY + " " + player.motionZ);
		}
	}

	public List<String> getPos() {
		return pos;
	}

	public void setPos(List<String> pos) {
		this.pos = pos;
	}

	public String get(int index) {
		String out = "";

		try {
			out = pos.get(index);
		} catch (IndexOutOfBoundsException e) {
			return "";
		}
		return out;
	}

	public String getMonitoring(InputContainer inputContainer, EntityPlayerSP player) {
		int index = inputContainer.index() - 1;
		String position = get(index);

		if (position.isEmpty()) {
			clearDelta();
			return TextFormatting.GRAY + "Empty";
		}
		
		if(!inputContainer.isPlayingback()&&!inputContainer.isRecording()) {
			return lastDesync;
		}
		String[] split = position.split(" ");
		double x = 0;
		double y = 0;
		double z = 0;
		double mx = 0;
		double my = 0;
		double mz = 0;
		try {
			x = Double.parseDouble(split[0]);
			y = Double.parseDouble(split[1]);
			z = Double.parseDouble(split[2]);
			mx = Double.parseDouble(split[3]);
			my = Double.parseDouble(split[4]);
			mz = Double.parseDouble(split[5]);
		} catch (Exception e) {
			return TextFormatting.DARK_PURPLE + "Error";
		}

		boolean isEqual = player.posX == x && player.posY == y && player.posZ == z && player.motionX == mx && player.motionY == my && player.motionZ == mz;

		if (isEqual) {
			this.x="";
			this.y="";
			this.z="";
			Mx="";
			My="";
			Mz="";
			lastDesync=TextFormatting.GREEN + "In sync";
			return lastDesync;
		} else {
			double dx = 0D;
			double dy = 0D;
			double dz = 0D;
			double dMx = 0D;
			double dMy = 0D;
			double dMz = 0D;

			dx = player.posX - x;
			dy = player.posY - y;
			dz = player.posZ - z;

			dMx = player.motionX - mx;
			dMy = player.motionY - my;
			dMz = player.motionZ - mz;

			boolean isWarning = Math.abs(dx) < 0.00001 && Math.abs(dy) < 0.00001 && Math.abs(dz) < 0.00001 && Math.abs(dMx) < 0.00001 && Math.abs(dMy) < 0.00001 && Math.abs(dMz) < 0.00001;

			if (dx != 0D) {
				TextFormatting format=desyncColor(dx);
				this.x=format+" X: " + dx;
			}else {
				this.x="";
			}
			if (dy != 0D) {
				TextFormatting format=desyncColor(dy);
				this.y=format+" Y: " + dy;
			}else {
				this.y="";
			}
			if (dz != 0D) {
				TextFormatting format=desyncColor(dz);
				this.z=format+" Z: " + dz;
			}else {
				this.z="";
			}
			if (dMx != 0D) {
				TextFormatting format=desyncColor(dMx);
				this.Mx=format+" MotionX: " + dMx;
			}else {
				this.Mx="";
			}
			if (dMy != 0D) {
				TextFormatting format=desyncColor(dMy);
				this.My=format+" MotionY: " + dMy;
			}else {
				this.My="";
			}
			if (dMz != 0D) {
				TextFormatting format=desyncColor(dMz);
				this.Mz=format+" MotionZ: " + dMz;
			}else {
				this.Mz="";
			}
			
			if (isWarning) {
				lastDesync = TextFormatting.YELLOW + "Slight desync ";
				return lastDesync;
			}
			
			boolean isModerate = Math.abs(dx) < 0.01 && Math.abs(dy) < 0.01 && Math.abs(dz) < 0.01 && Math.abs(dMx) < 0.01 && Math.abs(dMy) < 0.01 && Math.abs(dMz) < 0.01;
			
			if (isModerate) {
				lastDesync = TextFormatting.RED + "Moderate desync ";
				return lastDesync;
			}
			
			lastDesync = TextFormatting.DARK_RED + "Total desync ";
			return lastDesync;
		}
	}
	
	private TextFormatting desyncColor(double val) {
		val=Math.abs(val);
		if(val>0&&val<0.00001) {
			return TextFormatting.YELLOW;
		}else if(val>0.00001&&val<0.01){
			return TextFormatting.RED;
		}else {
			return TextFormatting.DARK_RED;
		}
	}
	
	public String getX() {
		return x;
	}

	public String getY() {
		return y;
	}

	public String getZ() {
		return z;
	}

	public String getMx() {
		return Mx;
	}

	public String getMy() {
		return My;
	}

	public String getMz() {
		return Mz;
	}
	
	private void clearDelta(){
		x="";
		y="";
		z="";
		Mx="";
		My="";
		Mz="";}
}
