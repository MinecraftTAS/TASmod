package de.scribble.lp.tasmod.mixin.killtherng;

import java.util.Random;
import java.util.UUID;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import de.pfannekuchen.killtherng.KillTheRng;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;

/**
 * When an entity gets created it gets a randomly generated UUID, this is bad, because UUID's cannot be the same or the 
 * Entity won't spawn.
 * 
 * @author Pancake
 */
@Mixin(Entity.class)
public class MixinUUIDFix {

	/**
	 * @author Pancake
	 * @param rng Parameter from Mixin, this is the actual Random used for this. It's our HijackedRandom, so we ignore it.
	 * @return Returns an actually random :O UUID.
	 */
	@Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/MathHelper;getRandomUUID(Ljava/util/Random;)Ljava/util/UUID;"))
	public UUID redirectRandom(Random rng) {
		return KillTheRng.ISDISABLED ? MathHelper.getRandomUUID(rng) : UUID.randomUUID();
	}
	
}
