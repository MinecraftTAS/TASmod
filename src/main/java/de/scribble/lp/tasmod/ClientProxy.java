package de.scribble.lp.tasmod;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class ClientProxy extends CommonProxy{
	
	public void preInit(FMLPreInitializationEvent ev) {
		super.preInit(ev);
	}
	public void init(FMLInitializationEvent ev) {
		MinecraftForge.EVENT_BUS.register(new InfoGui());
		super.init(ev);
	}
	public void postInit(FMLPostInitializationEvent ev) {
		super.postInit(ev);
	}
}
