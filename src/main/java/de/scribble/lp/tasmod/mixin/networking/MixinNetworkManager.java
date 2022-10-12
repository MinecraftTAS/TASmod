package de.scribble.lp.tasmod.mixin.networking;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import de.scribble.lp.tasmod.CommonProxy;
import de.scribble.lp.tasmod.ticksync.TickSyncQuitPacket;
import net.minecraft.network.NetworkManager;

@Mixin(NetworkManager.class)
public class MixinNetworkManager {
	
	@Inject(method = "closeChannel", at = @At("HEAD"))
	public void inject_closeChannel(CallbackInfo ci) {
		CommonProxy.NETWORK.sendToServer(new TickSyncQuitPacket());
//		try {
//			Client.killClient();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
	}
}
