package com.minecrafttas.tasmod.mixin.shields;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.minecrafttas.tasmod.TASmodClient;
import com.minecrafttas.tasmod.util.ShieldDownloader;

import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

@Mixin(TileEntityItemStackRenderer.class)
public class MixinTileEntityItemStackRenderer {

	@Redirect(method = "Lnet/minecraft/client/renderer/tileentity/TileEntityItemStackRenderer;renderByItem(Lnet/minecraft/item/ItemStack;F)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/texture/TextureManager;bindTexture(Lnet/minecraft/util/ResourceLocation;)V", ordinal = 1))
	public void inject_renderByItem(TextureManager manager, ResourceLocation original, ItemStack shield, float partialTicks) {
		EntityLivingBase entity = ShieldDownloader.renderedEntity;
		if (entity != null && shield != null) {
			// Dev Texture
			if (entity.getHeldItemMainhand() != null && entity.getHeldItemMainhand().equals(shield)) {
				if (!entity.getName().matches("Player[0-9]{1,4}")) {
					manager.bindTexture(TASmodClient.shieldDownloader.getResourceLocation(entity));
				}
				return;
			} else if (entity.getHeldItemOffhand() != null && entity.getHeldItemOffhand().equals(shield)) {
				if (!entity.getName().matches("Player[0-9]{1,4}")) {
					manager.bindTexture(TASmodClient.shieldDownloader.getResourceLocation(entity));
				}
				return;
			}
		}
		manager.bindTexture(original);
	}

}
