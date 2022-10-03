package de.scribble.lp.tasmod.mixin.networking;

import java.io.IOException;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import de.scribble.lp.tasmod.networking.Client;
import de.scribble.lp.tasmod.networking.exceptions.ClientAlreadyRunningException;
import net.minecraft.client.network.NetHandlerLoginClient;

@Mixin(NetHandlerLoginClient.class)
public class MixinNetHandlerLoginClient {
	@Inject(method = "<init>", at = @At("RETURN"))
	public void inject_init(CallbackInfo ci) {
		try {
			Client.createClient();
		} catch (ClientAlreadyRunningException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
