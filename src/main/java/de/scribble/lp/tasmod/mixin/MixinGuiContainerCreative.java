package de.scribble.lp.tasmod.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import de.scribble.lp.tasmod.virtual.VirtualMouseAndKeyboard;
import net.minecraft.client.gui.inventory.GuiContainerCreative;

@Mixin(GuiContainerCreative.class)
public class MixinGuiContainerCreative {
	@Redirect(method = "handleMouseInput", at = @At(value = "INVOKE",target = "Lorg/lwjgl/input/Mouse;getEventDWheel()I",ordinal = 0), remap=false)
	public int redirectHandleMouseInput() {
		return VirtualMouseAndKeyboard.getEventDWheel();
	}
	@Redirect(method = "drawScreen", at = @At(value = "INVOKE",target = "Lorg/lwjgl/input/Mouse;isButtonDown(I)Z",ordinal = 0),remap=false)
	public boolean redirectHandleMouseInput2(int i) {
		return VirtualMouseAndKeyboard.isKeyDown(-100);
	}
}
