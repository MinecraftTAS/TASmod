package de.scribble.lp.tasmod.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import de.scribble.lp.tasmod.ClientProxy;
import de.scribble.lp.tasmod.virtual.VirtualInput2;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.network.NetHandlerPlayClient;

@Mixin(NetHandlerPlayClient.class)
public class MixinNetHandlerPlayClient {
	@Inject(method = "handleJoinGame", at = @At(value = "HEAD"))
	public void clientJoinServerEvent(CallbackInfo ci) {
		ClientProxy.virtual.unpressEverything();
	}
}