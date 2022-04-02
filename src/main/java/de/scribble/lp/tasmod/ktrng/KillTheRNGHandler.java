package de.scribble.lp.tasmod.ktrng;

import de.scribble.lp.killtherng.KillTheRNG;
import de.scribble.lp.killtherng.URToolsClient;
import de.scribble.lp.killtherng.URToolsServer;
import de.scribble.lp.killtherng.networking.ChangeSeedPacket;
import de.scribble.lp.tasmod.TASmod;

public class KillTheRNGHandler {
	
	private boolean isLoaded;

	public KillTheRNGHandler(boolean isLoaded) {
		this.isLoaded=isLoaded;
		if (isLoaded) {
			KillTheRNG.LOGGER.info("Connection established with TASmod");
		}else {
			TASmod.logger.info("KillTheRNG doesn't appear to be loaded");
		}
	}
	
	public long getGlobalSeedClient() {
		if(isLoaded) 
			return URToolsClient.getRandomFromString("Global").getSeed();
		else
			return 0;
	}
	
	public void setGlobalSeedClient(long seedIn) {
		if (isLoaded) {
			URToolsClient.setSeedAll(seedIn);
		}
	}
	
	public void setGlobalSeedServer(long seedIn) {
		if(isLoaded) {
			KillTheRNG.NETWORK.sendToServer(new ChangeSeedPacket(seedIn));
		}
	}

	public boolean isLoaded() {
		return isLoaded;
	}
}
