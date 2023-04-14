package com.minecrafttas.tasmod.events;


import java.io.IOException;

import com.minecrafttas.tasmod.ClientProxy;
import com.minecrafttas.tasmod.TASmod;
import com.minecrafttas.tasmod.inputcontainer.server.InitialSyncStatePacket;
import com.minecrafttas.tasmod.networking.TASmodNetworkClient;
import com.minecrafttas.tasmod.tickratechanger.TickrateChangerClient;
import com.minecrafttas.tasmod.tickratechanger.TickrateChangerServer;
import com.mojang.authlib.GameProfile;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class PlayerJoinLeaveEvents {
	
	/**
	 * Executes when a player joined the server on the server side
	 * 
	 * @param player The player that joined the server
	 */
	public static void firePlayerJoinedServerSide(EntityPlayerMP player) {
		TASmod.logger.info("Firing login events for {} on the SERVER", player.getName());
		TickrateChangerServer.joinServer(player);
	}
	
	/**
	 * Executes when a player left the server on the server side
	 * 
	 * @param player The player that left the server
	 */
	public static void firePlayerLeaveServerSide(EntityPlayerMP player) {
//		TASmod.logger.info("Firing logout events for {} on the SERVER", player.getName());
		TASmod.containerStateServer.leaveServer(player);
	}

	/**
	 * Executes when the player joins the server on the client side
	 * @param player The singleplayer player
	 */
	@SideOnly(Side.CLIENT)
	public static void firePlayerJoinedClientSide(net.minecraft.client.entity.EntityPlayerSP player) {
		TASmod.logger.info("Firing login events for {} on the CLIENT", player.getName());
		
		Minecraft mc = Minecraft.getMinecraft();
		
		if(mc.isIntegratedServerRunning())
			ClientProxy.packetClient = new TASmodNetworkClient(TASmod.logger);
		else
			ClientProxy.packetClient = new TASmodNetworkClient(TASmod.logger, mc.getCurrentServerData().serverIP, 3111);
		
		ClientProxy.packetClient.sendToServer(new InitialSyncStatePacket(ClientProxy.virtual.getContainer().getState()));
		
		ClientProxy.virtual.unpressNext();
		ClientProxy.shieldDownloader.onPlayerJoin(player.getGameProfile());
		TickrateChangerClient.joinServer();
		ClientProxy.virtual.getContainer().printCredits();
		
		TASmod.ktrngHandler.setInitialSeed();
		TASmod.ktrngHandler.setUpdating(true);
	}
	
	/**
	 * Executes when the player leaves the server on the client side
	 * @param player
	 */
	@SideOnly(Side.CLIENT)
	public static void firePlayerLeaveClientSide(net.minecraft.client.entity.EntityPlayerSP player) {
		TASmod.logger.info("Firing logout events for {} on the CLIENT", player.getName());
		TASmod.ktrngHandler.setUpdating(false);
		try {
			if(ClientProxy.packetClient!=null) {
				ClientProxy.packetClient.killClient();
				ClientProxy.packetClient=null;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * When a player joins the world... Github workflows break without this, else I would use {@link #fireOtherPlayerJoinedClientSide(GameProfile)}
	 * @param event
	 */
	@SubscribeEvent
	public void fireOtherPlayerJoinedClientSide(EntityJoinWorldEvent event) {
		if ((event.getWorld().isRemote) && ((event.getEntity() instanceof net.minecraft.entity.player.EntityPlayer))){
			GameProfile profile = ((net.minecraft.entity.player.EntityPlayer)event.getEntity()).getGameProfile();
			TASmod.logger.info("Firing other login events for {} on the CLIENT", profile.getName());
			ClientProxy.shieldDownloader.onPlayerJoin(profile);
		}
	}
	
	/**
	 * When any player joins the world on the client
	 * @param profile
	 */
	public static void fireOtherPlayerJoinedClientSide(GameProfile profile) {
		TASmod.logger.info("Firing other login events for {} on the CLIENT", profile.getName());
		ClientProxy.shieldDownloader.onPlayerJoin(profile);
	}
}
