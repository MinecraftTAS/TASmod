package com.minecrafttas.tasmod.mixin;


import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.minecrafttas.tasmod.tickratechanger.TickrateChangerClient;
import com.minecrafttas.tasmod.ticksync.TickSyncClient;

import net.minecraft.client.Minecraft;
import net.minecraft.util.Timer;

@Mixin(Timer.class)
/**
 * Rewrites updateTimer to make it possible to interpolate ticks.
 * @author Pancake
 *
 */
public class MixinTimer {
	
	@Shadow
    private int elapsedTicks;
	@Shadow
    private float renderPartialTicks;
	@Shadow
    private float elapsedPartialTicks;
	@Shadow
    private long lastSyncSysClock;
	@Shadow
    private float tickLength;
	
	@Unique
	private long millisLastTick;
	@Unique
	private long lastGameLoop;
	@Unique
	private float lastTickDuration;
	
	@Inject(method = "updateTimer", at = @At("HEAD"), cancellable = true)
	public void inject_tick(CallbackInfo ci) {
		if (Minecraft.getMinecraft().getConnection() != null) {
			lastSyncSysClock = Minecraft.getSystemTime(); // update the tick tracker so that after returning to scheduling the client won't catch up all ticks (max 10)
			this.elapsedTicks = 0; // do not do any ticks
			long newGameLoop = Minecraft.getSystemTime();
			if (TickSyncClient.shouldTick.compareAndSet(true, false)) {
				this.elapsedTicks++;
				this.lastTickDuration = newGameLoop - this.millisLastTick;
				if(TickrateChangerClient.advanceTick) {
					lastTickDuration = TickrateChangerClient.millisecondsPerTick;	// Keep the lastTick duration steady during tickadvance, since it grows larger the longer you wait in tickrate 0
				}
				this.millisLastTick = newGameLoop; // Update millisLastTick
				this.renderPartialTicks = 0; // Reset after the tick
			}
			// Interpolating
			this.elapsedPartialTicks = (newGameLoop - this.lastGameLoop) / this.lastTickDuration;
			float newPartialTicks = this.renderPartialTicks;
			newPartialTicks += this.elapsedPartialTicks;
			newPartialTicks -= (int) this.renderPartialTicks;
			if (newPartialTicks > this.renderPartialTicks) {
				this.renderPartialTicks = newPartialTicks;
			}
			this.lastGameLoop = newGameLoop;
			ci.cancel();
		} else {
			this.millisLastTick = Minecraft.getSystemTime();
			this.lastGameLoop = Minecraft.getSystemTime();
			TickSyncClient.shouldTick.set(true); // The client should always tick if it once thrown out of the vanilla scheduling part, to make the server tick, etc.
		}
	}
}
