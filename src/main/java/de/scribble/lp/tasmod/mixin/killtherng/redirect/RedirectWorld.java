package de.scribble.lp.tasmod.mixin.killtherng.redirect;

import java.util.Random;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import de.pfannekuchen.killtherng.KillTheRng;
import de.pfannekuchen.killtherng.utils.WorldRandom;
import net.minecraft.world.World;

@Mixin(World.class)
public class RedirectWorld {
	
	@Shadow @Mutable
	public Random rand;
	
	@Inject(at = @At("RETURN"), method = "<init>")
	public void redirectRandom(CallbackInfo ci) {
		if (!KillTheRng.ISDISABLED) rand = new WorldRandom();
	}
	
}
