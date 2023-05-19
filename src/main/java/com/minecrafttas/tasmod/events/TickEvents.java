package com.minecrafttas.tasmod.events;

import com.minecrafttas.tasmod.ClientProxy;
import com.minecrafttas.tasmod.TASmod;
import com.minecrafttas.tasmod.ticksync.TickSyncClient;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

/**
 * Hooks into client on server tick loops
 * @author Scribble
 *
 */
public class TickEvents {
	
	public static void onRender() {
		KeybindingEvents.fireKeybindingsEvent();

		if (ClientProxy.packetClient != null && ClientProxy.packetClient.isClosed()) { // If the server died, but the client has not left the world
			TickSyncClient.shouldTick.set(true);
		}
	}

	@Environment(EnvType.CLIENT)
	public static void onClientTick() {
		TASmod.ktrngHandler.updateClient();
	}

	public static void onServerTick() {
	}
}
