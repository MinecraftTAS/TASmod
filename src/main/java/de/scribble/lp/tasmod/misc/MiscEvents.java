package de.scribble.lp.tasmod.misc;

import java.io.File;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedOutEvent;

public class MiscEvents {
	public static boolean ignorerespawntimerClient=false;
	public static boolean ignorerespawntimerServer=false;
	
//	@SubscribeEvent
//	public void onMainMenu(GuiOpenEvent ev) {
//		if(ev.getGui() instanceof GuiMainMenu) {
//			((GuiMainMenu) ev.getGui()).minceraftRoll=0;
//			((GuiMainMenu) ev.getGui()).splashText="TaS iS cHeAtInG !!1";
//		}
//	}
//	@SubscribeEvent
//	public void onPlayerLoggedOut(PlayerLoggedOutEvent ev) {
//		if (!FMLCommonHandler.instance().getMinecraftServerInstance().isDedicatedServer()&&!Minecraft.getMinecraft().getIntegratedServer().getPublic()) {
//			File file = new File(Minecraft.getMinecraft().mcDataDir,
//					"saves" + File.separator + Minecraft.getMinecraft().getIntegratedServer().getFolderName()
//							+ File.separator + "miscthings.txt");
//			new MiscSaving().saveThings(ev.player, file);
//		}
//	}
	@SubscribeEvent
	public void onPlayerLoggedIn(PlayerLoggedInEvent ev) {
//		if (!FMLCommonHandler.instance().getMinecraftServerInstance().isDedicatedServer()&&!Minecraft.getMinecraft().getIntegratedServer().getPublic()) {
//			File file = new File(Minecraft.getMinecraft().mcDataDir,
//					"saves" + File.separator + Minecraft.getMinecraft().getIntegratedServer().getFolderName()
//							+ File.separator + "miscthings.txt");
//			if (file.exists()) {
//				ev.player.portalCounter=new MiscReapplying().getPortalTime(file);
//			}
//		}
		if(ev.player instanceof EntityPlayerMP) {
			if (ignorerespawntimerClient||ignorerespawntimerServer) {
				EntityPlayerMP playa=(EntityPlayerMP)ev.player;
				playa.respawnInvulnerabilityTicks=0;
				MiscEvents.ignorerespawntimerClient=false;
			}
		}
	}
}
