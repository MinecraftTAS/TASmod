package com.minecrafttas.tasmod.mixin.tickrate;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import com.minecrafttas.tasmod.TASmodClient;

import net.minecraft.client.Minecraft;
import net.minecraft.util.Util;

/**
 * This mixin tries to make the animation of the advancement toasts dependent on the tickrate while keeping the code as vanilla as possible<br>
 * <br>
 * While I spent a long amount of time watching this code, I still don't quite fully understand the math behind this...<br>
 * <br>
 * Here's what I could find out: Toasts have 2 different states represented in visibility in the ToastInstance.<br>
 * And if it's set to SHOW the fly in animation and sound will play, the same goes with HIDE where it flies out after a certain amount of time<br>
 * After a lot of trial and error I found out that animationTimer, which was originally "i", is the way to go...<br>
 * <br>
 * So just as ItemRenderer and SubtitleOverlay, things are done with an offset for tickrate 0 and simple multiplication<br>
 * Also I used a copy of the vanilla ToastInstance-class to make it work for every subtitle on screen... If you seek to make it work for only 1, at the end is a commented code that shows you how to use @ModyfyVarable<br>
 * <br>
 * There is one compromise I had to make... When you change the tickrate while a toast is showing, it will stay at the old tickrate until it's done...<br>
 * Maybe I still fix this and get into this mess once more, but for now this will do and don't make the subtitles stuck in a loop until you change to the old tickrate<br>
 * Am I doing this right with commenting code? I hope so...<br>
 * <br>
 * Update 02.03.21:<br>
 * Well, it's been roughly a year since I have touched this code and I am finally back with updating this. And while I am at it, I removed that compromise mentioned earlier.<br>
 * 
 * Update 26.03.26 Unease <br> 
 * Reusing 5 year old code for legacy... how fitting
 * 
 * @author Scribble
 * @author Unease
 */
@Mixin(targets = "net/minecraft/client/gui/toasts/GuiToast$ToastInstance")
public abstract class MixinGuiToast {

	/**
	 * Vanilla, current time in ms when the animationBegan. The delta between animationTimer and animation time shows the progress
	 */
	@Shadow
	private long animationTime;
	/**
	 * Vanilla, the time the animation is visible. Used in the toast instances (e.g. AdvancementToast) to time their animation. Also used to set the visibility to HIDE and make the toast go away
	 */
	@Shadow
	private long visibleTime;

	/**
	 * When entering tickrate 0, store is the (ms) time when tickrate 0 was activated. This time replaces the "animationTimer" during tickrate 0
	 */
	private long store = 0L;
	/**
	 * Makes sure the code runs only once when switching between tickrate 0 and not tickrate 0... I have yet to find a more elegant solution...
	 */
	private boolean once = false;
	/**
	 * The offset of the ms time when using tickrate 0. This is used to "resume" the animation without any jumps, after exiting tickrate 0
	 */
	private long offset = 0L;
	/**
	 * When changing the tickrate while a toast is on screen, the {@link #animationTime} is not correct. The correct animationTime can be optained by subtracting the current time from this delta<br>
	 */
	private long animationDelta = 0L;
	/**
	 * When changing the tickrate while a toast is on screen, the {@link #visibleTime} is not correct. The correct visibleTime can be optained by subtracting the current time from this delta<br>
	 */
	private long visibleDelta = 0L;
	/**
	 * Used to detect a change in the tickrate and to run the code on.
	 */
	private float ticksave = TASmodClient.tickratechanger.ticksPerSecond;

	@ModifyVariable(method = "render(II)Z", at = @At(value = "STORE", ordinal = 0))
	public long modifyAnimationTime(long animationTimer) {
		//===========TICKRATE OTHER THAN 0===========
		if (TASmodClient.tickratechanger.ticksPerSecond != 0) {
			if (once) {
				once = false;
				offset = Util.getMillis() - store;
			}

			animationTimer = (long) ((Util.getMillis() - offset) * (TASmodClient.tickratechanger.ticksPerSecond / 20));

			if (ticksave != TASmodClient.tickratechanger.ticksPerSecond) {
				ticksave = TASmodClient.tickratechanger.ticksPerSecond;
				animationTime = animationTimer - animationDelta;
				visibleTime = animationTimer - visibleDelta;
			}
			animationDelta = animationTimer - animationTime;
			visibleDelta = animationTimer - visibleTime;
			//===========TICKRATE 0===========
		} else {
			if (!once) {
				once = true;
				store = (long) ((Util.getMillis() - offset));
			}
			animationTimer = (long) (store * (TASmodClient.tickratechanger.tickrateSaved / 20));
		}
		return animationTimer;
	}
}

