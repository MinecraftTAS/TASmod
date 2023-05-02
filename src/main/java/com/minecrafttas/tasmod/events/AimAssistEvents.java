package com.minecrafttas.tasmod.events;

import com.minecrafttas.tasmod.ClientProxy;
import com.minecrafttas.tasmod.tickratechanger.TickrateChangerClient;
import com.minecrafttas.tasmod.util.PlayerPositionCalculator;

import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class AimAssistEvents {
	
	@SubscribeEvent
	public void ingameOverlay(RenderGameOverlayEvent.Post ev) {
		Minecraft mc = Minecraft.getMinecraft();
        if (ev.isCancelable() || ev.getType() != ElementType.HOTBAR) {
            return;
        }
		if (mc.world == null) return;
		ClientProxy.hud.drawHud();
	}
	
	@SubscribeEvent
	public void blockOverlay(DrawBlockHighlightEvent ev) {
		Minecraft mc = Minecraft.getMinecraft();
		
		if (mc.world == null) return;
		if (TickrateChangerClient.ticksPerSecond == 0) {
			PlayerPositionCalculator.calculateNextPosition(mc, mc.player);
			ev.setCanceled(true);
		}
	}
	
}
