package de.scribble.lp.tasmod.ktrng;

import de.scribble.lp.killtherng.KillTheRNG;
import de.scribble.lp.killtherng.NextSeedHandler;
import de.scribble.lp.killtherng.SeedingModes;
import de.scribble.lp.killtherng.URToolsClient;
import de.scribble.lp.killtherng.networking.ChangeSeedPacket;
import de.scribble.lp.tasmod.TASmod;

/**
 * Easy access to the KillTheRNG library without littering the rest of the code
 * 
 * @author ScribbleLP
 *
 */
public class KillTheRNGHandler {
	
	private boolean isLoaded;
	
	private final NextSeedHandler nextSeedHandler=new NextSeedHandler();

	public KillTheRNGHandler(boolean isLoaded) {
		this.isLoaded=isLoaded;
		if (isLoaded) {
			KillTheRNG.LOGGER.info("Connection established with TASmod");
			KillTheRNG.isLibrary=true;
		}else {
			TASmod.logger.info("KillTheRNG doesn't appear to be loaded");
		}
	}
	
	public long getGlobalSeedClient() {
		if(isLoaded()) 
			return URToolsClient.getRandomFromString("Global").getSeed();
		else
			return 0;
	}
	
	public void setGlobalSeedClient(long seedIn) {
		if (isLoaded()) {
			URToolsClient.setSeedAll(seedIn);
		}
	}
	
	public void setGlobalSeedServer(long seedIn) {
		if(isLoaded()) {
			KillTheRNG.NETWORK.sendToServer(new ChangeSeedPacket(seedIn));
		}
	}
	
	public void nextPlayerInput() {
		if(isLoaded()) {
			nextSeedHandler.increaseNextSeedCounter();
		}
	}
	
	public void sendAndResetNextPlayerInput() {
		if(isLoaded()) {
			nextSeedHandler.sendAndReset();
		}
	}

	public boolean isLoaded() {
		return isLoaded;
	}
}
