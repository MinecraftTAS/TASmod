package de.scribble.lp.tasmod.mixin.playbackhooks;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import de.scribble.lp.tasmod.virtual.VirtualMouseAndKeyboard;
import net.minecraft.client.gui.achievement.GuiStats;

@Mixin(GuiStats.class)
public class MixinGuiStats {
	@Redirect(method = "net/minecraft/client/gui/achievement/GuiStats/Stats;drawListHeader(I,I,Lnet/minecraft/client/renderer/Tessellator)V", at = @At(value = "INVOKE", target = "Lorg/lwjgl/input/Mouse/isButtonDown(I)Z",remap = false))
	private static boolean redirectIsButtonDown(int i) {
		return !VirtualMouseAndKeyboard.isKeyDown(-100);
	}
}
