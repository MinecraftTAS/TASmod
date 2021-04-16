package de.pfannekuchen.tasmod.events;

import de.pfannekuchen.infogui.gui.SettingsGui;
import de.pfannekuchen.tasmod.utils.PlayerPositionCalculator;
import de.scribble.lp.tasmod.ClientProxy;
import de.scribble.lp.tasmod.tickratechanger.TickrateChangerClient;
import de.scribble.lp.tasmod.virtual.VirtualKeybindings;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class AimAssistEvents {
	
	public static boolean showNewPos;
	
	@SubscribeEvent
	public void ingameOverlay(RenderGameOverlayEvent.Post ev) {
		Minecraft mc = Minecraft.getMinecraft();
        if (ev.isCancelable() || ev.getType() != ElementType.HOTBAR) {
            return;
        }
		if (mc.world == null) return;
		SettingsGui.drawOverlay();
		if (AimAssistEvents.showNewPos) {
//			mc.fontRenderer.drawStringWithShadow("X (predicted): " + PlayerPositionCalculator.xNew, 16, 20, 0x00AAAA); //Show the next Location
//			mc.fontRenderer.drawStringWithShadow("Y (predicted): " + PlayerPositionCalculator.yNew, 16, 30, 0x00AAAA); 
//			mc.fontRenderer.drawStringWithShadow("Z (predicted): " + PlayerPositionCalculator.zNew, 16, 40, 0x00AAAA); 
		} else {
//			mc.fontRenderer.drawStringWithShadow("X: " + mc.player.posX, 16, 20, 0xFFFFFF); //Show the current Location
//			mc.fontRenderer.drawStringWithShadow("Y: " + mc.player.posY, 16, 30, 0xFFFFFF); 
//			mc.fontRenderer.drawStringWithShadow("Z: " + mc.player.posZ, 16, 40, 0xFFFFFF); 
		}
	}
	
	@SubscribeEvent
	public void blockOverlay(DrawBlockHighlightEvent ev) {
		Minecraft mc = Minecraft.getMinecraft();
		
		// Toggle
		if (VirtualKeybindings.isKeyDownExceptTextfield(ClientProxy.showNextLocation)) {
			showNewPos = !showNewPos;
		}
		
		if (mc.world == null) return;
		if (TickrateChangerClient.TICKS_PER_SECOND == 0) {
			PlayerPositionCalculator.calculateNextPosition(mc, mc.player);
			ev.setCanceled(true);
		}
	}
	
}
