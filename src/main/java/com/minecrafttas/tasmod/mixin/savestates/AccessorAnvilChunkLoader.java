package com.minecrafttas.tasmod.mixin.savestates;

import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.storage.AnvilChunkLoader;

@Mixin(AnvilChunkLoader.class)
public interface AccessorAnvilChunkLoader {
	
	@Accessor
	public  Map<ChunkPos, NBTTagCompound> getChunksToSave();
}
