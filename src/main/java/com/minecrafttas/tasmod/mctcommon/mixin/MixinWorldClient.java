package com.minecrafttas.tasmod.mctcommon.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.minecrafttas.tasmod.mctcommon.events.EventClient.EventPlayerLeaveClientSide;
import com.minecrafttas.tasmod.mctcommon.events.EventListenerRegistry;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;

@Mixin(ClientLevel.class)
public class MixinWorldClient {
	@Inject(method = "sendQuittingDisconnectingPacket", at = @At(value = "HEAD"))
	public void clientLeaveServerEvent(CallbackInfo ci) {
		EventListenerRegistry.fireEvent(EventPlayerLeaveClientSide.class, Minecraft.getInstance().player);
	}
}
