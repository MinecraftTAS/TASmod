package de.scribble.lp.tasmod.mixin.accessors;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;

@Mixin(Entity.class)
public interface AccessorEntity {

	@Invoker("getVectorForRotation")
	public Vec3d invokeGetVectorForRotation(float f, float f2);
	
}
