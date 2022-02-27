package de.scribble.lp.tasmod.mixin.accessors;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.util.Timer;

@Mixin(Timer.class)
public interface AccessorTimer {

	@Accessor("tickLength")
	public void tickLength(float f);
	
}
