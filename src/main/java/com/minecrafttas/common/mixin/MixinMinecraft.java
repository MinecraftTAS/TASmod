package com.minecrafttas.common.mixin;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.minecrafttas.common.events.EventClient.EventClientGameLoop;
import com.minecrafttas.common.events.EventClient.EventClientInit;
import com.minecrafttas.common.events.EventClient.EventClientTick;
import com.minecrafttas.common.events.EventClient.EventDoneLoadingWorld;
import com.minecrafttas.common.events.EventClient.EventLaunchIntegratedServer;
import com.minecrafttas.common.events.EventClient.EventOpenGui;

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
		EventClientGameLoop.fireOnClientGameLoop((Minecraft)(Object)this);
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
	
	@Shadow
	private GuiScreen currentScreen;
	
	@Redirect(method = "displayGuiScreen", at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;currentScreen:Lnet/minecraft/client/gui/GuiScreen;", opcode = Opcodes.PUTFIELD))
	public void modify_displayGuiScreen(Minecraft mc, GuiScreen guiScreen) {
		guiScreen = EventOpenGui.fireOpenGuiEvent(guiScreen);
		currentScreen = guiScreen;
	}
}
