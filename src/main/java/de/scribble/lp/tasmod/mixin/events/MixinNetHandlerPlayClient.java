package de.scribble.lp.tasmod.mixin.events;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import de.scribble.lp.tasmod.events.PlayerJoinLeaveEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.play.server.SPacketPlayerListItem;
import net.minecraft.network.play.server.SPacketPlayerListItem.Action;

@Mixin(NetHandlerPlayClient.class)
public class MixinNetHandlerPlayClient {
	@Shadow
	private Minecraft gameController;

	@Inject(method = "handleJoinGame", at = @At(value = "RETURN"))
	public void clientJoinServerEvent(CallbackInfo ci) {
		PlayerJoinLeaveEvents.firePlayerJoinedClientSide(gameController.player);
	}

	@ModifyVariable(method = "handlePlayerListItem", at = @At(value = "STORE"), index = 3, ordinal = 0)
	public net.minecraft.network.play.server.SPacketPlayerListItem.AddPlayerData otherClientJoinServerEvent(net.minecraft.network.play.server.SPacketPlayerListItem.AddPlayerData sentData, SPacketPlayerListItem packet) {
		if (packet.getAction() == Action.ADD_PLAYER) {
			PlayerJoinLeaveEvents.fireOtherPlayerJoinedClientSide(sentData.getProfile());
		}
		return sentData;
	}
}