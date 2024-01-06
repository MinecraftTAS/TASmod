package com.minecrafttas.killtherng.mixin;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.EntityLivingBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;

@Mixin(RenderLivingBase.class)
public abstract class MixinRender extends Render {
    protected MixinRender(RenderManager renderManager) {
        super(renderManager);
    }

    @Inject(method = "renderName", at = @At(value = "HEAD"))
    public void inject_renderName(EntityLivingBase entity, double d, double e, double f, CallbackInfo ci){
        long seed = getSeed(entity.rand);
        GlStateManager.alphaFunc(516, 0.1F);
        this.renderEntityName(entity, d, e+0.23D, f, Long.toString(seed), 64);
    }

    private long getSeed(Random rand) {
        long in = rand.nextLong();
        long seed = (((7847617*((24667315*(in >>> 32) + 18218081*(in & 0xffffffffL) + 67552711) >> 32) - 18218081*((-4824621*(in >>> 32) + 7847617*(in & 0xffffffffL) + 7847617) >> 32)) - 11) * 246154705703781L) & 0xffffffffffffL;
        seed = seed ^ 0x5deece66dL;
        rand.setSeed(seed);
        return seed;
    }
}
