package com.minecrafttas.tasmod.mixin.savestates;

import java.util.Set;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import com.minecrafttas.tasmod.util.Ducks.ClientLevelDuck;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;

@Mixin(ClientLevel.class)
public class MixinWorldClient implements ClientLevelDuck {

	@Shadow
	@Final
	private Set<Entity> entityList;

	@Override
	public void clearEntityList() {
		entityList.clear();
	}

}

