package de.scribble.lp.tasmod.mixin.events;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import de.scribble.lp.tasmod.events.OpenGuiEvents;
import net.minecraft.client.gui.GuiIngameMenu;

@Mixin(GuiIngameMenu.class)
public class MixinGuiIngameMenu {
	@Inject(method = "initGui", at = @At("RETURN"))
	public void inject_initGui(CallbackInfo ci) {
		OpenGuiEvents.openGuiIngameMenu((GuiIngameMenu) (Object) this);
	}
}
