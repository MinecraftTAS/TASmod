package com.minecrafttas.tasmod.mixin.savestates;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.entity.ai.EntityAIBase;

@Mixin(EntityAIBase.class)
public class TypeAdapterGenerator {
	@Inject(method = "<init>", at = @At("RETURN"))
	public void inject_aiBase(CallbackInfo ci) {
//		EntityAIBase thisObj = (EntityAIBase) (Object) this;
//		System.out.println(thisObj);
//		TASmod.generator.addClass(thisObj.getClass());
	}
}
