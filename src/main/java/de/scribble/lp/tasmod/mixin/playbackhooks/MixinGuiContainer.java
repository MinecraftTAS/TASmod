package de.scribble.lp.tasmod.mixin.playbackhooks;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import de.scribble.lp.tasmod.ClientProxy;
import de.scribble.lp.tasmod.virtual.VirtualInput;
import net.minecraft.client.gui.inventory.GuiContainer;

@Mixin(GuiContainer.class)
public class MixinGuiContainer {
	@Redirect(method = "mouseClicked",at = @At(value="INVOKE", target = "Lorg/lwjgl/input/Keyboard;isKeyDown(I)Z",ordinal = 0, remap=false))
	private boolean redirectIsKeyDown(int i) {
		return ClientProxy.virtual.isKeyDown(i);
	}
	@Redirect(method = "mouseReleased",at = @At(value="INVOKE", target = "Lorg/lwjgl/input/Keyboard;isKeyDown(I)Z",ordinal = 0, remap=false))
	private boolean redirectIsKeyDown2(int i) {
		return VirtualInput.isKeyDown(i); //TODO
	}
}
