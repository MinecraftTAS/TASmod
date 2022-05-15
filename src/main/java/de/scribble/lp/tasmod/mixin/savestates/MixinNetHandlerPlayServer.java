package de.scribble.lp.tasmod.mixin.savestates;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import de.scribble.lp.tasmod.TASmod;
import de.scribble.lp.tasmod.savestates.server.SavestateState;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;

@Mixin(NetHandlerPlayServer.class)
public class MixinNetHandlerPlayServer {
	
	
	@Redirect(method = "processPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/EntityPlayerMP;isInvulnerableDimensionChange()Z"))
	public boolean redirect_processPlayer(EntityPlayerMP parentIn) {
		return !parentIn.isInvulnerableDimensionChange() && (TASmod.savestateHandler.state!=SavestateState.LOADING && TASmod.savestateHandler.state!=SavestateState.WASLOADING);
	}
}
