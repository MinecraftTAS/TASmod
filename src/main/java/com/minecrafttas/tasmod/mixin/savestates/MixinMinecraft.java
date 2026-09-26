package com.minecrafttas.tasmod.mixin.savestates;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.minecrafttas.tasmod.savestates.handlers.SavestateResourcePackHandler;

import net.minecraft.client.Minecraft;

@Mixin(Minecraft.class)
public class MixinMinecraft {

	@Inject(method = "refreshResources", at = @At(value = "RETURN"))
	public void inject_refreshResources(CallbackInfo ci) {
		if (SavestateResourcePackHandler.clientRPLatch != null && SavestateResourcePackHandler.clientRPLatch.getCount() > 0) {
			SavestateResourcePackHandler.clientRPLatch.countDown();
		}
	}
}

