package de.scribble.lp.tasmod.mixin.networking;

import java.io.IOException;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import de.scribble.lp.tasmod.ClientProxy;
import de.scribble.lp.tasmod.networking.Client;
import net.minecraft.network.NetworkManager;

@Mixin(NetworkManager.class)
public class MixinNetworkManager {
	
	@Inject(method = "closeChannel", at = @At("HEAD"))
	public void inject_closeChannel(CallbackInfo ci) {
		try {
			ClientProxy.packetClient.killClient();
		} catch (IOException e) {
			e.printStackTrace();
		}
//		try {
//			Client.killClient();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
	}
}
