package com.minecrafttas.tasmod.mixin.playbackhooks;

import com.minecrafttas.tasmod.virtual.VirtualInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.minecrafttas.tasmod.TASmodClient;

import net.minecraft.client.gui.GuiChat;

@Mixin(GuiChat.class)
public class MixinGuiChat {
	/**
	 * @return {@link VirtualInput.VirtualMouseInput#getEventMouseScrollWheel()}
	 */
	@Redirect(method = "handleMouseInput", at = @At(value = "INVOKE", target = "Lorg/lwjgl/input/Mouse;getEventDWheel()I", remap = false))
	public int redirectHandleMouseInput4() {
		return TASmodClient.virtual.MOUSE.getEventMouseScrollWheel();
	}
}
