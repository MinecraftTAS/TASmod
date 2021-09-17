package de.scribble.lp.tasmod.mixin.shields;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import de.scribble.lp.tasmod.ClientProxy;
import de.scribble.lp.tasmod.shield.ShieldDownloader;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

@Mixin(TileEntityItemStackRenderer.class)
public class MixinTileEntityItemStackRenderer {
	
	@Redirect(method = "Lnet/minecraft/client/renderer/tileentity/TileEntityItemStackRenderer;renderByItem(Lnet/minecraft/item/ItemStack;F)V", at = @At(value="INVOKE", target = "Lnet/minecraft/client/renderer/texture/TextureManager;bindTexture(Lnet/minecraft/util/ResourceLocation;)V", ordinal = 1))
	public void inject_renderByItem(TextureManager manager, ResourceLocation original, ItemStack shield, float partialTicks) {
		EntityLivingBase entity=ShieldDownloader.renderedEntity;
		if(ShieldDownloader.renderedEntity!=null&&shield!=null) {
			if((entity.getHeldItemMainhand()!=null||entity.getHeldItemOffhand()!=null)&&entity.getHeldItemMainhand().equals(shield)&&!entity.getName().matches("Player[0-9]{1,4}")) {
				manager.bindTexture(ClientProxy.shieldDownloader.getResourceLocation(entity));
				return;
			}
		}
	}
	
}
