package de.scribble.lp.tasmod.mixin;

import java.util.Queue;
import java.util.concurrent.FutureTask;

import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import de.scribble.lp.tasmod.CommonProxy;
import de.scribble.lp.tasmod.ModLoader;
import de.scribble.lp.tasmod.savestates.server.SavestateEvents;
import de.scribble.lp.tasmod.savestates.server.SavestateHandler;
import de.scribble.lp.tasmod.tickratechanger.TickrateChangerServer;
import de.scribble.lp.tasmod.ticksync.TickSyncPackage;
import de.scribble.lp.tasmod.ticksync.TickSyncServer;
import net.minecraft.server.MinecraftServer;

@Mixin(MinecraftServer.class)
public abstract class MixinMinecraftServer2 {

	// =====================================================================================================================================

	@ModifyConstant(method = "run", constant = @Constant(longValue = 50L))
	public long modifyMSPT(long fiftyLong) {
		return TickrateChangerServer.MILISECONDS_PER_TICK;
	}

	// =====================================================================================================================================

	@Redirect(method = "run", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;tick()V", ordinal = 1))
	public void redirectTick(MinecraftServer server) {
		this.tick();

		if (SavestateHandler.wasLoading) {
			SavestateHandler.wasLoading = false;
			SavestateHandler.playerLoadSavestateEventServer();
		}

		if (TickSyncServer.isEnabled()) {
			if (TickSyncServer.getServertickcounter() == Integer.MAX_VALUE - 1) {
				TickSyncServer.resetTickCounter();
				CommonProxy.NETWORK.sendToAll(new TickSyncPackage(TickSyncServer.getServertickcounter(), true, true));
			} else {
				TickSyncServer.incrementServerTickCounter();
				CommonProxy.NETWORK.sendToAll(new TickSyncPackage(TickSyncServer.getServertickcounter(), false, true));
			}
		} else {
			TickSyncServer.incrementServerTickCounter();
			CommonProxy.NETWORK.sendToAll(new TickSyncPackage(TickSyncServer.getServertickcounter(), false, false));
		}
		if (TickrateChangerServer.ADVANCE_TICK) {
			TickrateChangerServer.changeServerTickrate(0F);
			TickrateChangerServer.ADVANCE_TICK = false;
		}
	}

	@Shadow
	public abstract void tick();

	// =====================================================================================================================================

	@Redirect(method = "run", at = @At(value = "INVOKE", target = "Ljava/lang/Math;max(JJ)J"))
	public long redirectMathMax(long oneLong, long i) {
		return i; // Getting the original value of i
	}

	// =====================================================================================================================================

	@Shadow
	private long currentTime;

	@Shadow
	private Queue<FutureTask<?>> futureTaskQueue;

	@Redirect(method = "run", at = @At(value = "INVOKE", target = "Ljava/lang/Thread;sleep(J)V"))
	public void redirectThreadSleep(long msToTick) {

		if (msToTick <= 0L) {
			if (TickrateChangerServer.TICKS_PER_SECOND > 20.0)
				msToTick = 0L;
			else
				msToTick = 1L;
		}
		for (long o = 0; o < msToTick; o++) {
			if (TickrateChangerServer.INTERRUPT) {
				msToTick = 1L;
				currentTime = System.currentTimeMillis();
				TickrateChangerServer.INTERRUPT = false;
			}
			synchronized (this.futureTaskQueue) {
				while (!this.futureTaskQueue.isEmpty()) {
					try {
						((FutureTask) this.futureTaskQueue.poll()).run();
					} catch (Throwable var9) {
						var9.printStackTrace();
					}
				}
			}
			try {
				Thread.sleep(1L);
			} catch (InterruptedException e) {
				ModLoader.logger.error("Thread Sleep Interrupted!");
				e.printStackTrace();
			}
		}
	}

	// =====================================================================================================================================
	
	@Inject(method = "tick", at = @At("HEAD"))
	public void lagServer(CallbackInfo ci) {
		if(SavestateEvents.lagServer) {
			SavestateEvents.lagServer=false;
			try {
				Thread.sleep(5000L);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
//	@ModifyVariable(method = "run", at = @At(value = "STORE"), index = 5, ordinal = 2)
//	public long limitLag(long j) {
//		if(j>(500L*(20/TickrateChangerServer.TICKS_PER_SECOND))){
//			return 50L;
//		}
//		return j;
//	}
	
}
