package de.scribble.lp.tasmod.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.client.Minecraft;

@Mixin(Minecraft.class)
public interface AccessorRunStuff {
	@Invoker("runTickKeyboard")
	public void runTickKeyboardAccessor();
	
	@Invoker("runTickMouse")
	public void runTickMouseAccessor();
}
