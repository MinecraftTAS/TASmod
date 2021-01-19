package de.scribble.lp.tasmod.savestates.playerloading;

import net.minecraft.client.Minecraft;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.WorldInfo;

/**
 * Loads the world info from the current world folder
 * @author ScribbleLP
 *
 */
public class SavestateWorldLoading {
	public static void loadWorldInfoFromFile() {
		WorldServer[] worlds=Minecraft.getMinecraft().getIntegratedServer().getServer().worlds;
		for (WorldServer world : worlds) {
			WorldInfo info=world.getSaveHandler().loadWorldInfo();
			world.worldInfo=info;
		}
	}
}
