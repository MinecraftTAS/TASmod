package com.minecrafttas.tasmod.mctcommon.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.minecrafttas.tasmod.mctcommon.events.EventClient;
import com.minecrafttas.tasmod.mctcommon.events.EventListenerRegistry;

import net.minecraft.client.Options;

@Mixin(Options.class)
public class MixinGameSettings {

	@Inject(method = "loadOptions", at = @At("HEAD"))
	public void events_loadOptions(CallbackInfo ci) {
		EventListenerRegistry.fireEvent(EventClient.EventOptionsInit.class, (Options) (Object) this);
	}
}
