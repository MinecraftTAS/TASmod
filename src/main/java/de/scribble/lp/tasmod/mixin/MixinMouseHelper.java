package de.scribble.lp.tasmod.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import de.scribble.lp.tasmod.ClientProxy;
import net.minecraft.util.MouseHelper;

@Mixin(MouseHelper.class)
public class MixinMouseHelper {
	@Redirect(method = "mouseXYChange", at = @At(value = "INVOKE", target = "getDX"))
	public int redirectGetDX() {
		return ClientProxy.virtual.getDeltaX();
	}
	
	@Redirect(method = "mouseXYChange", at = @At(value = "INVOKE", target = "getDY"))
	public int redirectGetDY() {
		return ClientProxy.virtual.getDeltaY();
	}
}
