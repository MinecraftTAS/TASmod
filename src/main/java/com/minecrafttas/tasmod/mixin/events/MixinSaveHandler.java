package com.minecrafttas.tasmod.mixin.events;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.minecrafttas.tasmod.mctcommon.events.EventListenerRegistry;
import com.minecrafttas.tasmod.events.EventNBT;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.LevelData;

@Mixin(LevelStorageSource.class)
public class MixinSaveHandler {

	@Inject(method = "saveWorldInfoWithPlayer", at = @At(value = "HEAD"))
	public void inject_onSaveWorldInfo(LevelData worldInfo, CompoundTag singlePlayerData, @Share(value = "worldInfo") LocalRef<LevelData> sharedWorldInfo) {
		sharedWorldInfo.set(worldInfo);
	}

	@ModifyArg(method = "saveWorldInfoWithPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/nbt/CompoundTag;setTag(Ljava/lang/String;Lnet/minecraft/nbt/CompoundTag;)V"), index = 1)
	public CompoundTag modifyarg_onSaveWorldInfo(CompoundTag singlePlayerCompound, @Share("worldInfo") LocalRef<LevelData> sharedWorldInfo) {
		LevelData worldInfo = sharedWorldInfo.get();
		return (CompoundTag) EventListenerRegistry.fireEvent(EventNBT.EventWorldWrite.class, singlePlayerCompound, worldInfo);
	}
}

