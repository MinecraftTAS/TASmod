package de.scribble.lp.tasmod.mixin.events;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import de.scribble.lp.tasmod.events.LoadWorldEvents;
import net.minecraft.server.MinecraftServer;

@Mixin(MinecraftServer.class)
public class MixinMinecraftServer {
	
	@Inject(method = "initiateShutdown", at = @At("HEAD"))
	public void inject_initiateShutDown(CallbackInfo ci) {
		LoadWorldEvents.startShutdown();
	}
	
	@Inject(method = "run", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;init()Z", shift = Shift.AFTER))
	public void inject_run(CallbackInfo ci) {
		LoadWorldEvents.doneLaunchingServer();
	}
}
