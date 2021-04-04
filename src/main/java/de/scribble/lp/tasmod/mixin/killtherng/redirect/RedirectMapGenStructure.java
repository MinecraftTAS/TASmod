package de.scribble.lp.tasmod.mixin.killtherng.redirect;

import java.util.Random;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import de.pfannekuchen.killtherng.KillTheRng;
import de.pfannekuchen.killtherng.utils.WorldRandom;
import net.minecraft.world.gen.structure.MapGenStructure;

@Mixin(MapGenStructure.class)
public class RedirectMapGenStructure {

	@Redirect(method = "findNearestStructurePosBySpacing", at = @At(value = "NEW", target = "Ljava/util/Random;<init>()Ljava/util/Random;"))
	private static Random redirectRandom2() {
		return KillTheRng.ISDISABLED ? new Random() : new WorldRandom();
	}
	
}
