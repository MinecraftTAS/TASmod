package com.minecrafttas.tasmod.mixin.clientcommands;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.minecrafttas.tasmod.registries.TASmodAPIRegistry;

import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.CommandSuggestions;

@Mixin(CommandSuggestions.class)
public abstract class MixinTabCompleter {

	@Shadow
	@Final
	private EditBox textField;
	@Shadow
	private boolean requestedCompletions;

	@Unique
	private String[] clientCompletions = null;

	@Inject(method = "requestCompletions", at = @At("HEAD"), cancellable = true)
	public void inject_requestTabCompletions(String currentCommand, CallbackInfo ci) {
		if (currentCommand.length() >= 1) {
			clientCompletions = TASmodAPIRegistry.CLIENT_COMMANDS.runTabCompletions(currentCommand);
			if (clientCompletions == null) {
			}
			if (clientCompletions != null) {
				requestedCompletions = true;
				ci.cancel();
			}
		}
	}

	@Inject(method = "complete", at = @At("RETURN"))
	public void inject_tabComplete(CallbackInfo ci) {
		if (clientCompletions != null) {
			this.setCompletions(clientCompletions);
			clientCompletions = null;
		}
	}

	@Shadow
	protected abstract void setCompletions(String... strings);
}

