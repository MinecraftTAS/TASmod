package de.scribble.lp.tasmod.mixin;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import de.pfannekuchen.tasmod.events.CameraInterpolationEvents;
import de.scribble.lp.tasmod.duck.SubtickDuck;
import de.scribble.lp.tasmod.playback.InputPlayback;
import de.scribble.lp.tasmod.tickratechanger.TickrateChangerClient;
import de.scribble.lp.tasmod.virtual.VirtualInput;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.shader.ShaderGroup;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.util.ReportedException;
import net.minecraft.util.math.MathHelper;

@Mixin(EntityRenderer.class)
public abstract class MixinEntityRenderer implements SubtickDuck{
	@Shadow
	private Minecraft mc;
	@Shadow
	private long prevFrameTime;
	@Shadow
	private float smoothCamYaw;
	@Shadow
	private float smoothCamPitch;
	@Shadow
	private float smoothCamPartialTicks;
	@Shadow
	private float smoothCamFilterX;
	@Shadow
	private float smoothCamFilterY;
	@Shadow
	private static boolean anaglyphEnable;
	@Shadow
	private long timeWorldIcon;
	@Shadow
	private ShaderGroup shaderGroup;
	@Shadow
	private boolean useShader;
	@Shadow
	private long renderEndNanoTime;
	
	public double dX = 0;
	public double dY = 0;
	
	@Inject(method = "updateCameraAndRender", at = @At("HEAD"), cancellable = true)
	public void injectUpdateCameraAndRenderer(float partialTicks, long nanoTime, CallbackInfo ci) {
		
		boolean flag = Display.isActive();

        if (!flag && this.mc.gameSettings.pauseOnLostFocus && (!this.mc.gameSettings.touchscreen || !Mouse.isButtonDown(1)))
        {
            if (Minecraft.getSystemTime() - this.prevFrameTime > 500L)
            {
                this.mc.displayInGameMenu();
            }
        }
        else
        {
            this.prevFrameTime = Minecraft.getSystemTime();
        }

        this.mc.mcProfiler.startSection("mouse");
        
        //Calculate sensitivity
        float f = this.mc.gameSettings.mouseSensitivity * 0.6F + 0.2F;
        float f1 = f * f * f * 8.0F;
        
        //No Gui
        if (this.mc.currentScreen == null) {
            mc.mouseHelper.mouseXYChange();
            dX += mc.mouseHelper.deltaX;
            dY += mc.mouseHelper.deltaY;
        } else {
        //In the gui
        	dX = 0;
        	dY = 0;
        }
        if (InputPlayback.isPlayingback()) {
        	dX = 0;
        	dY = 0;
        } else {
        	if (this.mc.currentScreen == null) {
        		CameraInterpolationEvents.rotationYaw = ((float)((double)CameraInterpolationEvents.rotationYaw + (double)mc.mouseHelper.deltaX * f1 * 0.15D));
            	CameraInterpolationEvents.rotationPitch = (float)((double)CameraInterpolationEvents.rotationPitch - (double)mc.mouseHelper.deltaY * f1 * 0.15D);
            	CameraInterpolationEvents.rotationPitch = MathHelper.clamp(CameraInterpolationEvents.rotationPitch, -90.0F, 90.0F);
        	}
        }
        
        if (flag && Minecraft.IS_RUNNING_ON_MAC && this.mc.inGameHasFocus && !Mouse.isInsideWindow())
        {
            Mouse.setGrabbed(false);
            Mouse.setCursorPosition(Display.getWidth() / 2, Display.getHeight() / 2 - 20);
            Mouse.setGrabbed(true);
        }
        
        if(TickrateChangerClient.TICKS_PER_SECOND==0) {
	        if (this.mc.inGameHasFocus && flag)
	        {
	            this.mc.getTutorial().handleMouse(this.mc.mouseHelper);
	            mc.mouseHelper.mouseXYChange();
	            float f2 = (float)this.mc.mouseHelper.deltaX * f1;
	            float f3 = (float)this.mc.mouseHelper.deltaY * f1;
	            int i = 1;
	
	            if (this.mc.gameSettings.invertMouse)
	            {
	                i = -1;
	            }
	
	            if (this.mc.gameSettings.smoothCamera)
	            {
	                this.smoothCamYaw += f2;
	                this.smoothCamPitch += f3;
	                float f4 = partialTicks - this.smoothCamPartialTicks;
	                this.smoothCamPartialTicks = partialTicks;
	                f2 = this.smoothCamFilterX * f4;
	                f3 = this.smoothCamFilterY * f4;
	                
	                this.mc.player.turn(f2, f3 * (float)i);
	            }
	            else
	            {
	                this.smoothCamYaw = 0.0F;
	                this.smoothCamPitch = 0.0F;
	                this.mc.player.turn(f2, f3 * (float)i);
	            }
	        }
        }
        this.mc.mcProfiler.endSection();

        if (!this.mc.skipRenderWorld)
        {
            anaglyphEnable = this.mc.gameSettings.anaglyph;
            final ScaledResolution scaledresolution = new ScaledResolution(this.mc);
            int i1 = scaledresolution.getScaledWidth();
            int j1 = scaledresolution.getScaledHeight();
            final int k1 = Mouse.getX() * i1 / this.mc.displayWidth;
            final int l1 = j1 - Mouse.getY() * j1 / this.mc.displayHeight - 1;
            int i2 = this.mc.gameSettings.limitFramerate;

            if (this.mc.world != null)
            {
                this.mc.mcProfiler.startSection("level");
                int j = Math.min(Minecraft.getDebugFPS(), i2);
                j = Math.max(j, 60);
                long k = System.nanoTime() - nanoTime;
                long l = Math.max((long)(1000000000 / j / 4) - k, 0L);
                this.renderWorld(partialTicks, System.nanoTime() + l);

                if (this.mc.isSingleplayer() && this.timeWorldIcon < Minecraft.getSystemTime() - 1000L)
                {
                    this.timeWorldIcon = Minecraft.getSystemTime();

                    if (!this.mc.getIntegratedServer().isWorldIconSet())
                    {
                        this.createWorldIcon();
                    }
                }

                if (OpenGlHelper.shadersSupported)
                {
                    this.mc.renderGlobal.renderEntityOutlineFramebuffer();

                    if (this.shaderGroup != null && this.useShader)
                    {
                        GlStateManager.matrixMode(5890);
                        GlStateManager.pushMatrix();
                        GlStateManager.loadIdentity();
                        this.shaderGroup.render(partialTicks);
                        GlStateManager.popMatrix();
                    }

                    this.mc.getFramebuffer().bindFramebuffer(true);
                }

                this.renderEndNanoTime = System.nanoTime();
                this.mc.mcProfiler.endStartSection("gui");

                if (!this.mc.gameSettings.hideGUI || this.mc.currentScreen != null)
                {
                    GlStateManager.alphaFunc(516, 0.1F);
                    this.setupOverlayRendering();
                    this.renderItemActivation(i1, j1, partialTicks);
                    this.mc.ingameGUI.renderGameOverlay(partialTicks);
                }

                this.mc.mcProfiler.endSection();
            }
            else
            {
                GlStateManager.viewport(0, 0, this.mc.displayWidth, this.mc.displayHeight);
                GlStateManager.matrixMode(5889);
                GlStateManager.loadIdentity();
                GlStateManager.matrixMode(5888);
                GlStateManager.loadIdentity();
                this.setupOverlayRendering();
                this.renderEndNanoTime = System.nanoTime();
                // Forge: Fix MC-112292
                net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher.instance.renderEngine = this.mc.getTextureManager();
                // Forge: also fix rendering text before entering world (not part of MC-112292, but the same reason)
                net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher.instance.fontRenderer = this.mc.fontRenderer;
            }

            if (this.mc.currentScreen != null)
            {
                GlStateManager.clear(256);

                try
                {
                    net.minecraftforge.client.ForgeHooksClient.drawScreen(this.mc.currentScreen, k1, l1, this.mc.getTickLength());
                }
                catch (Throwable throwable)
                {
                    CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Rendering screen");
                    CrashReportCategory crashreportcategory = crashreport.makeCategory("Screen render details");
//                    crashreportcategory.addDetail("Screen name", new ICrashReportDetail<String>()
//                    {
//                        public String call() throws Exception
//                        {
//                            return Minecraft.getMinecraft().currentScreen.getClass().getCanonicalName();
//                        }
//                    });
                    Minecraft.LOGGER.fatal(Minecraft.getMinecraft().currentScreen.getClass().getCanonicalName());
//                    crashreportcategory.addDetail("Mouse location", new ICrashReportDetail<String>()
//                    {
//                        public String call() throws Exception
//                        {
//                            return String.format("Scaled: (%d, %d). Absolute: (%d, %d)", k1, l1, Mouse.getX(), Mouse.getY());
//                        }
//                    });
                    Minecraft.LOGGER.fatal(Minecraft.getMinecraft().currentScreen.getClass().getCanonicalName());
//                    crashreportcategory.addDetail("Screen size", new ICrashReportDetail<String>()
//                    {
//                        public String call() throws Exception
//                        {
//                            return String.format("Scaled: (%d, %d). Absolute: (%d, %d). Scale factor of %d", scaledresolution.getScaledWidth(), scaledresolution.getScaledHeight(), Minecraft.getMinecraft().displayWidth, Minecraft.getMinecraft().displayHeight, scaledresolution.getScaleFactor());
//                        }
//                    });
                    Minecraft.LOGGER.fatal(Minecraft.getMinecraft().currentScreen.getClass().getCanonicalName());
                    throw new ReportedException(crashreport);
                }
            }
        }
        ci.cancel();
	}
	@Shadow
	protected abstract void renderItemActivation(int i1, int j1, float partialTicks2);
	@Shadow
	protected abstract void setupOverlayRendering();
	@Shadow
	protected abstract void createWorldIcon();
	@Shadow
	protected abstract void renderWorld(float partialTicks2, long l);
	
	@Override
	public void runSubtick(float partialTicks) {
		boolean flag=Display.isActive();
        if (flag && Minecraft.IS_RUNNING_ON_MAC && mc.inGameHasFocus && !Mouse.isInsideWindow())
        {
            Mouse.setGrabbed(false);
            Mouse.setCursorPosition(Display.getWidth() / 2, Display.getHeight() / 2 - 20);
            Mouse.setGrabbed(true);
        }

        if (mc.inGameHasFocus && flag)
        {
            
            mc.getTutorial().handleMouse(mc.mouseHelper);
            float f = mc.gameSettings.mouseSensitivity * 0.6F + 0.2F;
            float f1 = f * f * f * 8.0F;
            float f2 = (float)dX * f1;
            float f3 = (float)dY * f1;
            int i = 1;

            dX = 0;
            dY = 0;
            
            
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
//            VirtualInput.fillSubtick(VirtualInput.getTimeSinceLastTick(), mc.player.rotationPitch,  mc.player.rotationYaw);
            VirtualInput.fillSubtickWithPlayback();
            mc.player.rotationPitch=VirtualInput.getSubtickPitch();
            mc.player.rotationYaw=VirtualInput.getSubtickYaw();
            CameraInterpolationEvents.rotationPitch = mc.player.rotationPitch;
            CameraInterpolationEvents.rotationYaw = 180f + mc.player.rotationYaw;
        }
    }
}
