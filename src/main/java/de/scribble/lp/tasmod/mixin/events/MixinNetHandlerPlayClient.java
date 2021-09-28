package de.scribble.lp.tasmod.mixin.events;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import de.scribble.lp.tasmod.events.PlayerJoinLeaveEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetHandlerPlayClient;

@Mixin(NetHandlerPlayClient.class)
public class MixinNetHandlerPlayClient {
	@Shadow
	private Minecraft gameController;

	@Inject(method = "handleJoinGame", at = @At(value = "RETURN"))
	public void clientJoinServerEvent(CallbackInfo ci) {
		PlayerJoinLeaveEvents.firePlayerJoinedClientSide(gameController.player);
	}

	//Github Workflows can't process this for some reason...
	
//	@Inject(method = "handlePlayerListItem", at = @At(value = "HEAD"))
//	public void otherClientJoinServerEvent(SPacketPlayerListItem packet, CallbackInfo ci) {
//		for (int i = 0; i < packet.getEntries().size(); i++) {
//			if (packet.getAction() == Action.ADD_PLAYER) {
//				PlayerJoinLeaveEvents.fireOtherPlayerJoinedClientSide(packet.getEntries().get(i).getProfile());
//			}
//		}
//	}
}