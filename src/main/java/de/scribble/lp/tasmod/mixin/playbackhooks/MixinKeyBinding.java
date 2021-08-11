package de.scribble.lp.tasmod.mixin.playbackhooks;


import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import de.scribble.lp.tasmod.ClientProxy;
import net.minecraft.client.settings.KeyBinding;

@Mixin(KeyBinding.class)
public class MixinKeyBinding {
	@Redirect(method = "updateKeyBindState", at = @At(value = "INVOKE", target = "Lorg/lwjgl/input/Keyboard;isKeyDown(I)Z", remap = false))
	private static boolean redirectIsKeyDown(int i) {
		return ClientProxy.virtual.isKeyDown(i);
	}
}
