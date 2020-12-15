package de.scribble.lp.tasmod.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import de.scribble.lp.tasmod.virtual.VirtualMouseAndKeyboard;
import net.minecraft.client.gui.GuiClickableScrolledSelectionListProxy;

@Mixin(GuiClickableScrolledSelectionListProxy.class)
public class MixinGuiClickableScrolledSelectionListProxy {
	@Redirect(method = "handleMouseInput", at = @At(value = "INVOKE",target = "Lorg/lwjgl/input/Mouse;getEventButtonState()Z",ordinal = 0))
	public boolean redirectHandleMouseInput() {
		return VirtualMouseAndKeyboard.getEventMouseButtonState();
	}
}
