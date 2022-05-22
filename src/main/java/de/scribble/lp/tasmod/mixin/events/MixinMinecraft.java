package de.scribble.lp.tasmod.mixin.events;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import de.scribble.lp.tasmod.events.LoadWorldEvents;
import de.scribble.lp.tasmod.events.TickEvents;
import net.minecraft.client.Minecraft;

@Mixin(Minecraft.class)
public class MixinMinecraft {
	
	@Inject(method = "launchIntegratedServer", at = @At("HEAD"))
	public void inject_launchIntegratedServer(CallbackInfo ci) {
		LoadWorldEvents.startLaunchServer();
	}
	
	@Inject(method = "Lnet/minecraft/client/Minecraft;loadWorld(Lnet/minecraft/client/multiplayer/WorldClient;Ljava/lang/String;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/PlayerControllerMP;flipPlayer(Lnet/minecraft/entity/player/EntityPlayer;)V"))
	public void inject_loadWorld(CallbackInfo ci) {
		LoadWorldEvents.doneLoadingClientWorld();
	}
	
	@Inject(method = "runTick", at = @At("HEAD"))
	public void inject_runTick(CallbackInfo ci) {
		TickEvents.onClientTick();
	}
}
