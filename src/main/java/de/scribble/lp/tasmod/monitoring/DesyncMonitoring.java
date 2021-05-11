package de.scribble.lp.tasmod.monitoring;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.util.text.TextFormatting;

public class DesyncMonitoring {
	private List<String> pos= new ArrayList<String>();
	
	public void capturePosition() {
		EntityPlayerSP player=Minecraft.getMinecraft().player;
		if(player!=null) {
			pos.add(player.posX+" "+player.posY+" "+player.posZ);
		}
	}
	
	public List<String> getPos() {
		return pos;
	}
	
	public void setPos(List<String> pos) {
		this.pos = pos;
	}
	
	public String get(int index) {
		String out="";
		
		try {
			out=pos.get(index);
		}catch (IndexOutOfBoundsException e) {
			return "";
		}
		return out;
	}
	
	public String getMonitoring(int index, EntityPlayerSP player) {
		String position=get(index);
		
		if(position.isEmpty()) {
			return "";
		}
		String[] split= position.split(" ");
		double x=0;
		double y=0;
		double z=0;
		try {
			x=Double.parseDouble(split[0]);
			y=Double.parseDouble(split[1]);
			z=Double.parseDouble(split[2]);
		} catch (Exception e) {
		}
		
		if(player.posX==x&&player.posY==y&&player.posZ==z) {
			return TextFormatting.GREEN+position;
		}else {
			return TextFormatting.RED+position;
		}
		
	}
}
