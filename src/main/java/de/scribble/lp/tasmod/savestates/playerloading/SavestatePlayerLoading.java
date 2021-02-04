package de.scribble.lp.tasmod.savestates.playerloading;

import java.util.List;

import de.scribble.lp.tasmod.CommonProxy;
import de.scribble.lp.tasmod.ModLoader;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.SPacketHeldItemChange;
import net.minecraft.network.play.server.SPacketPlayerAbilities;
import net.minecraft.network.play.server.SPacketServerDifficulty;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.WorldInfo;

public class SavestatePlayerLoading {
	
	public static void loadAndSendMotionToPlayer() {
		
		MinecraftServer server=ModLoader.getServerInstance();
		List<EntityPlayerMP> players=server.getPlayerList().getPlayers();
		
		WorldServer[] worlds=server.worlds;
		for (WorldServer world : worlds) {
			WorldInfo info=world.getSaveHandler().loadWorldInfo();
			world.worldInfo=info;
		}
		for(EntityPlayerMP player : players) {
			player.inventory.clear();
			NBTTagCompound nbttagcompound = server.getPlayerList().readPlayerDataFromFile(player);
			player.setWorld(server.getWorld(player.dimension));
			World playerWorld = server.getWorld(player.dimension);
	        if (playerWorld == null)
	        {
	            player.dimension = 0;
	            playerWorld = server.getWorld(0);
	            BlockPos spawnPoint = playerWorld.provider.getRandomizedSpawnPoint();
	            player.setPosition(spawnPoint.getX(), spawnPoint.getY(), spawnPoint.getZ());
	        }
	        player.setWorld(playerWorld);
	        player.interactionManager.setWorld((WorldServer)player.world);
	        
	        player.sendAllContents(player.inventoryContainer, NonNullList.create());
			CommonProxy.NETWORK.sendTo(new SavestatePlayerLoadingPacket(nbttagcompound), player);
		}
	}
}
