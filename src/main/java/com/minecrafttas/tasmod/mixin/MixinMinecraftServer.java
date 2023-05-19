package com.minecrafttas.tasmod.mixin;

import java.util.Queue;
import java.util.concurrent.FutureTask;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.minecrafttas.tasmod.CommonProxy;
import com.minecrafttas.tasmod.TASmod;
import com.minecrafttas.tasmod.savestates.server.SavestateHandler;
import com.minecrafttas.tasmod.savestates.server.SavestateState;
import com.minecrafttas.tasmod.tickratechanger.TickrateChangerServer;
import com.minecrafttas.tasmod.ticksync.TickSyncServer;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.NetworkSystem;
import net.minecraft.server.MinecraftServer;

@Mixin(MinecraftServer.class)
public abstract class MixinMinecraftServer {

	// =====================================================================================================================================

	@ModifyConstant(method = "run", constant = @Constant(longValue = 50L))
	public long modifyMSPT(long fiftyLong) {
		return TickrateChangerServer.millisecondsPerTick;
	}

	// =====================================================================================================================================

	@Redirect(method = "run", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;tick()V"))
	public void redirectTick(MinecraftServer server) {
		
	}

	@Shadow
	public abstract void tick();

	// =====================================================================================================================================

//	@Redirect(method = "run", at = @At(value = "INVOKE", target = "Ljava/lang/Math;max(JJ)J"))
	public long redirectMathMax(long oneLong, long i) {
		return i; // Getting the original value of i
	}

	// =====================================================================================================================================

	@Shadow
	private long currentTime;

	@Shadow
	private Queue<FutureTask<?>> futureTaskQueue;

	@Shadow
	private NetworkSystem networkSystem;

	private int faketick = 0;

	@Shadow
	private boolean serverIsRunning;
	
	@Redirect(method = "run", at = @At(value = "INVOKE", target = "Ljava/lang/Thread;sleep(J)V"))
	public void redirectThreadSleep(long msToTick) {
		
		/*	The server should tick if:
		 *	(shouldTick in ticksync is true OR there are no players connected to the custom server) AND the tickrate is not zero. That or advance tick is true*/
		if( (TickSyncServer.shouldTick() && TickrateChangerServer.ticksPerSecond != 0) || TickrateChangerServer.advanceTick) {
			long timeBeforeTick = System.currentTimeMillis();
			
			if (TASmod.savestateHandler.state == SavestateState.WASLOADING) {
				TASmod.savestateHandler.state = SavestateState.NONE;
				SavestateHandler.playerLoadSavestateEventServer();
			}

			TASmod.ktrngHandler.updateServer();
			this.tick();
			CommonProxy.tickSchedulerServer.runAllTasks();
			
			if (TickrateChangerServer.advanceTick) {
				TickrateChangerServer.changeServerTickrate(0F);
				TickrateChangerServer.advanceTick = false;
			}
			TickSyncServer.serverPostTick();
			
			long tickDuration = System.currentTimeMillis() - timeBeforeTick;
			
			// ==================================================
			
			try {
				Thread.sleep(Math.max(1L, TickrateChangerServer.millisecondsPerTick - tickDuration));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		} else { // This is when the server should not tick... This is to ensure network tick stuff is working
			if(TickrateChangerServer.ticksPerSecond == 0) {
				faketick++;
				if (faketick >= 50) {
					faketick = 0;
					networkSystem.networkTick();
					if (((MinecraftServer) (Object) this).isDedicatedServer()) {
						runPendingCommands();
					}
				}
			}
			
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		synchronized (this.futureTaskQueue) {
			while (!this.futureTaskQueue.isEmpty()) {
				try {
					((FutureTask<?>) this.futureTaskQueue.poll()).run();
				} catch (Throwable var9) {
					var9.printStackTrace();
				}
			}
		}
	}

	@Environment(EnvType.CLIENT)
	private void runPendingCommands() {
		if ((MinecraftServer) (Object) this instanceof net.minecraft.server.dedicated.DedicatedServer) {
			net.minecraft.server.dedicated.DedicatedServer server = (net.minecraft.server.dedicated.DedicatedServer) (MinecraftServer) (Object) this;
			server.executePendingCommands();
		}
	}

	// =====================================================================================================================================


//	@ModifyVariable(method = "run", at = @At(value = "STORE"), index = 5, ordinal = 2)
	public long limitLag(long j) {
		if(j>=TickrateChangerServer.millisecondsPerTick*5){
			return TickrateChangerServer.millisecondsPerTick;
		}
		return j;
	}
	
}
