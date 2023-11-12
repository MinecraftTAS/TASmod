package com.minecrafttas.tasmod.mixin;

import java.util.Queue;
import java.util.concurrent.FutureTask;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.minecrafttas.tasmod.TASmod;
import com.minecrafttas.tasmod.events.EventServer.EventServerTickPost;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.NetworkSystem;
import net.minecraft.server.MinecraftServer;

@Mixin(MinecraftServer.class)
public abstract class MixinMinecraftServer {

	// =====================================================================================================================================

	@ModifyConstant(method = "run", constant = @Constant(longValue = 50L))
	public long modifyMSPT(long fiftyLong) {
		return TASmod.tickratechanger.millisecondsPerTick;
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
		if( (TASmod.ticksyncServer.shouldTick() && TASmod.tickratechanger.ticksPerSecond != 0) || TASmod.tickratechanger.advanceTick) {
			long timeBeforeTick = System.currentTimeMillis();
			
			this.tick();
			TASmod.tickSchedulerServer.runAllTasks();
			
			if (TASmod.tickratechanger.advanceTick) {
				TASmod.tickratechanger.changeServerTickrate(0F);
				TASmod.tickratechanger.advanceTick = false;
			}
			EventServerTickPost.fireServerTickPost((MinecraftServer)(Object)this);
			
			long tickDuration = System.currentTimeMillis() - timeBeforeTick;
			
			// ==================================================
			
			try {
				Thread.sleep(Math.max(1L, TASmod.tickratechanger.millisecondsPerTick - tickDuration));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		} else { // This is when the server should not tick... This is to ensure network tick stuff is working
			if(TASmod.tickratechanger.ticksPerSecond == 0) {
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

	@Environment(EnvType.SERVER)
	private void runPendingCommands() {
		if ((MinecraftServer) (Object) this instanceof net.minecraft.server.dedicated.DedicatedServer) {
			net.minecraft.server.dedicated.DedicatedServer server = (net.minecraft.server.dedicated.DedicatedServer) (MinecraftServer) (Object) this;
			server.executePendingCommands();
		}
	}

	// =====================================================================================================================================

}
