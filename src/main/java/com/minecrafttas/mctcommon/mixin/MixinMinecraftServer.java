package com.minecrafttas.mctcommon.mixin;

import com.minecrafttas.mctcommon.events.EventListenerRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.minecrafttas.mctcommon.events.EventServer.EventServerGameLoop;
import com.minecrafttas.mctcommon.events.EventServer.EventServerInit;
import com.minecrafttas.mctcommon.events.EventServer.EventServerStop;
import com.minecrafttas.mctcommon.events.EventServer.EventServerTick;

import net.minecraft.server.MinecraftServer;

@Mixin(MinecraftServer.class)
public class MixinMinecraftServer {
	
	@Inject(method = "run", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;init()Z", shift = Shift.AFTER))
	public void inject_init(CallbackInfo ci) {
		EventListenerRegistry.fireEvent(EventServerInit.class, (MinecraftServer)(Object)this);
	}
	
	@Inject(method = "initiateShutdown", at = @At("HEAD"))
	public void inject_initiateShutDown(CallbackInfo ci) {
		EventListenerRegistry.fireEvent(EventServerStop.class, (MinecraftServer)(Object)this);
	}
	
	@Inject(method = "tick", at = @At("HEAD"))
	public void inject_tick(CallbackInfo ci) {
		EventListenerRegistry.fireEvent(EventServerTick.class, (MinecraftServer)(Object)this);
	}
	
//	@Inject(method = "run", at = @At(value = "FIELD", target = "Lnet/minecraft/server/MinecraftServer;serverRunning:Z", shift = Shift.AFTER))
	public void inject_run(CallbackInfo ci) {
		EventListenerRegistry.fireEvent(EventServerGameLoop.class, (MinecraftServer)(Object)this);
	}
}
