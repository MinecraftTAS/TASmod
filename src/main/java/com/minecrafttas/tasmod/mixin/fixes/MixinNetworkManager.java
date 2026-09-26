package com.minecrafttas.tasmod.mixin.fixes;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.minecrafttas.tasmod.TASmod;
import com.minecrafttas.tasmod.TASmodClient;

import net.minecraft.network.protocol.Packet;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.network.Connection;

@Mixin(Connection.class)
public class MixinNetworkManager {
	@Shadow
	private Packet packetListener;

	/**
	 * Fixes #137
	 * 
	 * @param manager
	 */
	@Redirect(method = "processReceivedPackets", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/Connection;tick()V"))
	public void fixes_processReceivedPackets(Connection manager) {
		if (TASmod.tickratechanger.ticksPerSecond == 0) {
			if (!(packetListener instanceof ServerGamePacketListenerImpl) || TASmodClient.loadingScreenHandler.isLoading()) {
				manager.tick();
			}
		} else {
			manager.tick();
		}
	}
}

