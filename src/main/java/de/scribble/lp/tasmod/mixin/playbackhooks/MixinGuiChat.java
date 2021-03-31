package de.scribble.lp.tasmod.mixin.playbackhooks;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import de.scribble.lp.tasmod.virtual.VirtualInput;
import net.minecraft.client.gui.GuiChat;

@Mixin(GuiChat.class)
public class MixinGuiChat {
	@Redirect(method = "handleMouseInput", at = @At(value = "INVOKE",target = "Lorg/lwjgl/input/Mouse;getEventDWheel()I", remap = false))
	public int redirectHandleMouseInput4() {
		return VirtualInput.getEventDWheel(); //TODO
	}
}
