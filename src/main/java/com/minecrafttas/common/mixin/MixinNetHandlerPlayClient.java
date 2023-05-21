package com.minecrafttas.common.mixin;

import java.net.ConnectException;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.minecrafttas.common.events.client.player.EventOtherPlayerJoinedClientSide;
import com.minecrafttas.common.events.client.player.EventPlayerJoinedClientSide;

import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.play.server.SPacketPlayerListItem;
import net.minecraft.network.play.server.SPacketPlayerListItem.Action;

@Mixin(NetHandlerPlayClient.class)
public class MixinNetHandlerPlayClient {
	@Shadow
	private Minecraft gameController;

	@Inject(method = "handleJoinGame", at = @At(value = "RETURN"))
	public void clientJoinServerEvent(CallbackInfo ci) throws ConnectException {
		EventPlayerJoinedClientSide.firePlayerJoinedClientSide(gameController.player);
	}

	//Github Workflows can't process this for some reason...
	
	@Inject(method = "handlePlayerListItem", at = @At(value = "HEAD"))
	public void otherClientJoinServerEvent(SPacketPlayerListItem packet, CallbackInfo ci) {
		for (int i = 0; i < packet.getEntries().size(); i++) {
			if (packet.getAction() == Action.ADD_PLAYER) {
				EventOtherPlayerJoinedClientSide.fireOtherPlayerJoinedClientSide(packet.getEntries().get(i).getProfile());
			}
		}
	}
}
