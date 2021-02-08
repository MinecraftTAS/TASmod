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

public class SavestatePlayerLoading {
	
	public static void loadAndSendMotionToPlayer() {
		
		MinecraftServer server=ModLoader.getServerInstance();
		List<EntityPlayerMP> players=server.getPlayerList().getPlayers();
		PlayerList list=server.getPlayerList();
		
		WorldServer[] worlds=server.worlds;
		for (WorldServer world : worlds) {
			WorldInfo info=world.getSaveHandler().loadWorldInfo();
			world.worldInfo=info;
		}
		for(EntityPlayerMP player : players) {
			int dimensionPrev=player.dimension;
			WorldServer worldserver=player.getServerWorld();
			NBTTagCompound nbttagcompound = server.getPlayerList().readPlayerDataFromFile(player);
			int dimensionNow=player.dimension;
			WorldServer worldserver1=player.getServerWorld();
			if(dimensionNow!=dimensionPrev) {
				list.transferPlayerToDimension(player, dimensionNow, new NoPortalTeleporter());
			}
			CommonProxy.NETWORK.sendTo(new SavestatePlayerLoadingPacket(nbttagcompound), player);
		}
	}
	
}
