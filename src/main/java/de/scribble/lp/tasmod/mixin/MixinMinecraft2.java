package de.scribble.lp.tasmod.mixin;

import java.io.IOException;

import org.lwjgl.input.Keyboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import de.scribble.lp.tasmod.ClientProxy;
import de.scribble.lp.tasmod.ModLoader;
import de.scribble.lp.tasmod.duck.SubtickDuck;
import de.scribble.lp.tasmod.savestates.SavestateHandler;
import de.scribble.lp.tasmod.savestates.playerloading.SavestatePlayerLoading;
import de.scribble.lp.tasmod.tickratechanger.TickrateChangerClient;
import de.scribble.lp.tasmod.ticksync.TickSync;
import de.scribble.lp.tasmod.virtual.VirtualKeybindings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.util.Timer;

@Mixin(Minecraft.class)
public abstract class MixinMinecraft2 {

	// =====================================================================================================================================

	@Inject(method = "init", at = @At(value = "RETURN"))
	public void injectInit(CallbackInfo ci) {
		ModLoader.logger.debug("Initialising stuff for TASmod");
		new TickrateChangerClient();
		new TickSync();
	}

	// =====================================================================================================================================

	@Inject(method = "runGameLoop", at = @At(value = "HEAD"))
	public void injectRunGameLoop(CallbackInfo ci) {
		// TASmod
		VirtualKeybindings.increaseCooldowntimer();
		TickrateChangerClient.bypass();
		while (Keyboard.next()) {
			ClientProxy.virtual.updateNextKeyboard(Keyboard.getEventKey(), Keyboard.getEventKeyState(), Keyboard.getEventCharacter());
		}
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
		for (int j2 = 0; j2 < TickSync.getTickAmount((Minecraft) (Object) this); j2++) {
			if (TickrateChangerClient.TICKS_PER_SECOND != 0) {
				((SubtickDuck) this.entityRenderer).runSubtick(this.isGamePaused ? this.renderPartialTicksPaused : this.timer.renderPartialTicks);
			}
			this.runTick();
		}
		if (TickrateChangerClient.ADVANCE_TICK) {
			TickrateChangerClient.ADVANCE_TICK = false;
			TickrateChangerClient.changeClientTickrate(0F);
		}
	}

	@Shadow
	public abstract void runTick();

	// =====================================================================================================================================

	@Inject(method = "runTick", at = @At(value = "HEAD"))
	public void injectRunTick(CallbackInfo ci) throws IOException {
		TickSync.incrementClienttickcounter();

		if (SavestatePlayerLoading.wasLoading) {
			SavestatePlayerLoading.wasLoading = false;
			SavestateHandler.playerLoadSavestateEventClient();
		}
	}

	// =====================================================================================================================================

	@Inject(method = "runTickKeyboard", at = @At(value = "HEAD"))
	public void injectRunTickKeyboard(CallbackInfo ci) {
		ClientProxy.virtual.updateCurrentKeyboardEvents();
	}

	// =====================================================================================================================================

	@Redirect(method = "runTickKeyboard", at = @At(value = "INVOKE", target = "Lorg/lwjgl/input/Keyboard;next()Z"))
	public boolean redirectKeyboardNext() {
		return ClientProxy.virtual.nextKeyboardEvent();
	}

	// =====================================================================================================================================

	@Redirect(method = "runTickKeyboard", at = @At(value = "INVOKE", target = "Lorg/lwjgl/input/Keyboard;getEventKey()I"))
	public int redirectKeyboardGetEventKey() {
		return ClientProxy.virtual.getEventKey();
	}

	// =====================================================================================================================================

	@Redirect(method = "runTickKeyboard", at = @At(value = "INVOKE", target = "Lorg/lwjgl/input/Keyboard;getEventCharacter()C"))
	public char redirectKeyboardGetEventCharacter() {
		return ClientProxy.virtual.getEventCharacter();
	}

	// =====================================================================================================================================

	@Redirect(method = "runTickKeyboard", at = @At(value = "INVOKE", target = "Lorg/lwjgl/input/Keyboard;isKeyDown(I)Z"))
	public boolean redirectIsKeyDown(int keyCode) {
		return ClientProxy.virtual.isKeyDown(keyCode);
	}

	// =====================================================================================================================================
	
	@Redirect(method = "runTickKeyboard", at = @At(value = "INVOKE", target = "Lorg/lwjgl/input/Keyboard;getEventKeyState()Z"))
	public boolean redirectGetEventState() {
		return ClientProxy.virtual.getEventState();
	}
	
	// =====================================================================================================================================
	
	
}
