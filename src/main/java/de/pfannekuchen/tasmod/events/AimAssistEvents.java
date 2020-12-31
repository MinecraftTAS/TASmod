package de.pfannekuchen.tasmod.events;

import de.pfannekuchen.tasmod.utils.PlayerPositionCalculator;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class AimAssistEvents {
	
	@SubscribeEvent
	public void blockOverlay(DrawBlockHighlightEvent ev) {
		ev.setCanceled(true); // SCRIBBLE WHAT SHOULD I DOAAAAAAAAAAAAAAAAA
        Minecraft mc = Minecraft.getMinecraft();
		if (mc.world != null) PlayerPositionCalculator.calculateNextPosition(mc, mc.player);
	}
	
}
