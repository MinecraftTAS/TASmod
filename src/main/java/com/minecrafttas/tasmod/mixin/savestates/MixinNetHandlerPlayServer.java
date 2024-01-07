package com.minecrafttas.tasmod.mixin.savestates;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.minecrafttas.tasmod.TASmod;
import com.minecrafttas.tasmod.savestates.SavestateHandlerServer.SavestateState;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;

@Mixin(NetHandlerPlayServer.class)
public class MixinNetHandlerPlayServer {

	@Redirect(method = "processPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/EntityPlayerMP;isInvulnerableDimensionChange()Z"))
	public boolean redirect_processPlayer(EntityPlayerMP parentIn) {
		return !parentIn.isInvulnerableDimensionChange() && TASmod.savestateHandlerServer.state!=SavestateState.LOADING;
	}
}
