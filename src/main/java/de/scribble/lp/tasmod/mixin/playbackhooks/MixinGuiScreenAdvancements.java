package de.scribble.lp.tasmod.mixin.playbackhooks;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import de.scribble.lp.tasmod.ClientProxy;
import net.minecraft.client.gui.advancements.GuiScreenAdvancements;

@Mixin(GuiScreenAdvancements.class)
public class MixinGuiScreenAdvancements {
	@Redirect(method = "drawScreen", at = @At(value = "INVOKE", target = "Lorg/lwjgl/input/Mouse;isButtonDown(I)Z", remap = false))
	public boolean redirectIsButtonDown(int i) {
		return ClientProxy.virtual.isKeyDown(-100);
	}
}
