package com.minecrafttas.tasmod.mixin.fixes;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.minecrafttas.tasmod.TASmod;
import com.minecrafttas.tasmod.TASmodClient;

import net.minecraft.network.INetHandler;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.NetworkManager;
import net.minecraft.util.ITickable;

@Mixin(NetworkManager.class)
public class MixinNetworkManager {
	@Shadow
	private INetHandler packetListener;

	/**
	 * Fixes #137
	 * 
	 * @param manager
	 */
	@Redirect(method = "processReceivedPackets", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/ITickable;update()V"))
	public void redirect_processReceivedPackets(ITickable manager) {
		if (TASmod.tickratechanger.ticksPerSecond == 0) {
			if (!(packetListener instanceof NetHandlerPlayServer) || TASmodClient.loadingScreenHandler.isLoading()) {
				manager.update();
			}
		} else {
			manager.update();
		}
	}
}
