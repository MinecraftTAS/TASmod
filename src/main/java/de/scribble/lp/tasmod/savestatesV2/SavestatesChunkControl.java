package de.scribble.lp.tasmod.savestatesV2;

import java.util.Iterator;
import java.util.List;

import de.scribble.lp.tasmod.duck.ChunkProviderDuck;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ChunkProviderClient;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.WorldServer;
import net.minecraft.world.gen.ChunkProviderServer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class SavestatesChunkControl {
	
	@SideOnly(Side.CLIENT)
	public static void unloadAllClientChunks() {
		Minecraft mc = Minecraft.getMinecraft();
		
		ChunkProviderClient chunkProvider=mc.world.getChunkProvider();
		
		((ChunkProviderDuck)chunkProvider).unloadAllChunks();
		Minecraft.getMinecraft().renderGlobal.loadRenderers();
	}
	
	public static void unloadAllServerChunks() {
		
		WorldServer[] worlds=FMLCommonHandler.instance().getMinecraftServerInstance().worlds;
		for (WorldServer world:worlds) {
			ChunkProviderServer chunkProvider=world.getChunkProvider();
			
			((ChunkProviderDuck)chunkProvider).unloadAllChunks();
		}
	}
	
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
	
	public static void flushSaveHandler() {
		MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
		WorldServer[] worlds=server.worlds;
		for(WorldServer world :worlds) {
			world.getSaveHandler().flush();
		}
	}
}
