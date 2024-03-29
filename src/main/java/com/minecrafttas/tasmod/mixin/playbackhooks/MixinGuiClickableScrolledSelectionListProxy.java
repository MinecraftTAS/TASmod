package com.minecrafttas.tasmod.mixin.playbackhooks;

import com.minecrafttas.tasmod.virtual.VirtualInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.minecrafttas.tasmod.TASmodClient;

import net.minecraft.client.gui.GuiClickableScrolledSelectionListProxy;

@Mixin(GuiClickableScrolledSelectionListProxy.class)
public class MixinGuiClickableScrolledSelectionListProxy {
	
	/**
	 * @return {@link VirtualInput.VirtualMouseInput#getEventMouseState()}
	 */
	@Redirect(method = "handleMouseInput", at = @At(value = "INVOKE", target = "Lorg/lwjgl/input/Mouse;getEventButtonState()Z", ordinal = 0, remap = false))
	public boolean redirectHandleMouseInput() {
		return TASmodClient.virtual.MOUSE.getEventMouseState();
	}
}
