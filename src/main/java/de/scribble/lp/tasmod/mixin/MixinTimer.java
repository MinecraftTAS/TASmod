package de.scribble.lp.tasmod.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.util.Timer;

@Mixin(Timer.class)
public class MixinTimer {
	
	@Shadow
	private float elapsedPartialTicks;
	
//	@Redirect(method = "updateTimer", at = @At(value = "FIELD", opcode = Opcodes.PUTFIELD, target = "Lnet/minecraft/util/Timer;elapsedPartialTicks:F"))
	public void redirect_updateTimer(Timer timer, float original) {
		System.out.println(original);
		elapsedPartialTicks = original;
	}
}
