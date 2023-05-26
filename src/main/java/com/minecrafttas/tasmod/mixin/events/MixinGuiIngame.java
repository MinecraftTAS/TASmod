package com.minecrafttas.tasmod.mixin.events;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.minecrafttas.tasmod.events.EventClient.EventDrawHotbar;

import net.minecraft.client.gui.GuiIngame;

@Mixin(GuiIngame.class)
public class MixinGuiIngame {
	
	@Inject(method = "renderHotbar", at = @At("HEAD"))
	public void inject_renderHotbar(CallbackInfo ci) {
		EventDrawHotbar.fireOnDrawHotbar();
	}
}
