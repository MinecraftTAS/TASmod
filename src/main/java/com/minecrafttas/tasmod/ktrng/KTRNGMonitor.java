package com.minecrafttas.tasmod.ktrng;

import de.scribble.lp.killtherng.custom.EventAnnotations.CaptureRandomness;

public class KTRNGMonitor {
	
	@CaptureRandomness(name = "jukeboxRecordDropPosition")
	public static void monitor(long seed, String value) {
//		System.out.println(String.format("Seed: %s, Value: %s", seed, value));
	}
}
