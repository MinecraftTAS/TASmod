package de.scribble.lp.tasmod.mixin.playbackhooks;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import de.scribble.lp.tasmod.ClientProxy;
import net.minecraft.client.gui.GuiSlot;

@Mixin(GuiSlot.class)
public class MixinGuiSlot {
	@Redirect(method = "handleMouseInput", at = @At(value = "INVOKE",target = "Lorg/lwjgl/input/Mouse;getEventButtonState()Z",ordinal = 0, remap = false))
	public boolean redirectHandleMouseInput() {
		return ClientProxy.virtual.getEventMouseState();
	}
	@Redirect(method = "handleMouseInput", at = @At(value = "INVOKE",target = "Lorg/lwjgl/input/Mouse;getEventButton()I",ordinal = 0, remap = false))
	public int redirectHandleMouseInput2() {
		return ClientProxy.virtual.getEventMouseKey();
	}
	@Redirect(method = "handleMouseInput", at = @At(value = "INVOKE",target = "Lorg/lwjgl/input/Mouse;isButtonDown(I)Z",ordinal = 0, remap = false))
	public boolean redirectHandleMouseInput3(int i) {
		return ClientProxy.virtual.isKeyDown(-100);
	}
	@Redirect(method = "handleMouseInput", at = @At(value = "INVOKE",target = "Lorg/lwjgl/input/Mouse;getEventDWheel()I",ordinal = 0, remap = false))
	public int redirectHandleMouseInput4() {
		return ClientProxy.virtual.getEventMouseScrollWheel();
	}
}
