package de.scribble.lp.tasmod.monitoring;

import de.scribble.lp.killtherng.custom.EventAnnotations.CaptureRandomness;

public class KTRNGMonitor {
	
	@CaptureRandomness(name = "jukeboxRecordDropPosition")
	public static void monitorJukebox(long seed, String val) {
		System.out.println("Seed: "+seed+", Val: "+val);
	}
}
