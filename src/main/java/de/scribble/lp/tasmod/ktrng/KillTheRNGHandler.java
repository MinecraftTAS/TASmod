package de.scribble.lp.tasmod.ktrng;

import java.util.ArrayList;
import java.util.List;

import de.scribble.lp.killtherng.KillTheRNG;
import de.scribble.lp.killtherng.NextSeedHandler;
import de.scribble.lp.killtherng.SeedingModes;
import de.scribble.lp.killtherng.URToolsClient;
import de.scribble.lp.killtherng.custom.CustomRandom;
import de.scribble.lp.killtherng.networking.ChangeSeedPacket;
import de.scribble.lp.tasmod.ClientProxy;
import de.scribble.lp.tasmod.TASmod;
import de.scribble.lp.tasmod.virtual.VirtualKeybindings;
import net.minecraft.client.Minecraft;
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
	
	private List<Integer> blockedKeyCodes = new ArrayList<>();
	
	
	public KillTheRNGHandler(boolean isLoaded) {
		this.isLoaded=isLoaded;
		if (isLoaded) {
			KillTheRNG.LOGGER.info("Connection established with TASmod");
			KillTheRNG.isLibrary=true;
			KillTheRNG.mode=SeedingModes.Tick;
			nextSeedHandler=new NextSeedHandler();
			monitor=new KTRNGMonitor();
			
			registerBlockedStuff();
		}else {
			nextSeedHandler=null;
			monitor=null;
			TASmod.logger.info("KillTheRNG doesn't appear to be loaded");
		}
	}
	
	private void registerBlockedStuff() {
		blockedKeyCodes.add(0);
		blockedKeyCodes.add(Minecraft.getMinecraft().gameSettings.keyBindChat.getKeyCode());
		blockedKeyCodes.add(1);
	}
	
	public long getGlobalSeedClient() {
		if(isLoaded()) 
			return URToolsClient.getRandomFromString("Global").getSeed();
		else
			return 0;
	}
	
	public void setGlobalSeed(long seedIn) {
		setGlobalSeedClient(seedIn);
		setGlobalSeedServer(seedIn);
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

	public boolean isKeyCodeBlocked(int keycodeIn) {
		return VirtualKeybindings.isKeyCodeAlwaysBlocked(keycodeIn) || blockedKeyCodes.contains(keycodeIn);
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
