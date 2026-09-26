package com.minecrafttas.tasmod.mctcommon.mixin;

import java.net.ConnectException;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.minecrafttas.tasmod.mctcommon.events.EventClient.EventOtherPlayerJoinedClientSide;
import com.minecrafttas.tasmod.mctcommon.events.EventClient.EventPlayerJoinedClientSide;
import com.minecrafttas.tasmod.mctcommon.events.EventListenerRegistry;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket.Entry;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket.Action;

@Mixin(ClientPacketListener.class)
public class MixinNetHandlerPlayClient {
	@Shadow
	private Minecraft client;

	@Inject(method = "handleJoinGame", at = @At(value = "RETURN"))
	public void clientJoinServerEvent(CallbackInfo ci) throws ConnectException {
		EventListenerRegistry.fireEvent(EventPlayerJoinedClientSide.class, client.player);
	}

	@Inject(method = "handlePlayerInfoUpdate", at = @At(value = "HEAD"))
	public void otherClientJoinServerEvent(ClientboundPlayerInfoUpdatePacket packet, CallbackInfo ci) {
		// ClientboundPlayerInfoUpdatePacket action field access varies by version
		Action action = null;
		try {
			java.lang.reflect.Field field = ClientboundPlayerInfoUpdatePacket.class.getDeclaredField("action");
			field.setAccessible(true);
			action = (Action) field.get(packet);
		} catch (Exception e) {
			// Fallback: check if there's an actions() method
			try {
				java.lang.reflect.Method method = ClientboundPlayerInfoUpdatePacket.class.getMethod("actions");
				action = (Action) method.invoke(packet);
			} catch (Exception ignored) {}
		}
		
		if (action == Action.ADD_PLAYER) {
			for (Entry entry : packet.entries()) {
				EventListenerRegistry.fireEvent(EventOtherPlayerJoinedClientSide.class, entry.profile());
			}
		}
	}
}
