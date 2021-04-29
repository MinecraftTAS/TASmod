package de.scribble.lp.tasmod.mixin;

import java.io.IOException;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import de.pfannekuchen.killtherng.networking.UpdateSeedPacket;
import de.scribble.lp.tasmod.ClientProxy;
import de.scribble.lp.tasmod.CommonProxy;
import de.scribble.lp.tasmod.duck.GuiScreenDuck;
import de.scribble.lp.tasmod.duck.SubtickDuck;
import de.scribble.lp.tasmod.savestates.SavestateHandler;
import de.scribble.lp.tasmod.savestates.playerloading.SavestatePlayerLoading;
import de.scribble.lp.tasmod.tickratechanger.TickrateChangerClient;
import de.scribble.lp.tasmod.ticksync.TickSync;
import de.scribble.lp.tasmod.virtual.VirtualKeybindings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.util.Timer;

@Mixin(Minecraft.class)
public abstract class MixinMinecraft2 {


	// =====================================================================================================================================

	@Shadow
	private GuiScreen currentScreen;
	
	@Inject(method = "runGameLoop", at = @At(value = "HEAD"))
	public void injectRunGameLoop(CallbackInfo ci) {
		// TASmod
		VirtualKeybindings.increaseCooldowntimer();
		TickrateChangerClient.bypass();
		while (Keyboard.next()) {
			ClientProxy.virtual.updateNextKeyboard(Keyboard.getEventKey(), Keyboard.getEventKeyState(), Keyboard.getEventCharacter());
		}
		while (Mouse.next()) {
			if(this.currentScreen==null) {
				ClientProxy.virtual.updateNextMouse(Mouse.getEventButton(), Mouse.getEventButtonState(), Mouse.getEventDWheel(), Mouse.getEventX(), Mouse.getEventY(), TickrateChangerClient.TICKS_PER_SECOND==0);
			} else {
				GuiScreenDuck screen=(GuiScreenDuck) currentScreen;
				ClientProxy.virtual.updateNextMouse(Mouse.getEventButton(), Mouse.getEventButtonState(), Mouse.getEventDWheel(), screen.calcX(Mouse.getEventX()), screen.calcY(Mouse.getEventY()), TickrateChangerClient.TICKS_PER_SECOND==0);
			}
		}
		ClientProxy.virtual.updateContainer();
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

	@Redirect(method = "runTickKeyboard", at = @At(value = "INVOKE", target = "Lorg/lwjgl/input/Keyboard;next()Z", remap = false))
	public boolean redirectKeyboardNext() {
		return ClientProxy.virtual.nextKeyboardEvent();
	}

	// =====================================================================================================================================

	@Redirect(method = "runTickKeyboard", at = @At(value = "INVOKE", target = "Lorg/lwjgl/input/Keyboard;getEventKey()I", remap = false))
	public int redirectKeyboardGetEventKey() {
		return ClientProxy.virtual.getEventKeyboardKey();
	}

	// =====================================================================================================================================

	@Redirect(method = "runTickKeyboard", at = @At(value = "INVOKE", target = "Lorg/lwjgl/input/Keyboard;getEventCharacter()C", remap = false))
	public char redirectKeyboardGetEventCharacter() {
		return ClientProxy.virtual.getEventKeyboardCharacter();
	}

	// =====================================================================================================================================

	@Redirect(method = "runTickKeyboard", at = @At(value = "INVOKE", target = "Lorg/lwjgl/input/Keyboard;isKeyDown(I)Z", remap = false))
	public boolean redirectIsKeyDown(int keyCode) {
		return ClientProxy.virtual.isKeyDown(keyCode);
	}

	// =====================================================================================================================================

	@Redirect(method = "runTickKeyboard", at = @At(value = "INVOKE", target = "Lorg/lwjgl/input/Keyboard;getEventKeyState()Z", remap = false))
	public boolean redirectGetEventState() {
		return ClientProxy.virtual.getEventKeyboardState();
	}

	// =====================================================================================================================================

	@Inject(method = "runTickMouse", at = @At(value = "HEAD"))
	public void injectRunTickMouse(CallbackInfo ci) {
		ClientProxy.virtual.updateCurrentMouseEvents();
	}

	// =====================================================================================================================================

	@Redirect(method = "runTickMouse", at = @At(value = "INVOKE", target = "Lorg/lwjgl/input/Mouse;next()Z", remap = false))
	public boolean redirectMouseNext() {
		return ClientProxy.virtual.nextMouseEvent();
	}

	// =====================================================================================================================================

	@Redirect(method = "runTickMouse", at = @At(value = "INVOKE", target = "Lorg/lwjgl/input/Mouse;getEventButton()I", remap = false))
	public int redirectMouseGetEventButton() {
		return ClientProxy.virtual.getEventMouseKey() + 100;
	}

	// =====================================================================================================================================

	@Redirect(method = "runTickMouse", at = @At(value = "INVOKE", target = "Lorg/lwjgl/input/Mouse;getEventButtonState()Z", remap = false))
	public boolean redirectGetEventButtonState() {
		return ClientProxy.virtual.getEventMouseState();
	}

	// =====================================================================================================================================

	@ModifyConstant(method = "runTickMouse", constant = @Constant(longValue = 200L))
	public long fixMouseWheel(long twohundredLong) {
		return (long) Math.max(4000F / TickrateChangerClient.TICKS_PER_SECOND, 200L);
	}

	// =====================================================================================================================================

	@Redirect(method = "runTickMouse", at = @At(value = "INVOKE", target = "Lorg/lwjgl/input/Mouse;getEventDWheel()I", remap = false))
	public int redirectGetEventDWheel() {
		return ClientProxy.virtual.getEventMouseScrollWheel();
	}

	// =====================================================================================================================================

	@Redirect(method = "dispatchKeypresses", at = @At(value = "INVOKE", target = "Lorg/lwjgl/input/Keyboard;getEventKey()I", remap = false))
	public int redirectGetEventKeyDPK() {
		return ClientProxy.virtual.getEventKeyboardKey();
	}

	// =====================================================================================================================================

	@Redirect(method = "dispatchKeypresses", at = @At(value = "INVOKE", target = "Lorg/lwjgl/input/Keyboard;getEventCharacter()C", remap = false))
	public char redirectGetEventCharacterDPK() {
		return ClientProxy.virtual.getEventKeyboardCharacter();
	}

	// =====================================================================================================================================

	@Redirect(method = "dispatchKeypresses", at = @At(value = "INVOKE", target = "Lorg/lwjgl/input/Keyboard;getEventKeyState()Z", remap = false))
	public boolean redirectGetEventKeyStateDPK() {
		return ClientProxy.virtual.getEventKeyboardState();
	}

	// =====================================================================================================================================

	/**
     * Request a Seed Change for every Key Input
     * @author Pancake
     * @param ci Mixin
     */
	@Inject(method = "runTickKeyboard", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;dispatchKeypresses()V"))
	public void injectRunTickKeyboard2(CallbackInfo ci) {
		CommonProxy.NETWORK.sendToServer(new UpdateSeedPacket());
	}
	
	@Inject(method = "runTickMouse", at = @At(value = "INVOKE", target = "Lorg/lwjgl/input/Mouse;getEventButton()I", remap = false))
	public void injectRunTickMouse2(CallbackInfo ci) {
		CommonProxy.NETWORK.sendToServer(new UpdateSeedPacket());
	}
	
	// =====================================================================================================================================
	
	@Inject(method = "runTick", at = @At(value = "RETURN"))
	public void injectRunTickReturn(CallbackInfo ci) {
		ClientProxy.virtual.getContainer().nextTick();
	}
}
