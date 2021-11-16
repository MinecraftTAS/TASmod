package de.scribble.lp.tasmod.mixin.events;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import de.scribble.lp.tasmod.events.OpenGuiEvent;
import net.minecraft.client.gui.GuiMainMenu;

@Mixin(GuiMainMenu.class)
public class MixinGuiMainMenu {
	@Inject(method = "<init>", at = @At("RETURN"))
	public void inject_Init(CallbackInfo ci) {
		OpenGuiEvent.openGuiMainMenu((GuiMainMenu)(Object)this);
	}
}
