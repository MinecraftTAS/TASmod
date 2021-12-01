package de.scribble.lp.tasmod.mixin.accessors;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.minecraft.world.storage.WorldInfo;

@Mixin(World.class)
public interface AccessorWorld {

	@Accessor("unloadedEntityList")
	public List<Entity> unloadedEntityList();
	
	@Accessor("worldInfo")
	public WorldInfo worldInfo();
	
	@Accessor("worldInfo")
	public void worldInfo(WorldInfo i);

}
