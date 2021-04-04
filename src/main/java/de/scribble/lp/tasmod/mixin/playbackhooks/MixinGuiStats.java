package de.scribble.lp.tasmod.mixin.playbackhooks;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import de.scribble.lp.tasmod.ClientProxy;
import net.minecraft.client.gui.achievement.GuiStats;

@Mixin(GuiStats.Stats.class)
public class MixinGuiStats {
	@Redirect(method = "drawListHeader(IILnet/minecraft/client/renderer/Tessellator;)V", at = @At(value = "INVOKE", target = "Lorg/lwjgl/input/Mouse/isButtonDown(I)Z"))
	public boolean redirectIsButtonDown(int i) {
		return !ClientProxy.virtual.isKeyDown(-100);
	}
}
