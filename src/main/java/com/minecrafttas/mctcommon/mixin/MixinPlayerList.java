package com.minecrafttas.mctcommon.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.minecrafttas.mctcommon.events.EventServer.EventPlayerJoinedServerSide;
import com.minecrafttas.mctcommon.events.EventServer.EventPlayerLeaveServerSide;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetworkManager;
import net.minecraft.server.management.PlayerList;

@Mixin(PlayerList.class)
public class MixinPlayerList {
	@Inject(method="initializeConnectionToPlayer", at=@At("RETURN"), remap = false)
	public void inject_initializeConnectionToPlayer(NetworkManager netManager, EntityPlayerMP playerIn, CallbackInfo ci){
		EventPlayerJoinedServerSide.firePlayerJoinedServerSide(playerIn);
	}
	
	@Inject(method = "playerLoggedOut", at = @At("HEAD"))
	public void inject_playerLoggedOut(EntityPlayerMP playerIn, CallbackInfo ci) {
		EventPlayerLeaveServerSide.firePlayerLeaveServerSide(playerIn);
	}
}
