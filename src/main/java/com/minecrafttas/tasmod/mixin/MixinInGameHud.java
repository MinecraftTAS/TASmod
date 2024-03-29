package com.minecrafttas.tasmod.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

@Mixin(GuiIngame.class)
public abstract class MixinInGameHud {
	
	ResourceLocation potion = new ResourceLocation("tasmod:textures/potion.png");
	
	/**
	 * Renders the potion into the gui
	 * @param ci
	 */
	@Inject(method="renderExpBar", at=@At(value="HEAD"), remap = false)
	public void mixinRenderExperienceBar(CallbackInfo ci) {
		ScaledResolution scaledresolution = new ScaledResolution(Minecraft.getMinecraft());
		Minecraft.getMinecraft().getTextureManager().bindTexture(potion);
		int m = (scaledresolution.getScaledWidth() / 2)-6;
        int n = scaledresolution.getScaledHeight() - 31 - 19;
		int skale=20;
		GlStateManager.enableBlend();
		GlStateManager.enableAlpha();
		GlStateManager.color(1, 1, 1, 0.3F);
		Gui.drawModalRectWithCustomSizedTexture(m-3, n, 0F, 0F, skale, skale, skale, skale);
		GlStateManager.disableBlend();
		GlStateManager.disableAlpha();
	}
}
