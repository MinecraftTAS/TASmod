package de.scribble.lp.tasmod.mixin.killtherng.redirect;

import java.util.Random;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import de.pfannekuchen.killtherng.KillTheRng;
import de.pfannekuchen.killtherng.utils.WorldRandom;
import net.minecraft.world.end.DragonSpawnManager;

@Mixin(DragonSpawnManager.class)
public class RedirectDragonSpawnManager {

	@Redirect(method = "Lnet/minecraft/world/end/DragonSpawnManager$SUMMONING_PILLARS;process", at = @At(value = "NEW", target = "Ljava/util/Random;<init>()Ljava/util/Random;"))
	public Random redirectRandom() {
		return KillTheRng.ISDISABLED ? new Random() : new WorldRandom();
	}
	
}
