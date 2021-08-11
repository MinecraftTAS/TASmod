package de.scribble.lp.tasmod.mixin.killtherng.redirect;

import java.util.Random;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import de.pfannekuchen.killtherng.KillTheRNG;
import de.pfannekuchen.killtherng.utils.WorldRandom;
import net.minecraft.block.BlockDispenser;

@Mixin(BlockDispenser.class)
public class RedirectBlockDispenser {

	@Shadow
	protected Random rand;
	
	@Inject(at = @At("RETURN"), method = "<init>")
	public void hackRandom(CallbackInfo ci) {
		if (!KillTheRNG.ISDISABLED) rand = new WorldRandom();
	}
	
}
