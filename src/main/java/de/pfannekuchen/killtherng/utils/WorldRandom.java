package de.pfannekuchen.killtherng.utils;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

import de.pfannekuchen.killtherng.KillTheRng;
import net.minecraftforge.fml.common.FMLCommonHandler;

/**
 * This Random forces a Seed for all 'Random' Operations and logs them into a file.
 * @author Pancake
 */
public final class WorldRandom extends Random {
	
	/**
	 * Stuff needed to calculate the next seed
	 * @author Pancake
	 */
    private static final long multiplier = 0x5DEECE66DL;
    private static final long addend = 0xBL;
    private static final long mask = (1L << 48) - 1;
	
    public static volatile AtomicBoolean update = new AtomicBoolean(false);
    
	/**
	 * Set the serialVersionUID to be the same as in {@link Random} so that Deserialization is Compatible. 
	 * @author Pancake
	 */
	private static final long serialVersionUID = 3905348978240129619L;

	private static volatile ArrayList<Random> instances = new ArrayList<>();
	
	public WorldRandom(long seed) {
		super(seed);
		instances.add(this);
	}
	
	public WorldRandom() {
		super();
		instances.add(this);
	}
	
    /**
     * Idiot Check, in case my disabling didn't work
     * @author Pancake
     */
    @Override
    protected int next(int bits) {
    	if (update.get()) {
    		update.set(false);
    		updateSeed(((nextLong() * multiplier + addend) & mask)); // Set the seed to the mathematically next seed.
    	}
    	if (KillTheRng.ISDISABLED) {
    		System.err.println("\n\nKillTheRng shouldn't have been enabled!\n\n");
    		FMLCommonHandler.instance().exitJava(-1, true);
    	}
    	return super.next(bits);
    }
	
	/**
	 * Update the Seed for all Random instances, since they can change it themselves
	 * 
	 * @author Pancake
	 * @param newSeed Set the Seed of every World Random Instance
	 */
	public static void updateSeed(long newSeed) {
		instances.forEach((c) -> {
			c.setSeed(newSeed);
		});
	}
	
}
