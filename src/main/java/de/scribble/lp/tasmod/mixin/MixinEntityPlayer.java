package de.scribble.lp.tasmod.mixin;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.entity.player.EntityPlayer;

@Mixin(EntityPlayer.class)
public class MixinEntityPlayer {
	@Redirect(method = "onLivingUpdate", at = @At(value = "FIELD", opcode = Opcodes.PUTFIELD, target = "Lnet/minecraft/entity/player/EntityPlayer;jumpMovementFactor:F", ordinal = 1))
	private void setJumpMovementFactor(EntityPlayer owner, float value) {
		owner.jumpMovementFactor=0.026F;
	}
}
