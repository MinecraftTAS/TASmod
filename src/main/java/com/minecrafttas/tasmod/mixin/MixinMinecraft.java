package com.minecrafttas.tasmod.mixin;

import java.io.IOException;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.minecrafttas.tasmod.TASmodClient;
import com.minecrafttas.tasmod.events.EventClient.EventClientTickPost;
import com.minecrafttas.tasmod.savestates.SavestateHandlerServer;
import com.minecrafttas.tasmod.util.Ducks.SubtickDuck;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.util.Timer;

@Mixin(Minecraft.class)
public abstract class MixinMinecraft {


	// =====================================================================================================================================

	@Shadow
	private GuiScreen currentScreen;
	
	@Inject(method = "runGameLoop", at = @At(value = "HEAD"))
	public void injectRunGameLoop(CallbackInfo ci) {
		TASmodClient.gameLoopSchedulerClient.runAllTasks();
	}

	// =====================================================================================================================================

	@Shadow
	private EntityRenderer entityRenderer;
	@Shadow
	private boolean isGamePaused;
	@Shadow
	private float renderPartialTicksPaused;
	@Shadow
	private Timer timer;

	@Redirect(method = "runGameLoop", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;runTick()V"))
	public void redirectRunTick(Minecraft mc) {
		TASmodClient.virtual.updateContainer();
		if (TASmodClient.tickratechanger.ticksPerSecond != 0) {
			((SubtickDuck) this.entityRenderer).runSubtick(this.isGamePaused ? this.renderPartialTicksPaused : this.timer.renderPartialTicks);
		}
		this.runTick();
		TASmodClient.tickSchedulerClient.runAllTasks();
		if (TASmodClient.tickratechanger.advanceTick) {
			TASmodClient.tickratechanger.advanceTick = false;
			TASmodClient.tickratechanger.changeClientTickrate(0F);
		}
		EventClientTickPost.fireOnClientPostTick((Minecraft)(Object)this);
	}

	@Shadow
	public abstract void runTick();

	@Inject(method = "shutdownMinecraftApplet", at = @At("HEAD"))
	public void inject_shutdownMinecraftApplet(CallbackInfo ci) {
		try {
			if (TASmodClient.client != null) {
				TASmodClient.tickratechanger.changeTickrate(20);
				TASmodClient.client.disconnect();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Inject(method = "runTick", at = @At(value = "HEAD"))
	public void injectRunTick(CallbackInfo ci) throws IOException {
		if (SavestateHandlerServer.wasLoading) {
			SavestateHandlerServer.wasLoading = false;
			
			if(Minecraft.getMinecraft().player!=null) { 		//The player can be null when loading a savestate and quitting to the main menu
				SavestateHandlerServer.playerLoadSavestateEventClient(); // TODO Replace with event
			}
		}
	}

	@ModifyConstant(method = "runTickMouse", constant = @Constant(longValue = 200L))
	public long fixMouseWheel(long twohundredLong) {
		return (long) Math.max(4000F / TASmodClient.tickratechanger.ticksPerSecond, 200L);
	}

	@Inject(method = "runTick", at = @At(value = "RETURN"))
	public void injectRunTickReturn(CallbackInfo ci) {
		TASmodClient.controller.nextTick(); // TODO Replace with event
	}
}
