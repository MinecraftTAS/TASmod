package com.minecrafttas.mctcommon.mixin;

import com.minecrafttas.mctcommon.events.EventListenerRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.minecrafttas.mctcommon.events.EventClient.EventPlayerLeaveClientSide;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;

@Mixin(WorldClient.class)
public class MixinWorldClient {
	@Inject(method = "sendQuittingDisconnectingPacket", at = @At(value = "HEAD"))
	public void clientLeaveServerEvent(CallbackInfo ci) {
		EventListenerRegistry.fireEvent(EventPlayerLeaveClientSide.class, Minecraft.getMinecraft().player);
	}
}