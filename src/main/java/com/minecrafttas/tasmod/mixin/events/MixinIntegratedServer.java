package com.minecrafttas.tasmod.mixin.events;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.minecrafttas.tasmod.events.LoadWorldEvents;

import net.minecraft.server.integrated.IntegratedServer;

@Mixin(IntegratedServer.class)
public class MixinIntegratedServer {
	
	@Inject(method = "init", at = @At("HEAD"))
	public void inject_init(CallbackInfoReturnable<Boolean> ci) {
		LoadWorldEvents.initServer();
	}
}
