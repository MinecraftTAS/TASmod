package de.scribble.lp.tasmod.mixin;


import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import de.scribble.lp.tasmod.ticksync.TickSyncClient;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Timer;

@Mixin(Timer.class)
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
	private long lastTickDuration;
	@Unique
	private long millisSinceTick;
	@Unique
	private long lastGameLoop;
	
	@Inject(method = "updateTimer", at = @At("HEAD"), cancellable = true)
	public void inject_tick(CallbackInfo ci) {
		if (Minecraft.getMinecraft().getConnection() != null) {
			lastSyncSysClock = Minecraft.getSystemTime(); // update the tick tracker so that after returning to scheduling the client won't catch up all ticks (max 10)
			this.elapsedTicks = 0; // do not do any ticks
			long newGameLoop = Minecraft.getSystemTime();
			if (TickSyncClient.shouldTick) {
				TickSyncClient.shouldTick = false;
				this.elapsedTicks++;
				this.lastTickDuration = newGameLoop - this.millisSinceTick;
				this.millisSinceTick = newGameLoop;
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
			this.millisSinceTick = Minecraft.getSystemTime();
			this.lastGameLoop = Minecraft.getSystemTime();
			TickSyncClient.shouldTick = true; // The client should always tick if it once thrown out of the vanilla scheduling part, to make the server tick, etc.
		}
	}
}
