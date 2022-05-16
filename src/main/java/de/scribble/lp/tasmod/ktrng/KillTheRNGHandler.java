package de.scribble.lp.tasmod.ktrng;

import de.scribble.lp.killtherng.KillTheRNG;
import de.scribble.lp.killtherng.NextSeedHandler;
import de.scribble.lp.killtherng.URToolsClient;
import de.scribble.lp.killtherng.custom.CustomRandom;
import de.scribble.lp.killtherng.networking.ChangeSeedPacket;
import de.scribble.lp.tasmod.ClientProxy;
import de.scribble.lp.tasmod.TASmod;
import net.minecraft.util.text.TextFormatting;

/**
 * Easy access to the KillTheRNG library without littering the rest of the code
 * 
 * @author ScribbleLP
 *
 */
public class KillTheRNGHandler{
	
	private boolean isLoaded;
	
	private final NextSeedHandler nextSeedHandler;
	
	private final KTRNGMonitor monitor;
	
	private int seedCounter;
	
	
	public KillTheRNGHandler(boolean isLoaded) {
		this.isLoaded=isLoaded;
		if (isLoaded) {
			KillTheRNG.LOGGER.info("Connection established with TASmod");
			KillTheRNG.isLibrary=true;
			nextSeedHandler=new NextSeedHandler();
			monitor=new KTRNGMonitor();
		}else {
			nextSeedHandler=null;
			monitor=null;
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
			seedCounter++;
		}
	}
	
	public void sendAndResetNextPlayerInput() {
		if(isLoaded()) {
			nextSeedHandler.sendAndReset();
			monitor.testRand.advance(seedCounter);
			seedCounter=0;
		}
	}
	
	public void setTestSeed(long seed) {
		if(isLoaded()) {
			clear();
			monitor.testRand.setSeed(seed);
		}
	}
	
	public String getDesyncString() {
		return monitor.monitorString;
	}
	
	public void clear(){
		monitor.monitorString="";
	}

	public boolean isLoaded() {
		return isLoaded;
	}

	private class KTRNGMonitor implements de.scribble.lp.killtherng.custom.KTRNGEventHandler.KTRNGEvent{

		public String monitorString;
		
		public CustomRandom testRand;
		
		public KTRNGMonitor() {
			testRand=new CustomRandom("TestRand");
			KillTheRNG.eventHandler.register(this);
		}
		
		@Override
		public void trigger() {
			updateDesyncString();
		}

		private void updateDesyncString() {
			if(ClientProxy.virtual.getContainer().isPlayingback())
				monitorString=KillTheRNG.randomness.Global.getSeed()==testRand.getSeed() ? "" : TextFormatting.RED+String.format("Expected: %s %s", testRand.getSeed(), testRand.steps(KillTheRNG.randomness.Global));
		}

	}
	
}
