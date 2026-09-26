package com.minecrafttas.tasmod.mixin.clientcommands;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.minecrafttas.tasmod.registries.TASmodAPIRegistry;

import net.minecraft.client.player.LocalPlayer;

@Mixin(LocalPlayer.class)
public class MixinEntityPlayerSP {

	@Inject(method = "sendChatMessage", at = @At("HEAD"), cancellable = true)
	public void inject_sendChatMessage(String message, CallbackInfo ci) {
		if (TASmodAPIRegistry.CLIENT_COMMANDS.runClientCommands(message)) {
			ci.cancel();
		}
	}
}

