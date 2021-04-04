package de.pfannekuchen.killtherng.utils;

import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

import de.pfannekuchen.killtherng.KillTheRng;
import net.minecraftforge.fml.common.FMLCommonHandler;

/**
 * This Random forces a Seed for all 'Random' Operations and logs them into a file.
 * @author Pancake
 */
public final class EntityRandom extends Random {
	
	/**
	 * Seed used for creating new Random instances
	 */
	public static AtomicLong currentSeed = new AtomicLong(0L);

	/**
	 * Set the serialVersionUID to be the same as in {@link Random} so that Deserialization is compatible. 
	 * @author Pancake
	 */
	private static final long serialVersionUID = 3905348978240129619L;
    
    /**
     * Idiot Check, in case my disabling didn't work
     * @author Pancake
     */
    @Override
    protected int next(int bits) {
    	if (KillTheRng.ISDISABLED) {
    		System.err.println("\n\nKillTheRng shouldn't have been enabled!\n\n");
    		FMLCommonHandler.instance().exitJava(-1, true);
    	}
    	return super.next(bits);
    }
    
	// Go into every public method and set the seed before executing the RNG action
	
	@Override
	public boolean nextBoolean() {
		return new Random(currentSeed.get()).nextBoolean();
	}
	
	@Override
	public double nextDouble() {
		return new Random(currentSeed.get()).nextDouble();
	}
	
	@Override
	public int nextInt() {
		return new Random(currentSeed.get()).nextInt();
	}
	
	@Override
	public int nextInt(int bound) {
		return new Random(currentSeed.get()).nextInt(bound);
	}
	
	@Override
	public float nextFloat() {
		return new Random(currentSeed.get()).nextFloat();
	}
	
	@Override
	public void nextBytes(byte[] bytes) {
		new Random(currentSeed.get()).nextBytes(bytes);
	}
	
	@Override
	public double nextGaussian() {
		return new Random(currentSeed.get()).nextGaussian();
	}
	
	@Override
	public long nextLong() {
		return new Random(currentSeed.get()).nextLong();
	}
	
}
