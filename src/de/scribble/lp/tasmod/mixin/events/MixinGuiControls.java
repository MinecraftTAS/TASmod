package de.scribble.lp.tasmod.mixin.events;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import de.scribble.lp.tasmod.events.OpenGuiEvents;
import net.minecraft.client.gui.GuiControls;
import net.minecraft.client.gui.GuiScreen;

@Mixin(GuiControls.class)
public class MixinGuiControls extends GuiScreen{
	
	@Inject(method = "initGui", at = @At("HEAD"))
	public void inject_initGui(CallbackInfo ci) {
		OpenGuiEvents.openGuiControls((GuiControls)(Object)this);
	}
	
	@Override
	public void onGuiClosed() {
		OpenGuiEvents.closeGuiControls((GuiControls)(Object)this);
	}
}
