package de.scribble.lp.tasmod.savestates.server.chunkloading;

import java.util.List;

import de.scribble.lp.tasmod.duck.ChunkProviderDuck;
import de.scribble.lp.tasmod.mixin.accessors.AccessorSaveHandler;
import de.scribble.lp.tasmod.mixin.accessors.AccessorWorld;
import de.scribble.lp.tasmod.mixin.savestates.MixinChunkProviderClient;
import de.scribble.lp.tasmod.mixin.savestates.MixinChunkProviderServer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ChunkProviderClient;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.ChunkProviderServer;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
/**
 * Various methods to unload/reload chunks and make loadless savestates possible
 * @author ScribbleLP
 *
 */
public class SavestatesChunkControl {
	/**
	 * Unloads all chunks and reloads the renderer so no chunks will be visible throughout the unloading progress<br>
	 * <br>
	 * Side: Client
	 * @see MixinChunkProviderClient#unloadAllChunks()
	 */
	@SideOnly(Side.CLIENT)
	public static void unloadAllClientChunks() {
		Minecraft mc = Minecraft.getMinecraft();
		
		ChunkProviderClient chunkProvider=mc.world.getChunkProvider();
		
		((ChunkProviderDuck)chunkProvider).unloadAllChunks();
		Minecraft.getMinecraft().renderGlobal.loadRenderers();
	}
	/**
	 * Unloads all chunks on the server<br>
	 * <br>
	 * Side: Server
	 * @see MixinChunkProviderServer#unloadAllChunks()
	 */
	public static void unloadAllServerChunks() {
		//Forge
		WorldServer[] worlds=DimensionManager.getWorlds();
		//Vanilla
		//WorldServer[] worlds=FMLCommonHandler.instance().getMinecraftServerInstance().worlds;
		
		for (WorldServer world:worlds) {
			ChunkProviderServer chunkProvider=world.getChunkProvider();
			
			((ChunkProviderDuck)chunkProvider).unloadAllChunks();
		}
		
	}
	/**
	 * The player chunk map keeps track of which chunks need to be sent to the client. <br>
	 * Removing the player stops the server from sending chunks to the client.<br>
	 * <br>
	 * Side: Server
	 * @see #addPlayersToChunkMap()
	 */
	public static void disconnectPlayersFromChunkMap() {
		MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
		List<EntityPlayerMP> players=server.getPlayerList().getPlayers();
		//Forge
		WorldServer[] worlds=DimensionManager.getWorlds();
		//Vanilla
		//WorldServer[] worlds=server.worlds;
		for (WorldServer world:worlds) {
			for (EntityPlayerMP player : players) {
				world.getPlayerChunkMap().removePlayer(player);
			}
		}
	}
	/**
	 * The player chunk map keeps track of which chunks need to be sent to the client. <br>
	 * This adds the player to the chunk map so the server knows it can send the information to the client<br>
	 * <br>
	 * Side: Server
	 * @see #disconnectPlayersFromChunkMap()
	 */
	public static void addPlayersToChunkMap() {
		MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
		List<EntityPlayerMP> players=server.getPlayerList().getPlayers();
		//Vanilla
		//WorldServer[] worlds=server.worlds;
//		for (EntityPlayerMP player : players) {
//			switch (player.dimension) {
//			case -1:
//				worlds[1].getPlayerChunkMap().addPlayer(player);
//				worlds[1].getChunkProvider().provideChunk((int)player.posX >> 4, (int)player.posZ >> 4);
//				break;
//			case 0:
//				worlds[0].getPlayerChunkMap().addPlayer(player);
//				worlds[0].getChunkProvider().provideChunk((int)player.posX >> 4, (int)player.posZ >> 4);
//				break;
//			case 1:
//				worlds[2].getPlayerChunkMap().addPlayer(player);
//				worlds[2].getChunkProvider().provideChunk((int)player.posX >> 4, (int)player.posZ >> 4);
//				break;
//			}
//		}
		//Forge
		for (EntityPlayerMP player : players) {
			WorldServer world=DimensionManager.getWorld(player.dimension);
			world.getPlayerChunkMap().addPlayer(player);
			world.getChunkProvider().provideChunk((int)player.posX >> 4, (int)player.posZ >> 4);
		}
	}
	/**
	 * Tells the save handler to save all changes to disk and remove all references to the region files, making them editable on disc<br>
	 * <br>
	 * Side: Server
	 */
	public static void flushSaveHandler() {
		//Forge
		WorldServer[] worlds=DimensionManager.getWorlds();
		//Vanilla
		//MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
		//WorldServer[] worlds=server.worlds;
		for(WorldServer world : worlds) {
			world.getSaveHandler().flush();
		}
	}
	/**
	 * The session lock is minecrafts failsafe system when it comes to saving. It prevents writing to the world folder from 2 different locations <br>
	 * <br>
	 * That works by storing system time to a session.lock file, when the server started. The integrated server also saves the time when it started in a variable. <br>
	 * <br>
	 * Those two times are then compared every time minecraft tries to save and fails if the times are different.<br>
	 * <br>
	 * Since we never close the integrated server, but copy an "old" session.lock file with the savestate, the session.lock will always mismatch.<br>
	 * Thus we need to update the session lock once the loadstating is completed<br>
	 * <br>
	 * TLDR:<br>
	 * Updates the session lock to allow for vanilla saving again<br>
	 * <br>
	 * Side: Server
	 */
	public static void updateSessionLock() {
		//Forge
		WorldServer[] worlds=DimensionManager.getWorlds();
		//Vanilla
		//MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
		//WorldServer[] worlds=server.worlds;
		for(WorldServer world : worlds) {
			((AccessorSaveHandler) world.getSaveHandler()).invokeSetSessionLock();
		}
	}
	/**
	 * A bug occurs when unloading the client world. The client world has a "unloadedEntityList" which, as the name implies, stores all unloaded entities <br>
	 * <br>
	 * Strange things happen, when the client player is unloaded, which is what happens when we use {@linkplain #unloadAllClientChunks()}.<br>
	 * <br>
	 * This method ensures that the player is loaded by removing the player from the unloadedEntityList. <br>
	 * <br>
	 * TLDR:<br>
	 * Makes sure that the player is not removed from the loaded entity list<br>
	 * <br>
	 * Side: Client
	 */
	@SideOnly(Side.CLIENT)
	public static void keepPlayerInLoadedEntityList(net.minecraft.entity.player.EntityPlayer player) {
		((AccessorWorld) Minecraft.getMinecraft().world).unloadedEntityList().remove(player);
	}
	
	/**
	 * Similar to {@linkplain #keepPlayerInLoadedEntityList(EntityPlayer)}, the chunks themselves have a list with loaded entities <br>
	 * <br>
	 * Even after adding the player to the world, the chunks may not load the player correctly. <br>
	 * <br>
	 * Without this, no model is shown in third person and the player is able to place blocks inside of him.<br>
	 * This state is fixed, once the player moves into a different chunk, since the new chunk adds the player to it's list. <br>
	 * <br>
	 * 
	 * TLDR:<br>
	 * Adds the player to the chunk so he can't place any blocks inside himself <br>
	 * <br>
	 * Side: Client
	 */
	@SideOnly(Side.CLIENT)
	public static void addPlayerToChunk(net.minecraft.entity.player.EntityPlayer player) {
		int i = MathHelper.floor(player.posX / 16.0D);
        int j = MathHelper.floor(player.posZ / 16.0D);
        Chunk chunk=Minecraft.getMinecraft().world.getChunkFromChunkCoords(i, j);
        for (int k = 0; k < chunk.getEntityLists().length; k++) {
        	if(chunk.getEntityLists()[k].contains(player)) {
        		return;
        	}
		}
        chunk.addEntity(player);
	}
	
}
