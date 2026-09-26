package com.minecrafttas.tasmod.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.minecrafttas.tasmod.TASmodClient;
import com.minecrafttas.tasmod.ticksync.TickSyncClient;

import net.minecraft.client.Minecraft;
import net.minecraft.client.DeltaTracker;
import net.minecraft.util.Util;

@Mixin(DeltaTracker.class)
/**
 * <p>Rewrites updateTimer, to add a tickratechanger and apply ticksync.
 * <p>Dynamically speeds up or slows down the tickrate depending on the time between {@link TickSyncClient TickSync} packages.
 * @author Pancake
 *
 */
public class MixinTimer {

	/**
	 * <p>How many ticks elapsed since the {@link DeltaTracker#updateTimer()} method was called.
	 * <p>Used in Minecraft#runGameLoop to call {@link Minecraft#runTick()} in the for loop
	 */
	@Shadow
	private int elapsedTicks;
	/**
	 * How many "frames" elapsed since the {@link DeltaTracker#updateTimer()} method was called
	 */
	@Shadow
	private float elapsedPartialTicks;
	/**
	 * Value between 0-1 of how many frames have elapsed inbetween ticks
	 */
	@Shadow
	private float renderPartialTicks;
	/**
	 * The last time the {@link DeltaTracker#updateTimer()} method was called
	 */
	@Shadow
	private long lastSyncSysClock;

	// ==========================================================

	/**
	 * System time the last time a tick has run and ticksync has triggered
	 */
	@Unique
	private long timeSinceLastTick;
	/**
	 * The tick length in the last tick
	 */
	@Unique
	private float lastTickLength;

	/**
	 * <p>Overwrites {@link DeltaTracker#updateTimer()} in a way,<br>
	 * so that the tickrate matches the tickrate of the server.
	 * 
	 * <p>It does this by removing {@link DeltaTracker#tickLength} from the equasion.<br>
	 * Takes the time between incoming packets from {@link TickSyncClient#onClientPacket(com.minecrafttas.mctcommon.networking.interfaces.PacketID, java.nio.ByteBuffer, String) TickSyncClient.onClientPacket()}
	 * and calculates the tickrate dynamically.
	 * 
	 * <p>If no packet is present, it stops the client.<br>
	 * This can happen when:
	 * <ol>
	 * <li>The packet did not reach it's destination</li>
	 * <li>The tickrate on the server is 0</li>
	 * <li>The server is lagging and unable to send packets</li>
	 * <li>Another player is taking too long to send a packet</li>
	 * </ol>
	 * 
	 * @param ci
	 */
	@Inject(method = "updateTimer", at = @At("HEAD"), cancellable = true)
	public void inject_tick(CallbackInfo ci) {
		/* 
		 * Run overriden updateTimer method
		 * 
		 * Only runs when there is a connection to the custom networking server and the player is in a world
		 */
		if (TASmodClient.client != null && !TASmodClient.client.isClosed() && Minecraft.getInstance().level != null) {

			long currentTime = Util.getMillis();	// The current system time as of calling this method
			/*
			 * The length of the tick in milliseconds.
			 * Set to 50 in vanilla, but in this instance,
			 * it is set to the duration of when Ticksync.shouldTick was true
			 */
			float tickLength = lastTickLength;

			this.elapsedTicks = 0; // Prevent the client from ticking

			/*
			 * Ticksync block.
			 * Allows the client to run for 1 tick if TickSyncClient.shouldTick is true
			 */
			if (TickSyncClient.shouldTick.compareAndSet(true, false)) {

				this.elapsedTicks++;	// Allow the client to tick once
				tickLength = currentTime - timeSinceLastTick;	// Check the time between ticks
				if (TASmodClient.tickratechanger.advanceTick) {
					tickLength = TASmodClient.tickratechanger.millisecondsPerTick;	// Keep the lastTick duration steady during tickadvance, since it grows larger the longer you wait in tickrate 0
				}
				timeSinceLastTick = currentTime;
				this.renderPartialTicks = 0;	// Reset render partial ticks after a new tick
			}

			/*
			 * Vanilla calculation from updateTimer,
			 * with added interpolation calculation
			 */
			this.elapsedPartialTicks = (currentTime - this.lastSyncSysClock) / tickLength;	// Use the calculated tickLength instead of the vanill tickLength
			float newRenderPartialTicks = this.renderPartialTicks;
			newRenderPartialTicks += this.elapsedPartialTicks;
			newRenderPartialTicks -= (int) this.renderPartialTicks;

			if (newRenderPartialTicks > this.renderPartialTicks) {	// Fixes stuttering when the renderPartialTicks stay the same during tickrate 0
				this.renderPartialTicks = newRenderPartialTicks;
			}

			this.lastSyncSysClock = currentTime;	// Update vanilla variable
			lastTickLength = tickLength;	// Update last tick length
			ci.cancel();
		}
		// Run vanilla updateTimer
		else {
			this.timeSinceLastTick = Util.getMillis();
			TickSyncClient.shouldTick.set(true);	// Client should always tick, when in the main menu
		}
	}
}

