package com.minecrafttas.tasmod.mixin.playbackhooks;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.minecrafttas.tasmod.TASmodClient;
import com.minecrafttas.tasmod.virtual.VirtualKey2;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.inventory.GuiContainer;

@Mixin(GuiContainer.class)
public class MixinGuiContainer {
	
	/**
	 * Redirects the check for {@link VirtualKey2#LSHIFT} and {@link VirtualKey2#RSHIFT} in mouseClicked
	 * @param i The keycode to check for
	 * @return If the keycode is down
	 */
	@Redirect(method = "mouseClicked", at = @At(value = "INVOKE", target = "Lorg/lwjgl/input/Keyboard;isKeyDown(I)Z", ordinal = 0, remap = false))
	private boolean redirectIsKeyDown(int i) {
		return TASmodClient.virtual.isKeyDown(i);
	}

	/**
	 * Redirects the check for {@link VirtualKey2#LSHIFT} and {@link VirtualKey2#RSHIFT} in mouseReleased
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
	@Redirect(method = "keyTyped", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/entity/EntityPlayerSP;closeScreen()V"))
	public void redirectCloseScreen(EntityPlayerSP player) {
		Minecraft mc = Minecraft.getMinecraft();
		if(TASmodClient.virtual.isKeyDown(mc.gameSettings.keyBindInventory.getKeyCode()) && ((GuiContainer)(Object)this).isFocused()) {
			return;
		}
		player.closeScreen();
	}
}
