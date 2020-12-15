package de.scribble.lp.tasmod.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import de.scribble.lp.tasmod.virtual.VirtualMouseAndKeyboard;
import net.minecraft.client.gui.achievement.GuiStats;

@Mixin(GuiStats.class)
public class MixinGuiStats {
	@Redirect(method = "drawListHeader", at = @At(value = "INVOKE", target = "Lorg/lwjgl/input/Mouse;isButtonDown(I)Z"))
	public boolean redirectIsButtonDown() {
		System.out.println("Test");
		return !VirtualMouseAndKeyboard.isKeyDown(-100);
	}
}
