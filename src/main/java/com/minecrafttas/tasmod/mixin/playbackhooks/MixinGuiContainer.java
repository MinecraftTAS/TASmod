package com.minecrafttas.tasmod.mixin.playbackhooks;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.minecrafttas.tasmod.TASmodClient;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.inventory.GuiContainer;

@Mixin(GuiContainer.class)
public class MixinGuiContainer {
	@Redirect(method = "mouseClicked", at = @At(value = "INVOKE", target = "Lorg/lwjgl/input/Keyboard;isKeyDown(I)Z", ordinal = 0, remap = false))
	private boolean redirectIsKeyDown(int i) {
		return TASmodClient.virtual.isKeyDown(i);
	}

	@Redirect(method = "mouseReleased", at = @At(value = "INVOKE", target = "Lorg/lwjgl/input/Keyboard;isKeyDown(I)Z", ordinal = 0, remap = false))
	private boolean redirectIsKeyDown2(int i) {
		return TASmodClient.virtual.isKeyDown(i);
	}

	@Redirect(method = "keyTyped", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/entity/EntityPlayerSP;closeScreen()V"))
	public void redirectCloseScreen(EntityPlayerSP player) {
		Minecraft mc = Minecraft.getMinecraft();
		if(TASmodClient.virtual.isKeyDown(mc.gameSettings.keyBindInventory.getKeyCode()) && ((GuiContainer)(Object)this).isFocused()) {
			return;
		}
		player.closeScreen();
	}
}
