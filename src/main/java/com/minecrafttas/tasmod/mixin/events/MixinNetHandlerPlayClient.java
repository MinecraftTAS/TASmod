package com.minecrafttas.tasmod.mixin.events;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.minecrafttas.tasmod.mctcommon.events.EventClient.EventSetCameraAngle;
import com.minecrafttas.tasmod.mctcommon.events.EventListenerRegistry;

import net.minecraft.client.multiplayer.ClientPacketListener;

@Mixin(ClientPacketListener.class)
public class MixinNetHandlerPlayClient {

	@Inject(method = "handlePlayerPosLook", at = @At(value = "RETURN"))
	public void event_handlePlayerPosLook(CallbackInfo ci) {
		EventListenerRegistry.fireEvent(EventSetCameraAngle.class);
	}
}

