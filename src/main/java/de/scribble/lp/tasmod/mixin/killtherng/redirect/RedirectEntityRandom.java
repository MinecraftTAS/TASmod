package de.scribble.lp.tasmod.mixin.killtherng.redirect;

import java.util.Random;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import de.pfannekuchen.killtherng.KillTheRNG;
import de.pfannekuchen.killtherng.utils.EntityRandom;
import net.minecraft.entity.Entity;

@Mixin(Entity.class)
public class RedirectEntityRandom {

	@Redirect(method = "<init>", at = @At(value = "NEW", target = "Ljava/util/Random;<init>()Ljava/util/Random;", remap = false))
	public Random redirectRandom() {
		return KillTheRNG.ISDISABLED ? new Random() : new EntityRandom();
	}
	
}
