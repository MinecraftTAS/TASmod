package de.scribble.lp.tasmod.custom;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;

import de.scribble.lp.tasmod.playback.InputPlayback;
import de.scribble.lp.tasmod.recording.InputRecorder;
import de.scribble.lp.tasmod.virtual.VirtualMouseAndKeyboard;
import net.minecraft.client.Minecraft;

public class CustomEntityRenderer {
	public void runSubtick(float partialTicks, Minecraft mc, float smoothCamYaw, float smoothCamPitch, float smoothCamPartialTicks, float smoothCamFilterX, float smoothCamFilterY) {
    	boolean flag=Display.isActive();
        if (flag && Minecraft.IS_RUNNING_ON_MAC && mc.inGameHasFocus && !Mouse.isInsideWindow())
        {
            Mouse.setGrabbed(false);
            Mouse.setCursorPosition(Display.getWidth() / 2, Display.getHeight() / 2 - 20);
            Mouse.setGrabbed(true);
        }

        if (mc.inGameHasFocus && flag)
        {
            mc.mouseHelper.mouseXYChange();
            mc.getTutorial().handleMouse(mc.mouseHelper);
            float f = mc.gameSettings.mouseSensitivity * 0.6F + 0.2F;
            float f1 = f * f * f * 8.0F;
            float f2 = (float)mc.mouseHelper.deltaX * f1;
            float f3 = (float)mc.mouseHelper.deltaY * f1;
            int i = 1;

            if (mc.gameSettings.invertMouse)
            {
                i = -1;
            }

            if (mc.gameSettings.smoothCamera)
            {
                smoothCamYaw += f2;
                smoothCamPitch += f3;
                float f4 = partialTicks - smoothCamPartialTicks;
                smoothCamPartialTicks = partialTicks;
                f2 = smoothCamFilterX * f4;
                f3 = smoothCamFilterY * f4;
                mc.player.turn(f2, f3 * (float)i);
            }
            else
            {
                smoothCamYaw = 0.0F;
                smoothCamPitch = 0.0F;
                mc.player.turn(f2, f3 * (float)i);
            }
            InputPlayback.nextPlaybackSubtick();
            VirtualMouseAndKeyboard.fillSubtick(VirtualMouseAndKeyboard.getTimeSinceLastTick(), mc.player.rotationPitch,  mc.player.rotationYaw);
            InputRecorder.recordSubTick();
            VirtualMouseAndKeyboard.fillSubtickWithPlayback();
            mc.player.rotationPitch=VirtualMouseAndKeyboard.getSubtickPitch();
            mc.player.rotationYaw=VirtualMouseAndKeyboard.getSubtickYaw();
        }
    }
}
