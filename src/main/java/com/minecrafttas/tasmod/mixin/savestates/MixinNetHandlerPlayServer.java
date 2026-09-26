package com.minecrafttas.tasmod.mixin.savestates;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.minecrafttas.tasmod.TASmod;
import com.minecrafttas.tasmod.savestates.SavestateHandlerServer.SavestateState;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

@Mixin(ServerGamePacketListenerImpl.class)
public class MixinNetHandlerPlayServer {

	/**
	 * Disables the "Player moved wrongly!" message during a savestate 
	 */
	@Redirect(method = "processPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/ServerPlayer;isInvulnerableDimensionChange()Z"))
	public boolean redirect_processPlayer(ServerPlayer parentIn) {
		return TASmod.savestateHandlerServer.getState() != SavestateState.LOADING;
	}
}
