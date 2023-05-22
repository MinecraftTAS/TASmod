package com.minecrafttas.tasmod.mixin.savestates;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.world.chunk.storage.IChunkLoader;
import net.minecraft.world.gen.ChunkProviderServer;

@Mixin(ChunkProviderServer.class)
public interface AccessorChunkLoader {
	
	@Accessor
	public IChunkLoader getChunkLoader();
}
