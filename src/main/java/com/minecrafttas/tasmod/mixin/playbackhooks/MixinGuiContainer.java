package com.minecrafttas.tasmod.mixin.playbackhooks;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.minecrafttas.tasmod.TASmodClient;
import com.minecrafttas.tasmod.virtual.VirtualKey;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.Screen;

@Mixin(AbstractContainerScreen.class)
public class MixinGuiContainer {

	/**
	 * Redirects the check for {@link VirtualKey#LSHIFT} and {@link VirtualKey#RSHIFT} in mouseClicked
	 * @param i The keycode to check for
	 * @return If the keycode is down
	 */
	@Redirect(method = "mouseClicked", at = @At(value = "INVOKE", target = "Lorg/lwjgl/input/Keyboard;isKeyDown(I)Z", ordinal = 0, remap = false))
	private boolean redirectIsKeyDown(int i) {
		return TASmodClient.virtual.isKeyDown(i);
	}

	/**
	 * Redirects the check for {@link VirtualKey#LSHIFT} and {@link VirtualKey#RSHIFT} in mouseReleased
	 * @param i The keycode to check for
	 * @return If the keycode is down
	 */
	@Redirect(method = "mouseReleased", at = @At(value = "INVOKE", target = "Lorg/lwjgl/input/Keyboard;isKeyDown(I)Z", ordinal = 0, remap = false))
	private boolean redirectIsKeyDown2(int i) {
		return TASmodClient.virtual.isKeyDown(i);
	}

	/**
	 * Fixes <a href="https://github.com/MinecraftTAS/TASmod/issues/67">#67</a>
	 * @param player The current player
	 */
	@Redirect(method = "keyTyped", at = @At(value = "INVOKE", target = "Lnet.minecraft.client.player.LocalPlayer;closeScreen()V"))
	public void redirectCloseScreen(LocalPlayer player) {
		Minecraft mc = Minecraft.getInstance();
		if (TASmodClient.virtual.isKeyDown(mc.options.keyInventory.getDefaultKey().getValue()) && ((AbstractContainerScreen) (Object) this).isFocused()) {
			return;
		}
		// mc.setScreen(null); // API changed in 26.3
	}
}

