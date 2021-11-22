package de.scribble.lp.tasmod.mixin.fixes;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import de.scribble.lp.tasmod.ClientProxy;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.GameSettings;

@Mixin(Minecraft.class)
public class MixinMinecraftFullscreen {
	
	@Shadow
	private GameSettings gameSettings;

	@Inject(method = "toggleFullscreen", at = @At("RETURN"))
	public void inject_toggleFullscreen(CallbackInfo ci) {
		int keyF11=this.gameSettings.keyBindFullscreen.getKeyCode();
		ClientProxy.virtual.getNextKeyboard().get(keyF11).setPressed(false);
	}
}
