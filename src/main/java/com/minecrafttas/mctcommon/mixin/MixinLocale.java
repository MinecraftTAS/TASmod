package com.minecrafttas.common.mixin;

import com.minecrafttas.common.LanguageManager;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.Locale;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Map;

@Mixin(Locale.class)
public class MixinLocale {

    @Shadow
    private Map<String, String> properties;
    @Inject(method="loadLocaleDataFiles", at = @At("RETURN"))
    private void inject_loadLocalDataFiles(IResourceManager iResourceManager, List<String> list, CallbackInfo ci){
        properties.putAll(LanguageManager.onResourceManagerReload(iResourceManager, list));
    }
}
