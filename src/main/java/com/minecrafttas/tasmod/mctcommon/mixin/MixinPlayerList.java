package com.minecrafttas.tasmod.mctcommon.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.minecrafttas.tasmod.mctcommon.events.EventListenerRegistry;
import com.minecrafttas.tasmod.mctcommon.events.EventServer.EventPlayerJoinedServerSide;
import com.minecrafttas.tasmod.mctcommon.events.EventServer.EventPlayerLeaveServerSide;

import net.minecraft.network.Connection;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;

@Mixin(PlayerList.class)
public class MixinPlayerList {
	@Inject(method = "initializeConnectionToPlayer", at = @At("RETURN"), remap = false)
	public void inject_initializeConnectionToPlayer(Connection netManager, ServerPlayer playerIn, CallbackInfo ci) {
		EventListenerRegistry.fireEvent(EventPlayerJoinedServerSide.class, playerIn);
	}

	@Inject(method = "playerLoggedOut", at = @At("HEAD"))
	public void inject_playerLoggedOut(ServerPlayer playerIn, CallbackInfo ci) {
		EventListenerRegistry.fireEvent(EventPlayerLeaveServerSide.class, playerIn);
	}
}
