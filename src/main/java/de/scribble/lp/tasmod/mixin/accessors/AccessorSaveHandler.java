package de.scribble.lp.tasmod.mixin.accessors;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.world.storage.SaveHandler;

@Mixin(SaveHandler.class)
public interface AccessorSaveHandler {

	@Invoker("setSessionLock")
	public void invokeSetSessionLock();
	
}
