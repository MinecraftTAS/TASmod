package de.scribble.lp.tasmod.savestates.chunkloading;

import java.util.List;

import de.scribble.lp.tasmod.duck.ChunkProviderDuck;
import de.scribble.lp.tasmod.mixin.MixinChunkProviderClient;
import de.scribble.lp.tasmod.mixin.MixinChunkProviderServer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ChunkProviderClient;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.WorldServer;
import net.minecraft.world.gen.ChunkProviderServer;
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
	 * Unloads all chunks and reloads the renderer so no chunks will be visible throughout the unloading progress <br>
	 * 
	 * @see MixinChunkProviderClient#unloadAllChunks()
	 * @Side Client
	 */
	@SideOnly(Side.CLIENT)
	public static void unloadAllClientChunks() {
		Minecraft mc = Minecraft.getMinecraft();
		
		ChunkProviderClient chunkProvider=mc.world.getChunkProvider();
		
		((ChunkProviderDuck)chunkProvider).unloadAllChunks();
		Minecraft.getMinecraft().renderGlobal.loadRenderers();
	}
	/**
	 * Unloads all chunks on the server
	 * TODO Maybe change to the vanilla method
	 * 
	 * @see MixinChunkProviderServer#unloadAllChunks()
	 * @Side Server
	 */
	public static void unloadAllServerChunks() {
		
		WorldServer[] worlds=FMLCommonHandler.instance().getMinecraftServerInstance().worlds;
		for (WorldServer world:worlds) {
			ChunkProviderServer chunkProvider=world.getChunkProvider();
			
			((ChunkProviderDuck)chunkProvider).unloadAllChunks();
		}
	}
	/**
	 * The player chunk map keeps track of which chunks need to be sent to the client. <br>
	 * Removing the player stops the server from sending chunks to the client.
	 * 
	 * @Side Server
	 * @see #addPlayersToChunkMap()
	 */
	public static void disconnectPlayersFromChunkMap() {
		MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
		List<EntityPlayerMP> players=server.getPlayerList().getPlayers();
		WorldServer[] worlds=server.worlds;
		for (WorldServer world:worlds) {
			for (EntityPlayerMP player : players) {
				world.getPlayerChunkMap().removePlayer(player);
			}
		}
	}
	/**
	 * The player chunk map keeps track of which chunks need to be sent to the client. <br>
	 * This adds the player to the chunk map so the server knows it can send the information to the client
	 * 
	 * @Side Server
	 * @see #disconnectPlayersFromChunkMap()
	 */
	public static void addPlayersToChunkMap() {
		MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
		List<EntityPlayerMP> players=server.getPlayerList().getPlayers();
		WorldServer[] worlds=server.worlds;
		for (EntityPlayerMP player : players) {
			switch (player.dimension) {
			case -1:
				worlds[1].getPlayerChunkMap().addPlayer(player);
				break;
			case 0:
				worlds[0].getPlayerChunkMap().addPlayer(player);
				break;
			case 1:
				worlds[2].getPlayerChunkMap().addPlayer(player);
				break;
			}
		}
	}
	/**
	 * Tells the save handler to save all changes to disk and remove all references to the region files, making them editable on disc
	 * 
	 * @Side Server
	 */
	public static void flushSaveHandler() {
		MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
		WorldServer[] worlds=server.worlds;
		for(WorldServer world :worlds) {
			world.getSaveHandler().flush();
		}
	}
}
