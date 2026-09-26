package com.minecrafttas.tasmod.events;

import com.minecrafttas.tasmod.mctcommon.events.EventListenerRegistry.EventBase;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.storage.LevelData;

public interface EventNBT {

	@FunctionalInterface
	public interface EventPlayerRead extends EventBase {
		public void onPlayerReadNBT(CompoundTag compound, ServerPlayer player);
	}

	@FunctionalInterface
	public interface EventPlayerWrite extends EventBase {
		public void onPlayerWriteNBT(CompoundTag compound, ServerPlayer player);
	}

	@FunctionalInterface
	public interface EventWorldRead extends EventBase {
		public void onWorldReadNBT(CompoundTag worldCompound);
	}

	@FunctionalInterface
	public interface EventWorldWrite extends EventBase {
		public CompoundTag onWorldWriteNBT(CompoundTag compound, LevelData worldInfo);
	}
}

