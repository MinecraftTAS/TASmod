package com.minecrafttas.tasmod.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.minecrafttas.tasmod.mctcommon.events.EventListenerRegistry;
import com.minecrafttas.tasmod.TASmodClient;
import com.minecrafttas.tasmod.events.EventClient.EventClientTickPost;
import com.minecrafttas.tasmod.util.Ducks.SubtickDuck;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.DeltaTracker;

@Mixin(Minecraft.class)
public abstract class MixinMinecraft {

	// =====================================================================================================================================

	@Shadow
	private Screen screen;

	@Inject(method = "runGameLoop", at = @At(value = "HEAD"))
	public void injectRunGameLoop(CallbackInfo ci) {
		TASmodClient.gameLoopSchedulerClient.runAllTasks();
	}

	// =====================================================================================================================================

	@Shadow
	private GameRenderer entityRenderer;
	@Shadow
	private boolean isGamePaused;
	@Shadow
	private float renderPartialTicksPaused;
	@Shadow
	private DeltaTracker timer;

	@Redirect(method = "runGameLoop", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;runTick()V"))
	public void redirectRunTick(Minecraft mc) {
		if (TASmodClient.tickratechanger.ticksPerSecond != 0) {
			((SubtickDuck) this.entityRenderer).runUpdate(this.isGamePaused ? this.renderPartialTicksPaused : 0.0f);
		}
		this.runTick();
		TASmodClient.tickSchedulerClient.runAllTasks();
		if (TASmodClient.tickratechanger.advanceTick) {
			TASmodClient.tickratechanger.advanceTick = false;
			TASmodClient.tickratechanger.changeClientTickrate(0F);
		}
		EventListenerRegistry.fireEvent(EventClientTickPost.class, (Minecraft) (Object) this);
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

	@ModifyConstant(method = "runTickMouse", constant = @Constant(longValue = 200L))
	public long fixMouseWheel(long twohundredLong) {
		return (long) Math.max(4000F / TASmodClient.tickratechanger.ticksPerSecond, 200L);
	}
}
