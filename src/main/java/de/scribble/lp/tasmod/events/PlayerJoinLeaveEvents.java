package de.scribble.lp.tasmod.events;


import de.scribble.lp.tasmod.ClientProxy;
import de.scribble.lp.tasmod.TASmod;
import de.scribble.lp.tasmod.tickratechanger.TickrateChangerServer;
import de.scribble.lp.tasmod.ticksync.TickSyncServer;
import net.minecraft.entity.player.EntityPlayerMP;
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
	}
	
	/**
	 * Executes when a player left the server on the server side
	 * 
	 * @param player The player that left the server
	 */
	public static void firePlayerLeaveServerSide(EntityPlayerMP player) {
		TASmod.logger.info("Firing logout events for {} on the SERVER", player.getName());
		TickrateChangerServer.leaveServer(player);
	}

	/**
	 * Executes when the player joins the server on the client side
	 * @param player The singleplayer player
	 */
	@SideOnly(Side.CLIENT)
	public static void firePlayerJoinedClientSide(net.minecraft.client.entity.EntityPlayerSP player) {
		TASmod.logger.info("Firing login events for {} on the CLIENT", player.getName());
		ClientProxy.virtual.unpressEverything();
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
}
