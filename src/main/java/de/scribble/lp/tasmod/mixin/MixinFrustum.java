package de.scribble.lp.tasmod.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.renderer.culling.Frustum;

@Mixin(Frustum.class)
public class MixinFrustum {

	@Inject(at = @At("HEAD"), method = "isBoxInFrustum", cancellable = true)
	public void hacked(CallbackInfoReturnable<Boolean> cir) {
		cir.setReturnValue(true);
		cir.cancel();
	}

	@Inject(at = @At("HEAD"), method = "isBoundingBoxInFrustum", cancellable = true)
	public void hacked2(CallbackInfoReturnable<Boolean> cir) {
		cir.setReturnValue(true);
		cir.cancel();
	}
	
}
