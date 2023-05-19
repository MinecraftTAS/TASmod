package com.minecrafttas.tasmod.mixin.playbackhooks;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.minecrafttas.tasmod.TASmodClient;

import net.minecraft.client.gui.GuiSlot;

@Mixin(GuiSlot.class)
public class MixinGuiSlot {
	@Redirect(method = "handleMouseInput", at = @At(value = "INVOKE",target = "Lorg/lwjgl/input/Mouse;getEventButtonState()Z",ordinal = 0, remap = false))
	public boolean redirectHandleMouseInput() {
		return TASmodClient.virtual.getEventMouseState();
	}
	@Redirect(method = "handleMouseInput", at = @At(value = "INVOKE",target = "Lorg/lwjgl/input/Mouse;getEventButton()I",ordinal = 0, remap = false))
	public int redirectHandleMouseInput2() {
		return TASmodClient.virtual.getEventMouseKey();
	}
	@Redirect(method = "handleMouseInput", at = @At(value = "INVOKE",target = "Lorg/lwjgl/input/Mouse;isButtonDown(I)Z",ordinal = 0, remap = false))
	public boolean redirectHandleMouseInput3(int i) {
		return TASmodClient.virtual.isKeyDown(-100);
	}
	@Redirect(method = "handleMouseInput", at = @At(value = "INVOKE",target = "Lorg/lwjgl/input/Mouse;getEventDWheel()I",ordinal = 0, remap = false))
	public int redirectHandleMouseInput4() {
		return TASmodClient.virtual.getEventMouseScrollWheel();
	}
}
