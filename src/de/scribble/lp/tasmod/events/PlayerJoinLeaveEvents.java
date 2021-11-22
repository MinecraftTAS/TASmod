package de.scribble.lp.tasmod.events;


import com.mojang.authlib.GameProfile;

import de.scribble.lp.tasmod.ClientProxy;
import de.scribble.lp.tasmod.TASmod;
import de.scribble.lp.tasmod.tickratechanger.TickrateChangerServer;
import de.scribble.lp.tasmod.ticksync.TickSyncServer;
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
		TickSyncServer.joinServer(player);
		TASmod.containerStateServer.joinServer(player);
		TickrateChangerServer.joinServer(player);
	}
	
	/**
	 * Executes when a player left the server on the server side
	 * 
	 * @param player The player that left the server
	 */
	public static void firePlayerLeaveServerSide(EntityPlayerMP player) {
//		TASmod.logger.info("Firing logout events for {} on the SERVER", player.getName());
	}

	/**
	 * Executes when the player joins the server on the client side
	 * @param player The singleplayer player
	 */
	@SideOnly(Side.CLIENT)
	public static void firePlayerJoinedClientSide(net.minecraft.client.entity.EntityPlayerSP player) {
		TASmod.logger.info("Firing login events for {} on the CLIENT", player.getName());
		ClientProxy.virtual.unpressEverything();
		ClientProxy.shieldDownloader.onPlayerJoin(player.getGameProfile());
	}
	
	/**
	 * Executes when the player leaves the server on the client side
	 * @param player
	 */
	@SideOnly(Side.CLIENT)
	public static void firePlayerLeaveClientSide(net.minecraft.client.entity.EntityPlayerSP player) {
		TASmod.logger.info("Firing logout events for {} on the CLIENT", player.getName());
		ClientProxy.virtual.unpressEverything();
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
			LoadWorldEvent.doneLoadingIngame();
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
