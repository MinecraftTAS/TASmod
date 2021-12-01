package de.scribble.lp.tasmod.mixin.accessors;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.server.MinecraftServer;

@Mixin(MinecraftServer.class)
public interface AccessorMinecraftServer {

	@Accessor("tickCounter")
	public void tickCounter(int i);
	
}