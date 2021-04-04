package de.scribble.lp.tasmod.mixin.killtherng;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import de.pfannekuchen.killtherng.networking.UpdateSeedPacket;
import de.scribble.lp.tasmod.CommonProxy;
import net.minecraft.client.Minecraft;

/**
 * Change the Seed of every Random, every keyboard input
 * @author Pancake
 */
@Mixin(Minecraft.class)
public abstract class MixinMinecraft {

	
    /**
     * Request a Seed Change for every Key Input
     * @param ci Mixin
     */
	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;dispatchKeypresses()V"), method = "runTickKeyboard")
	public void injectRunTickKeyboard(CallbackInfo ci) {
		CommonProxy.NETWORK.sendToServer(new UpdateSeedPacket());
	}
	
}
