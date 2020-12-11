package de.scribble.lp.tasmod.ticksync;

public class TickSyncCamera {
	private static int cameratickcounter;
	
	public static void incrementCameratickcounter() {
		cameratickcounter++;
	}
	public static void resetCameraTickcounter() {
		cameratickcounter=0;
	}
	public static int getCameratickcounter() {
		return cameratickcounter;
	}
}
