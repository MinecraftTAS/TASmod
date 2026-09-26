package com.minecrafttas.tasmod.mixin.savestates;

import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.storage.RegionFileStorage;

@Mixin(RegionFileStorage.class)
public interface AccessorAnvilChunkLoader {

	@Accessor
	public Map<ChunkPos, CompoundTag> getChunksToSave();
}
