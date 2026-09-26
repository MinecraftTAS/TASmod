package com.minecrafttas.tasmod.mixin.events;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.minecrafttas.tasmod.mctcommon.events.EventListenerRegistry;
import com.minecrafttas.tasmod.events.EventNBT;
import com.minecrafttas.tasmod.events.EventNBT.EventPlayerRead;
import com.minecrafttas.tasmod.events.EventNBT.EventPlayerWrite;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.nbt.CompoundTag;

/**
 * Implements {@link EventPlayerRead} and {@link EventPlayerWrite} events
 * 
 * @author Scribble
 */
@Mixin(ServerPlayer.class)
public class MixinEntityPlayerMP {

	@Inject(method = "readEntityFromNBT", at = @At(value = "RETURN"))
	public void readClientMotion(CompoundTag compound, CallbackInfo ci) {
		EventListenerRegistry.fireEvent(EventNBT.EventPlayerRead.class, compound, (ServerPlayer) (Object) this);
	}

	@Inject(method = "writeEntityToNBT", at = @At(value = "RETURN"))
	public void writeClientMotion(CompoundTag compound, CallbackInfo ci) {
		EventListenerRegistry.fireEvent(EventNBT.EventPlayerWrite.class, compound, (ServerPlayer) (Object) this);
	}
}

