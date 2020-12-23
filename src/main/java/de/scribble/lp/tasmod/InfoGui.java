package de.scribble.lp.tasmod;

import java.awt.MouseInfo;
import java.awt.Toolkit;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;

import de.scribble.lp.tasmod.ticksync.TickSync;
import de.scribble.lp.tasmod.util.PointerNormalizer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * The InfoGui returns!
 * @author ScribbleLP
 *
 */
public class InfoGui extends Gui{
	Minecraft mc= Minecraft.getMinecraft();
    @SubscribeEvent
    public void drawStuff(RenderGameOverlayEvent.Post event) {
        if (event.isCancelable() || event.getType() != ElementType.HOTBAR) {
            return;
        }
        ScaledResolution scaled = new ScaledResolution(mc);
        int width = scaled.getScaledWidth();
        int height = scaled.getScaledHeight();
        if (!(mc.gameSettings.showDebugInfo)) {
//            if (Infoenabled) {
                new Gui().drawCenteredString(mc.fontRenderer, (mc.player.posX - 0.5) + " " + Math.round((mc.player.posY)) + " " + (mc.player.posZ - 0.5), 130, 10, 0xFFFFFF);    //Coordinates
                new Gui().drawString(mc.fontRenderer, "Pitch: " + mc.player.rotationPitch, 16, 20, 0xFFFFFF);                //Show the current Pitch
                new Gui().drawString(mc.fontRenderer, "Yaw: " + mc.player.rotationYaw, 22, 30, 0xFFFFFF);        //Show the current Yaw (This comes from the modversion for 1.7.10 since 1.7 has just SOUTH as a yaw in F3)

                new Gui().drawString(mc.fontRenderer, Mouse.getEventX() + " " + Mouse.getEventY(), 22, 40, 0xFFFFFF); //Current Pointer location
                
                new Gui().drawString(mc.fontRenderer, "S: "+TickSync.getServertickcounter(), 22, 70, 0xFFFFFF); //Current Pointer location
                new Gui().drawString(mc.fontRenderer, "C: "+TickSync.getClienttickcounter(), 22, 80, 0xFFFFFF); //Current Pointer location
                new Gui().drawCenteredString(mc.fontRenderer, "TASmod is still in development! Major issues may arise!", width/2, height-50, 0xFF8400); //Current Pointer location
                
//            }
//            if (Strokesenabled) {
//                drawKeyStrokes(height, width);
//            }
            //Draw the Tickcounter. Value depends if playback or a recording is playing.
//            if (!TAS.doneRecording()) {
//                new Gui().drawCenteredString(mc.fontRenderer, Integer.toString(TAS.recorder.recordstep + 1), 30, height - 24, 0xFFFFFF);
//            } else if (TAS.tasPlayer != null && !TAS.tasPlayer.donePlaying) {
//                new Gui().drawCenteredString(mc.fontRenderer, Integer.toString(TAS.tasPlayer.step + 1), 30, height - 24, 0xFFFFFF);
//            }
        }
    }
}
