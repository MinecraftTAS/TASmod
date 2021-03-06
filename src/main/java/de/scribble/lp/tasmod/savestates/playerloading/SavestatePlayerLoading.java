package de.scribble.lp.tasmod.savestates.playerloading;

import java.util.List;

import de.scribble.lp.tasmod.CommonProxy;
import de.scribble.lp.tasmod.ModLoader;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerList;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.common.DimensionManager;

public class SavestatePlayerLoading {
	
	/**
	 * Loads all worlds and players from the disk. Also sends the playerdata to the client in {@linkplain SavestatePlayerLoadingPacketHandler}
	 * 
	 * Side: Server
	 */
	public static void loadAndSendMotionToPlayer() {
		
		MinecraftServer server=ModLoader.getServerInstance();
		List<EntityPlayerMP> players=server.getPlayerList().getPlayers();
		PlayerList list=server.getPlayerList();
		
		WorldServer[] worlds=DimensionManager.getWorlds();
		for (WorldServer world : worlds) {
			WorldInfo info=world.getSaveHandler().loadWorldInfo();
			world.worldInfo=info;
		}
		for(EntityPlayerMP player : players) {
			
			int dimensionPrev=player.dimension;
			
			NBTTagCompound nbttagcompound = server.getPlayerList().getPlayerNBT(player);
			
			int dimensionNow=0;
			if (nbttagcompound.hasKey("Dimension"))
            {
                dimensionNow = nbttagcompound.getInteger("Dimension");
            }
			
			if(dimensionNow!=dimensionPrev) {
				list.transferPlayerToDimension(player, dimensionNow, new NoPortalTeleporter());
			}
			
			player.readFromNBT(nbttagcompound);
			
			CommonProxy.NETWORK.sendTo(new SavestatePlayerLoadingPacket(nbttagcompound), player);
		}
	}
	
}
