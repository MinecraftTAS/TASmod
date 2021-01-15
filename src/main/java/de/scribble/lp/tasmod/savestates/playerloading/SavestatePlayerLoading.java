package de.scribble.lp.tasmod.savestates.playerloading;

import java.util.List;

import de.scribble.lp.tasmod.CommonProxy;
import de.scribble.lp.tasmod.ModLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.FMLCommonHandler;

public class SavestatePlayerLoading {
	public static void loadPlayersFromFile() {
		
		List<EntityPlayerMP> players=FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayers();
		players.forEach(player->{
			NBTTagCompound nbttagcompound = Minecraft.getMinecraft().getIntegratedServer().getServer().getPlayerList().readPlayerDataFromFile(player);
			CommonProxy.NETWORK.sendTo(new SavestatePlayerLoadingPacket(nbttagcompound), player);
		});
	}
}
