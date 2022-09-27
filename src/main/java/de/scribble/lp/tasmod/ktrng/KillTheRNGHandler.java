package de.scribble.lp.tasmod.ktrng;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import de.scribble.lp.killtherng.KillTheRNG;
import de.scribble.lp.killtherng.SeedingModes;
import de.scribble.lp.killtherng.networking.ChangeSeedPacket;
import de.scribble.lp.tasmod.ClientProxy;
import de.scribble.lp.tasmod.TASmod;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Easy access to the KillTheRNG library without littering the rest of the code
 * 
 * @author Scribble
 *
 */
public class KillTheRNGHandler{
	
	private boolean isLoaded;
	
	private final KTRNGMonitor monitor;
	
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
			monitor=new KTRNGMonitor();
			
		}else {
			monitor=null;
			TASmod.logger.info("KillTheRNG doesn't appear to be loaded");
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
	@SideOnly(Side.CLIENT)
	public long getGlobalSeedClient() {
		if(isLoaded()) 
			return KillTheRNG.clientRandom.GlobalClient.getSeed();
		else
			return 0;
	}
	
	/**
	 * Set the global seed on both client and server
	 * @param seedIn The seed on both client and server
	 */
	@SideOnly(Side.CLIENT)
	public void setGlobalSeed(long seedIn) {
		setGlobalSeedClient(seedIn);
		setGlobalSeedServer(seedIn);
	}
	
	/**
	 * Set the global seed on the client
	 * @param seedIn The seed on the client
	 */
	@SideOnly(Side.CLIENT)
	public void setGlobalSeedClient(long seedIn) {
		if (isLoaded()) {
			KillTheRNG.clientRandom.setSeedAll(seedIn);
		}
	}
	
	/**
	 * Sends a packet to the server, setting the global seed
	 * @param seedIn The seed on the server
	 */
	@SideOnly(Side.CLIENT)
	public void setGlobalSeedServer(long seedIn) {
		if(isLoaded()) {
			KillTheRNG.NETWORK.sendToServer(new ChangeSeedPacket(seedIn));
		}
	}
	//=================================================TASmod integration
	
	/**
	 * Executed every tick.
	 */
	@SideOnly(Side.CLIENT)
	public void updateClient() {
		if(isLoaded()) {
			Long seed = pollSeed();
			if(seed != null) {
				setGlobalSeedClient(seed);
			}
		}
	}
	
	/**
	 * Executed every tick on the server
	 */
	public void updateServer() {
		if(isLoaded()) {
//			KillTheRNG.tickmodeServer.tickCustom();
		}
	}
	
	//=================================================Monitoring
	
	private Queue<Long> seedQueue = new ConcurrentLinkedQueue<>();
	
	public void addToQueue(long seed) {
		seedQueue.add(seed);
	}
	
	public Long pollSeed() {
		return seedQueue.poll();
	}
	
	public void clearQueue() {
		seedQueue.clear();
	}
	
	/**
	 * @return Monitor information displayed in InfoGui
	 */
	public String getDesyncString() {
		return monitor.monitorString;
	}
	
	/**
	 * Clears the monitor string
	 */
	public void clear(){
		monitor.monitorString="";
	}

	private class KTRNGMonitor{

		public String monitorString;
		
		public KTRNGMonitor() {
		}
		
		private void updateDesyncString() {
			if(ClientProxy.virtual.getContainer().isPlayingback()) {
//				monitorString=KillTheRNG.randomness.Global.getSeed()==testRand.getSeed() ? "" : TextFormatting.RED+String.format("Expected: %s %s", testRand.getSeed(), testRand.steps(KillTheRNG.randomness.Global));
			}
		}
	}
	
}
