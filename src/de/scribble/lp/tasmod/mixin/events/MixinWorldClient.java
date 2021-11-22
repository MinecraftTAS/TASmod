package de.scribble.lp.tasmod.mixin.events;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import de.scribble.lp.tasmod.events.PlayerJoinLeaveEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;

@Mixin(WorldClient.class)
public class MixinWorldClient {
	@Inject(method = "sendQuittingDisconnectingPacket", at = @At(value = "HEAD"))
	public void clientLeaveServerEvent(CallbackInfo ci) {
		PlayerJoinLeaveEvents.firePlayerLeaveClientSide(Minecraft.getMinecraft().player);
	}
}
