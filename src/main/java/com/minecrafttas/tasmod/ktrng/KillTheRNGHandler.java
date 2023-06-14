package com.minecrafttas.tasmod.ktrng;

import com.minecrafttas.common.events.EventClient.EventPlayerJoinedClientSide;
import com.minecrafttas.common.events.EventServer.EventServerTick;
import com.minecrafttas.killtherng.KillTheRNG;
import com.minecrafttas.killtherng.SeedingModes;
import com.minecrafttas.tasmod.TASmod;
import com.minecrafttas.tasmod.TASmodClient;
import com.minecrafttas.tasmod.playback.PlaybackController.TASstate;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.server.MinecraftServer;

/**
 * Easy access to the KillTheRNG library without littering the rest of the code
 * 
 * @author Scribble
 *
 */
public class KillTheRNGHandler implements EventServerTick, EventPlayerJoinedClientSide{
	
	private boolean isLoaded;
	
	/**
	 * Instantiates a KillTheRNGHandler instance
	 * @param isLoaded If the KillTheRNG mod is loaded
	 */
	public KillTheRNGHandler(boolean isLoaded) {
		
		this.isLoaded=isLoaded;
		
		if (isLoaded) {
			KillTheRNG.LOGGER.info("Connection established with TASmod");
			KillTheRNG.isLibrary=true;
			KillTheRNG.mode=SeedingModes.TickChange;
			
			KillTheRNG.annotations.register(new KTRNGMonitor());
		}else {
			TASmod.LOGGER.info("KillTheRNG doesn't appear to be loaded");
		}
	}
	
	public long advanceGlobalSeedServer() {
		if(isLoaded()) {
			return KillTheRNG.commonRandom.nextSeed();
		} else {
			return 0;
		}
	}
	
	public long getGlobalSeedServer() {
		if(isLoaded()) {
			return KillTheRNG.commonRandom.GlobalServer.getSeed();
		} else {
			return 0;
		}
	}
	
	public boolean isLoaded() {
		return isLoaded;
	}
	
	
	//=================================================Setting the seed
	/**
	 * @return The global seed of the client
	 */
	@Environment(EnvType.CLIENT)
	public long getGlobalSeedClient() {
		if(isLoaded()) 
			return KillTheRNG.clientRandom.GlobalClient.getSeed();
		else
			return 0;
	}
	
	/**
	 * Set the global seed on the client
	 * @param seedIn The seed on the client
	 */
	@Environment(EnvType.CLIENT)
	public void setGlobalSeedClient(long seedIn) {
		if (isLoaded()) {
			KillTheRNG.clientRandom.setSeedAll(seedIn);
		}
	}
	
	
	public void setGlobalSeedServer(long seedIn) {
		if (isLoaded()) {
			KillTheRNG.commonRandom.setSeedAll(seedIn);
		}
	}
	/**
	 * Sends a packet to the server, setting the global seed
	 * @param seedIn The seed on the server
	 */
	@Environment(EnvType.CLIENT)
	public void sendGlobalSeedToServer(long seedIn) {
		if(isLoaded()) {
			if(TASmodClient.client != null)
				TASmodClient.packetClient.sendToServer(new KTRNGSeedPacket(seedIn));
			else
				setGlobalSeedClient(seedIn);
		}
	}
	//=================================================TASmod integration
	
	/**
	 * Executed every tick on the server
	 */
	@Override
	public void onServerTick(MinecraftServer server) {
		if(isLoaded()) {
			if(TASmod.containerStateServer.getState() != TASstate.PAUSED)
				TASmod.packetServer.sendToAll(new KTRNGSeedPacket(advanceGlobalSeedServer()));
		}
	}
	
	//================================================= Seedsync
	
	public void broadcastStartSeed() {
		if(isLoaded()) {
			long seed = getGlobalSeedServer();
			TASmod.packetServer.sendToAll(new KTRNGStartSeedPacket(seed));
		}
	}
	
	@Environment(EnvType.CLIENT)
	public void setInitialSeed(long initialSeed) {
		if(TASmodClient.client != null) {
			TASmod.LOGGER.info("Sending initial client seed: {}", initialSeed);
			TASmodClient.packetClient.sendToServer(new KTRNGStartSeedPacket(initialSeed));	// TODO Every new player in multiplayer will currently send the initial seed, which is BAD
		} else {
			TASmod.ktrngHandler.setGlobalSeedClient(initialSeed);
		}
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void onPlayerJoinedClientSide(EntityPlayerSP player) {
		setInitialSeed(getGlobalSeedClient());
	}
	
	
}
