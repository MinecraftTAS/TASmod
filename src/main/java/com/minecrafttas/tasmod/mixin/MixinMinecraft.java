package com.minecrafttas.tasmod.mixin;

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

import com.minecrafttas.tasmod.TASmodClient;
import com.minecrafttas.tasmod.externalGui.InputContainerView;
import com.minecrafttas.tasmod.savestates.server.SavestateHandler;
import com.minecrafttas.tasmod.savestates.server.playerloading.SavestatePlayerLoading;
import com.minecrafttas.tasmod.ticksync.TickSyncClient;
import com.minecrafttas.tasmod.util.Ducks.GuiScreenDuck;
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
		
		while (Keyboard.next()) {
			TASmodClient.virtual.updateNextKeyboard(Keyboard.getEventKey(), Keyboard.getEventKeyState(), Keyboard.getEventCharacter());
		}
		while (Mouse.next()) {
			if(this.currentScreen == null) {
				TASmodClient.virtual.updateNextMouse(Mouse.getEventButton(), Mouse.getEventButtonState(), Mouse.getEventDWheel(), Mouse.getEventX(), Mouse.getEventY(), TASmodClient.tickratechanger.ticksPerSecond==0);
			} else {
				GuiScreenDuck screen = (GuiScreenDuck) currentScreen;
				TASmodClient.virtual.updateNextMouse(Mouse.getEventButton(), Mouse.getEventButtonState(), Mouse.getEventDWheel(), screen.calcX(Mouse.getEventX()), screen.calcY(Mouse.getEventY()), TASmodClient.tickratechanger.ticksPerSecond==0);
			}
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
		TickSyncClient.clientPostTick((Minecraft)(Object)this);
	}

	@Shadow
	public abstract void runTick();

	@Inject(method = "shutdownMinecraftApplet", at = @At("HEAD"))
	public void inject_shutdownMinecraftApplet(CallbackInfo ci) {
		try {
			if (TASmodClient.packetClient != null) {
				TASmodClient.tickratechanger.changeTickrate(20);
				TASmodClient.packetClient.killClient();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	// =====================================================================================================================================

	@Inject(method = "runTick", at = @At(value = "HEAD"))
	public void injectRunTick(CallbackInfo ci) throws IOException {
		
		InputContainerView.update(TASmodClient.virtual);
		if (SavestatePlayerLoading.wasLoading) {
			SavestatePlayerLoading.wasLoading = false;
			
			if(Minecraft.getMinecraft().player!=null) { 		//The player can be null when loading a savestate and quitting to the main menu
				SavestateHandler.playerLoadSavestateEventClient();
			}
		}
	}

	// =====================================================================================================================================

	@Inject(method = "runTickKeyboard", at = @At(value = "HEAD"))
	public void injectRunTickKeyboard(CallbackInfo ci) {
		TASmodClient.virtual.updateCurrentKeyboard();
	}

	// =====================================================================================================================================

	@Redirect(method = "runTickKeyboard", at = @At(value = "INVOKE", target = "Lorg/lwjgl/input/Keyboard;next()Z", remap = false))
	public boolean redirectKeyboardNext() {
		return TASmodClient.virtual.nextKeyboardEvent();
	}

	// =====================================================================================================================================

	@Redirect(method = "runTickKeyboard", at = @At(value = "INVOKE", target = "Lorg/lwjgl/input/Keyboard;getEventKey()I", remap = false))
	public int redirectKeyboardGetEventKey() {
		return TASmodClient.virtual.getEventKeyboardKey();
	}

	// =====================================================================================================================================

	@Redirect(method = "runTickKeyboard", at = @At(value = "INVOKE", target = "Lorg/lwjgl/input/Keyboard;getEventCharacter()C", remap = false))
	public char redirectKeyboardGetEventCharacter() {
		return TASmodClient.virtual.getEventKeyboardCharacter();
	}

	// =====================================================================================================================================

	@Redirect(method = "runTickKeyboard", at = @At(value = "INVOKE", target = "Lorg/lwjgl/input/Keyboard;isKeyDown(I)Z", remap = false))
	public boolean redirectIsKeyDown(int keyCode) {
		return TASmodClient.virtual.isKeyDown(keyCode);
	}

	// =====================================================================================================================================

	@Redirect(method = "runTickKeyboard", at = @At(value = "INVOKE", target = "Lorg/lwjgl/input/Keyboard;getEventKeyState()Z", remap = false))
	public boolean redirectGetEventState() {
		return TASmodClient.virtual.getEventKeyboardState();
	}

	// =====================================================================================================================================

	@Inject(method = "runTickMouse", at = @At(value = "HEAD"))
	public void injectRunTickMouse(CallbackInfo ci) {
		TASmodClient.virtual.updateCurrentMouseEvents();
	}

	// =====================================================================================================================================

	@Redirect(method = "runTickMouse", at = @At(value = "INVOKE", target = "Lorg/lwjgl/input/Mouse;next()Z", remap = false))
	public boolean redirectMouseNext() {
		return TASmodClient.virtual.nextMouseEvent();
	}

	// =====================================================================================================================================

	@Redirect(method = "runTickMouse", at = @At(value = "INVOKE", target = "Lorg/lwjgl/input/Mouse;getEventButton()I", remap = false))
	public int redirectMouseGetEventButton() {
		
//		if(!VirtualKeybindings.isKeyCodeAlwaysBlocked(ClientProxy.virtual.getEventMouseKey()-100)) {
//			TASmod.ktrngHandler.nextPlayerInput(); // Advance ktrng seed on player input
//		}
		return TASmodClient.virtual.getEventMouseKey() + 100;
	}

	// =====================================================================================================================================

	@Redirect(method = "runTickMouse", at = @At(value = "INVOKE", target = "Lorg/lwjgl/input/Mouse;getEventButtonState()Z", remap = false))
	public boolean redirectGetEventButtonState() {
		return TASmodClient.virtual.getEventMouseState();
	}

	// =====================================================================================================================================

	@ModifyConstant(method = "runTickMouse", constant = @Constant(longValue = 200L))
	public long fixMouseWheel(long twohundredLong) {
		return (long) Math.max(4000F / TASmodClient.tickratechanger.ticksPerSecond, 200L);
	}

	// =====================================================================================================================================

	@Redirect(method = "runTickMouse", at = @At(value = "INVOKE", target = "Lorg/lwjgl/input/Mouse;getEventDWheel()I", remap = false))
	public int redirectGetEventDWheel() {
		return TASmodClient.virtual.getEventMouseScrollWheel();
	}

	// =====================================================================================================================================

	@Redirect(method = "dispatchKeypresses", at = @At(value = "INVOKE", target = "Lorg/lwjgl/input/Keyboard;getEventKey()I", remap = false))
	public int redirectGetEventKeyDPK() {
		return TASmodClient.virtual.getEventKeyboardKey();
	}

	// =====================================================================================================================================

	@Redirect(method = "dispatchKeypresses", at = @At(value = "INVOKE", target = "Lorg/lwjgl/input/Keyboard;getEventCharacter()C", remap = false))
	public char redirectGetEventCharacterDPK() {
		return TASmodClient.virtual.getEventKeyboardCharacter();
	}

	// =====================================================================================================================================

	@Redirect(method = "dispatchKeypresses", at = @At(value = "INVOKE", target = "Lorg/lwjgl/input/Keyboard;getEventKeyState()Z", remap = false))
	public boolean redirectGetEventKeyStateDPK() {
		return TASmodClient.virtual.getEventKeyboardState();
	}

	// =====================================================================================================================================
	
	@Inject(method = "runTick", at = @At(value = "RETURN"))
	public void injectRunTickReturn(CallbackInfo ci) {
		TASmodClient.virtual.getContainer().nextTick();
	}
}
