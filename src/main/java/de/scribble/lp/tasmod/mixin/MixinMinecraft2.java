package de.scribble.lp.tasmod.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import de.scribble.lp.tasmod.ModLoader;
import de.scribble.lp.tasmod.duck.SubtickDuck;
import de.scribble.lp.tasmod.tickratechanger.TickrateChangerClient;
import de.scribble.lp.tasmod.ticksync.TickSync;
import de.scribble.lp.tasmod.virtual.VirtualKeybindings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.util.Timer;

@Mixin(Minecraft.class)
public abstract class MixinMinecraft2 {
	
	//=====================================================================================================================================
	
	@Inject(method="init",at = @At(value = "RETURN"))
	public void injectInit(CallbackInfo ci) {
		ModLoader.logger.debug("Initialising stuff for TASmod");
		new TickrateChangerClient();
		new TickSync();
	}
	
	//=====================================================================================================================================
	
	@Inject(method = "runGameLoop", at = @At(value = "HEAD"))
	public void injectRunGameLoop(CallbackInfo ci) {
		//TASmod
        VirtualKeybindings.increaseCooldowntimer();
        TickrateChangerClient.bypass();
	}
	
	//=====================================================================================================================================
	
	@Shadow
	private EntityRenderer entityRenderer;
	@Shadow
	private boolean isGamePaused;
	@Shadow
	private float renderPartialTicksPaused;
	@Shadow
	private Timer timer;

	@Redirect(method = "runGameLoop", at=@At(value = "INVOKE", target = "runTick"))
	public void redirectRunTick(Minecraft mc) {
		for (int j2 = 0; j2 < TickSync.getTickAmount((Minecraft)(Object)this); j2++) {
    		if(TickrateChangerClient.TICKS_PER_SECOND!=0) {
				((SubtickDuck)this.entityRenderer).runSubtick(this.isGamePaused ? this.renderPartialTicksPaused : this.timer.renderPartialTicks);
			}
			this.runTick();
		}
		if(TickrateChangerClient.ADVANCE_TICK) {
			TickrateChangerClient.ADVANCE_TICK=false;
			TickrateChangerClient.changeClientTickrate(0F);
		}
	}

	public abstract void runTick();
	
	//=====================================================================================================================================
	
	@Inject(method = "runTickKeyboard", at = @At(value = "HEAD"))
	public void injectRunTickKeyboard(CallbackInfo ci) {
		//TODO Update nextKeyboard and generate VirtualKeyEvents
	}
	
	//=====================================================================================================================================
	
	@Redirect(method = "runTickKeyboard", at = @At(value = "INVOKE", target = "Lorg/lwjgl/input/Keyboard;next()Z"))
	public boolean redirectKeyboardNext() {
		return false; //TODO VirtualKeyEvents.next()
	}
	
	//=====================================================================================================================================
	
	@Redirect(method = "getEventKey", at = @At(value = "INVOKE", target = "Lorg/lwjgl/input/Keyboard;getEventKey()I"))
	public int redirectKeyboardGetEventKey() {
		return 0; // TODO VirtualKeyEvents.getEventKey()
	}
	
	//=====================================================================================================================================
	
	@Redirect(method = "getEventKey", at = @At(value = "INVOKE", target = "Lorg/lwjgl/input/Keyboard;getEventCharacter()C"))
	public char redirectKeyboardGetEventCharacter() {
		return Character.MIN_VALUE; // TODO VirtualKeyEvents.getEventCharacter()
	}
	
	//=====================================================================================================================================
	
	@Redirect(method = "getEventKey", at = @At(value = "INVOKE", target = "Lorg/lwjgl/input/Keyboard;isKeyDown(I)Z"))
	public boolean redirectIsKeyDown(int keyCode) {
		return false; // TODO VirtualInput2 isKeyDown();
	}
	
	//=====================================================================================================================================
}
