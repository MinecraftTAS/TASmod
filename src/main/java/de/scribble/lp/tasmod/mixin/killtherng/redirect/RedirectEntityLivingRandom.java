package de.scribble.lp.tasmod.mixin.killtherng.redirect;

import java.util.Random;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.entity.EntityLiving;

@Mixin(EntityLiving.class)
public class RedirectEntityLivingRandom {

	@Redirect(method = "dropLoot", at = @At(value = "NEW", target = "Ljava/util/Random;<init>(J)Ljava/util/Random;"))
	public Random redirectRandom(long originalSeed) {
		return new Random(originalSeed);
	}
	
}
