package com.minecrafttas.tasmod.mixin.events;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;

import com.minecrafttas.tasmod.mctcommon.events.EventListenerRegistry;
import com.minecrafttas.tasmod.events.EventNBT;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.storage.LevelStorageSource;

@Mixin(LevelStorageSource.class)
public class MixinSaveFormatOld {

	@ModifyArgs(method = "getWorldData", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/datafix/DataFixer;process(Lnet/minecraft/util/datafix/FixTypes;Lnet/minecraft/nbt/CompoundTag;)Lnet/minecraft/nbt/CompoundTag;"))
	public CompoundTag modifyargs_getWorldData(CompoundTag compound) {
		EventListenerRegistry.fireEvent(EventNBT.EventWorldRead.class, compound);
		return compound;
	}
}

