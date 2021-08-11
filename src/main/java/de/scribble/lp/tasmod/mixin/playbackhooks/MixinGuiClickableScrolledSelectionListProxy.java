package de.scribble.lp.tasmod.mixin.playbackhooks;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import de.scribble.lp.tasmod.ClientProxy;
import net.minecraft.client.gui.GuiClickableScrolledSelectionListProxy;

@Mixin(GuiClickableScrolledSelectionListProxy.class)
public class MixinGuiClickableScrolledSelectionListProxy {
	@Redirect(method = "handleMouseInput", at = @At(value = "INVOKE", target = "Lorg/lwjgl/input/Mouse;getEventButtonState()Z", ordinal = 0, remap = false))
	public boolean redirectHandleMouseInput() {
		return ClientProxy.virtual.getEventMouseState();
	}
}
