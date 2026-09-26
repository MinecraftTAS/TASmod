package com.minecrafttas.tasmod.mctcommon.mixin;

import java.util.List;
import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.minecrafttas.tasmod.mctcommon.LanguageManager;

import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.locale.Language;

@Mixin(Language.class)
public class MixinLocale {

	@Shadow
	private Map<String, String> properties;

	@Inject(method = "loadLocaleDataFiles", at = @At("RETURN"))
	private void inject_loadLocalDataFiles(ResourceManager resourceManager, List<String> list, CallbackInfo ci) {
		LanguageManager.onResourceManagerReload(properties, resourceManager, list);
	}
}
