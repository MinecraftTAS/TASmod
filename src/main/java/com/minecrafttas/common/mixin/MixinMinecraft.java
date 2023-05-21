package com.minecrafttas.common.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.minecrafttas.common.events.client.EventClientInit;
import com.minecrafttas.common.events.client.EventClientTick;
import com.minecrafttas.common.events.client.EventDoneLoadingWorld;
import com.minecrafttas.common.events.client.EventGameLoop;
import com.minecrafttas.common.events.client.EventLaunchIntegratedServer;
import com.minecrafttas.common.events.client.EventOpenGui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;

@Mixin(Minecraft.class)
public class MixinMinecraft {
	
	@Inject(method = "init", at = @At(value = "RETURN"))
	public void inject_init(CallbackInfo ci) {
		EventClientInit.fireOnClientInit((Minecraft)(Object)this);
	}
	
	@Inject(method = "runGameLoop", at = @At(value = "HEAD"))
	public void inject_runGameLoop(CallbackInfo ci) {
		EventGameLoop.fireOnRunGameLoop((Minecraft)(Object)this);
	}
	
	@Inject(method = "runTick", at = @At("HEAD"))
	public void inject_runTick(CallbackInfo ci) {
		EventClientTick.fireOnClientTick((Minecraft)(Object)this);
	}
	
	@Inject(method = "launchIntegratedServer", at = @At("HEAD"))
	public void inject_launchIntegratedServer(CallbackInfo ci) {
		EventLaunchIntegratedServer.fireOnLaunchIntegratedServer();
	}
	
	@Inject(method = "Lnet/minecraft/client/Minecraft;loadWorld(Lnet/minecraft/client/multiplayer/WorldClient;Ljava/lang/String;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/PlayerControllerMP;flipPlayer(Lnet/minecraft/entity/player/EntityPlayer;)V"))
	public void inject_loadWorld(CallbackInfo ci) {
		EventDoneLoadingWorld.fireOnDoneLoadingWorld();
	}
	
	
	@Inject(method = "displayGuiScreen", at = @At("HEAD"))
	public void inject_displayGuiScreen(GuiScreen guiScreen, CallbackInfo ci) {
		EventOpenGui.fireOpenGuiEvent(guiScreen);
	}
}
