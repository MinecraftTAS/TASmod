package de.scribble.lp.tasmod.mixin;

import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.FutureTask;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import de.scribble.lp.tasmod.ModLoader;
import de.scribble.lp.tasmod.duck.SubtickDuck;
import de.scribble.lp.tasmod.input.InputContainer;
import de.scribble.lp.tasmod.playback.InputPlayback;
import de.scribble.lp.tasmod.recording.InputRecorder;
import de.scribble.lp.tasmod.tickratechanger.TickrateChangerClient;
import de.scribble.lp.tasmod.ticksync.TickSync;
import de.scribble.lp.tasmod.virtual.VirtualInput;
import de.scribble.lp.tasmod.virtual.VirtualKeybindings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.MusicTicker;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiGameOver;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiSleepMP;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.toasts.GuiToast;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.client.tutorial.Tutorial;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.Entity;
import net.minecraft.network.NetworkManager;
import net.minecraft.profiler.Profiler;
import net.minecraft.profiler.Snooper;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.util.FrameTimer;
import net.minecraft.util.ReportedException;
import net.minecraft.util.Timer;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.EnumDifficulty;

@Mixin(Minecraft.class)
public abstract class MixinMinecraft {
	
	@Shadow
	private boolean isGamePaused;
	@Shadow
	private float renderPartialTicksPaused;
	@Shadow
	private Timer timer;
	@Shadow
	private EntityRenderer entityRenderer;
	@Shadow
	private IReloadableResourceManager mcResourceManager;
	@Shadow
	private int rightClickDelayTimer;
	@Shadow
	private Profiler mcProfiler;
	@Shadow
	private GuiIngame ingameGUI;
	@Shadow
	private Tutorial tutorial;
	@Shadow
	private WorldClient world;
	@Shadow
	private RayTraceResult objectMouseOver;
	@Shadow
	private PlayerControllerMP playerController;
	@Shadow
	private TextureManager renderEngine;
	@Shadow
	private GuiScreen currentScreen;
	@Shadow
	private EntityPlayerSP player;
	@Shadow
	private int leftClickCounter;
	@Shadow
	private int joinPlayerCounter;
	@Shadow
	private RenderGlobal renderGlobal;
	@Shadow
	private MusicTicker mcMusicTicker;
	@Shadow
	private SoundHandler mcSoundHandler;
	@Shadow
	private ParticleManager effectRenderer;
	@Shadow
	private NetworkManager myNetworkManager;
	@Shadow
	private long systemTime;
	
	@Shadow
	private GameSettings gameSettings;
	@Shadow
	private boolean actionKeyF3;
	@Shadow
	private long debugCrashKeyPressTime;
	
	@Shadow
	private boolean inGameHasFocus;
    @Shadow
    public abstract Entity getRenderViewEntity();
    @Shadow
    public abstract NetHandlerPlayClient getConnection();
    @Shadow
    public abstract void clickMouse();
    @Shadow
    public abstract void rightClickMouse();
    @Shadow
    public abstract void middleClickMouse();
    @Shadow
	private Queue < FutureTask<? >> scheduledTasks;
	@Shadow
	private Framebuffer framebufferMc;
	@Shadow
	private boolean skipRenderWorld;
	@Shadow
	private GuiToast toastGui;
	@Shadow
	private long prevFrameTime;
	@Shadow
	private int displayWidth;
	@Shadow
	private int displayHeight;
	@Shadow
	private int fpsCounter;
	@Shadow
	private FrameTimer frameTimer;
	@Shadow
	private long startNanoTime;
	@Shadow
	private long debugUpdateTime;
	@Shadow
	private String debug;
	@Shadow
	private Snooper usageSnooper;
	@Shadow
	private IntegratedServer integratedServer;
	@Shadow
	private static int debugFPS;
	
	@Shadow
    protected abstract void displayDebugInfo(long i1);
    @Shadow
	protected abstract void shutdown();
    @Shadow
	protected abstract boolean isSingleplayer();
    @Shadow
	protected abstract void checkGLError(String string);
    @Shadow
	protected abstract void updateDisplay();
	@Shadow
	protected abstract int getLimitFramerate();
    @Shadow
	protected abstract boolean isFramerateLimitBelowMax();
    
	/**
	 * Rewrites 
	 * 
	 */
    @Inject(method = "runGameLoop", at = @At(value="HEAD"), cancellable = true)
    public void redoentireRunGameLoop(CallbackInfo ci) throws IOException {
    	long i = System.nanoTime();
        this.mcProfiler.startSection("root");

        if (Display.isCreated() && Display.isCloseRequested())
        {
            this.shutdown();
        }

        this.timer.updateTimer();
        this.mcProfiler.startSection("scheduledExecutables");

        synchronized (this.scheduledTasks)
        {
            while (!this.scheduledTasks.isEmpty())
            {
                Util.runTask(this.scheduledTasks.poll(), Minecraft.LOGGER);
            }
        }

        this.mcProfiler.endSection();
        long l = System.nanoTime();
        this.mcProfiler.startSection("tick");
        
        //TASmod
        VirtualKeybindings.increaseCooldowntimer();
        TickrateChangerClient.INSTANCE.bypass();
        
        for (int j = 0; j < Math.min(10, this.timer.elapsedTicks); ++j)
        {
        	for (int j2 = 0; j2 < TickSync.getTickAmount((Minecraft)(Object)this); j2++) {
        		if(TickrateChangerClient.TICKS_PER_SECOND!=0) {
					((SubtickDuck)this.entityRenderer).runSubtick(this.isGamePaused ? this.renderPartialTicksPaused : this.timer.renderPartialTicks);
				}
				this.runTick();
			}
			if(TickrateChangerClient.ADVANCE_TICK) {
				TickrateChangerClient.ADVANCE_TICK=false;
				TickrateChangerClient.changeClientTickrate(0F);
			}
        }
		this.mcProfiler.endStartSection("preRenderErrors");
        long i1 = System.nanoTime() - l;
        this.checkGLError("Pre render");
        this.mcProfiler.endStartSection("sound");
        this.mcSoundHandler.setListener(this.getRenderViewEntity(), this.timer.renderPartialTicks); //Forge: MC-46445 Spectator mode particles and sounds computed from where you have been before
        this.mcProfiler.endSection();
        this.mcProfiler.startSection("render");
        GlStateManager.pushMatrix();
        GlStateManager.clear(16640);
        this.framebufferMc.bindFramebuffer(true);
        this.mcProfiler.startSection("display");
        GlStateManager.enableTexture2D();
        this.mcProfiler.endSection();

        if (!this.skipRenderWorld)
        {
            net.minecraftforge.fml.common.FMLCommonHandler.instance().onRenderTickStart(this.timer.renderPartialTicks);
            this.mcProfiler.endStartSection("gameRenderer");
            this.entityRenderer.updateCameraAndRender(this.isGamePaused ? this.renderPartialTicksPaused : this.timer.renderPartialTicks, i);
            this.mcProfiler.endStartSection("toasts");
            this.toastGui.drawToast(new ScaledResolution((Minecraft)(Object)this));
            this.mcProfiler.endSection();
            net.minecraftforge.fml.common.FMLCommonHandler.instance().onRenderTickEnd(this.timer.renderPartialTicks);
        }

        this.mcProfiler.endSection();

        if (this.gameSettings.showDebugInfo && this.gameSettings.showDebugProfilerChart && !this.gameSettings.hideGUI)
        {
            if (!this.mcProfiler.profilingEnabled)
            {
                this.mcProfiler.clearProfiling();
            }

            this.mcProfiler.profilingEnabled = true;
            this.displayDebugInfo(i1);
        }
        else
        {
            this.mcProfiler.profilingEnabled = false;
            this.prevFrameTime = System.nanoTime();
        }

        this.framebufferMc.unbindFramebuffer();
        GlStateManager.popMatrix();
        GlStateManager.pushMatrix();
        this.framebufferMc.framebufferRender(this.displayWidth, this.displayHeight);
        GlStateManager.popMatrix();
        GlStateManager.pushMatrix();
        this.entityRenderer.renderStreamIndicator(this.timer.renderPartialTicks);
        GlStateManager.popMatrix();
        this.mcProfiler.startSection("root");
        this.updateDisplay();
        Thread.yield();
        this.checkGLError("Post render");
        ++this.fpsCounter;
        boolean flag = this.isSingleplayer() && this.currentScreen != null && this.currentScreen.doesGuiPauseGame() && !this.integratedServer.getPublic();

        if (this.isGamePaused != flag)
        {
            if (this.isGamePaused)
            {
                this.renderPartialTicksPaused = this.timer.renderPartialTicks;
            }
            else
            {
                this.timer.renderPartialTicks = this.renderPartialTicksPaused;
            }

            this.isGamePaused = flag;
        }

        long k = System.nanoTime();
        this.frameTimer.addFrame(k - this.startNanoTime);
        this.startNanoTime = k;

        while (Minecraft.getSystemTime() >= this.debugUpdateTime + 1000L)
        {
            debugFPS = this.fpsCounter;
            this.debug = String.format("%d fps (%d chunk update%s) T: %s%s%s%s%s", debugFPS, RenderChunk.renderChunksUpdated, RenderChunk.renderChunksUpdated == 1 ? "" : "s", (float)this.gameSettings.limitFramerate == GameSettings.Options.FRAMERATE_LIMIT.getValueMax() ? "inf" : this.gameSettings.limitFramerate, this.gameSettings.enableVsync ? " vsync" : "", this.gameSettings.fancyGraphics ? "" : " fast", this.gameSettings.clouds == 0 ? "" : (this.gameSettings.clouds == 1 ? " fast-clouds" : " fancy-clouds"), OpenGlHelper.useVbo() ? " vbo" : "");
            RenderChunk.renderChunksUpdated = 0;
            this.debugUpdateTime += 1000L;
            this.fpsCounter = 0;
            this.usageSnooper.addMemoryStatsToSnooper();

            if (!this.usageSnooper.isSnooperRunning())
            {
                this.usageSnooper.startSnooper();
            }
        }

        if (this.isFramerateLimitBelowMax())
        {
            this.mcProfiler.startSection("fpslimit_wait");
            Display.sync(this.getLimitFramerate());
            this.mcProfiler.endSection();
        }

        this.mcProfiler.endSection();
        ci.cancel();
    }
    @Shadow
    protected abstract void loadWorld(WorldClient worldClient);
//	@Inject(method="runGameLoop",at = @At(value = "INVOKE", target = "Lnet/minecraft/profiler/Profiler;startSection(S)V", shift = At.Shift.AFTER, remap = false, ordinal = 2))
//	public void injectTick(CallbackInfo ci) {
//		if(TickrateChangerClient.cooldownKeyPause>0) {
//        	TickrateChangerClient.cooldownKeyPause--;
//        }
//        if(TickrateChangerClient.cooldownKeyAdvance>0) {
//        	TickrateChangerClient.cooldownKeyAdvance--;
//        }
//        TickrateChangerClient.INSTANCE.bypass();
//        for (int j = 0; j < Math.min(10, this.timer.elapsedTicks); ++j)
//        {
//	        if(TickSync.isEnabled()&&Minecraft.getMinecraft().world!=null) {
//				if(TickSync.getClienttickcounter()==TickSync.getServertickcounter()) { //If the tickrate matches the server tickrate
//					TickSync.incrementClienttickcounter();
//					if(TickrateChangerClient.TICKS_PER_SECOND!=0) {
//						centityRenderer.runSubtick(this.isGamePaused ? this.renderPartialTicksPaused : this.timer.renderPartialTicks,(Minecraft)(Object)this,this.entityRenderer.smoothCamYaw,this.entityRenderer.smoothCamPitch,this.entityRenderer.smoothCamPartialTicks,this.entityRenderer.smoothCamFilterX,this.entityRenderer.smoothCamFilterY);
//					}
//					this.runTick();
//				}else if(TickSync.getClienttickcounter()>TickSync.getServertickcounter()) {	//If it's too fast
//					continue;
//				}else if(TickSync.getClienttickcounter()<TickSync.getServertickcounter()) {
//					for(int h=0;h<TickSync.getServertickcounter()-TickSync.getClienttickcounter();h++) {	//If it's too slow
//						TickSync.incrementClienttickcounter();
//						if(TickrateChangerClient.TICKS_PER_SECOND!=0) {
//							centityRenderer.runSubtick(this.isGamePaused ? this.renderPartialTicksPaused : this.timer.renderPartialTicks,(Minecraft)(Object)this,this.entityRenderer.smoothCamYaw,this.entityRenderer.smoothCamPitch,this.entityRenderer.smoothCamPartialTicks,this.entityRenderer.smoothCamFilterX,this.entityRenderer.smoothCamFilterY);
//						}
//						this.runTick();
//					}
//				}
//			}else if(Minecraft.getMinecraft().world==null) {
//				this.runTick();
//			}else { //If Ticksync is disabled
//				TickSync.incrementClienttickcounter();
//				if(TickrateChangerClient.TICKS_PER_SECOND!=0) {
//					centityRenderer.runSubtick(this.isGamePaused ? this.renderPartialTicksPaused : this.timer.renderPartialTicks,(Minecraft)(Object)this,this.entityRenderer.smoothCamYaw,this.entityRenderer.smoothCamPitch,this.entityRenderer.smoothCamPartialTicks,this.entityRenderer.smoothCamFilterX,this.entityRenderer.smoothCamFilterY);
//				}
//				this.runTick();
//			}
//			if(TickrateChangerClient.ADVANCE_TICK) {
//				TickrateChangerClient.ADVANCE_TICK=false;
//				TickrateChangerClient.changeClientTickrate(0F);
//			}
//        }
//	}
//	@Redirect(method="runGameLoop", at = @At(value="INVOKE",target = "Lnet/minecraft/client/Minecraft;runTick()V"))
//	public void cancelVanilla() {
//		return;
//	}
	@Shadow
	protected abstract void runTick();
	
	@Inject(method="init",at = @At(value = "RETURN"))
	public void injectInit(CallbackInfo ci) {
		ModLoader.logger.debug("Initialising stuff for TASmod");
		new TickrateChangerClient();
		new TickSync();
	}
	
	@Inject(method="runTick", at=@At(value="HEAD"), cancellable = true)
	public void injectRunTick(CallbackInfo ci) throws IOException {
		TickSync.incrementClienttickcounter();
		if (this.rightClickDelayTimer > 0)
        {
            --this.rightClickDelayTimer;
        }

		net.minecraftforge.fml.common.FMLCommonHandler.instance().onPreClientTick();
		
        this.mcProfiler.startSection("gui");

        if (!this.isGamePaused)
        {
            this.ingameGUI.updateTick();
        }

        this.mcProfiler.endSection();
        this.entityRenderer.getMouseOver(1.0F);
        this.tutorial.onMouseHover(this.world, this.objectMouseOver);
        this.mcProfiler.startSection("gameMode");

        if (!this.isGamePaused && this.world != null)
        {
            this.playerController.updateController();
        }

        this.mcProfiler.endStartSection("textures");

        if (this.world != null)
        {
            this.renderEngine.tick();
        }

        if (this.currentScreen == null && this.player != null)
        {
            if (this.player.getHealth() <= 0.0F && !(this.currentScreen instanceof GuiGameOver))
            {
                this.displayGuiScreen((GuiScreen)null);
            }
            else if (this.player.isPlayerSleeping() && this.world != null)
            {
                this.displayGuiScreen(new GuiSleepMP());
            }
        }
        else if (this.currentScreen != null && this.currentScreen instanceof GuiSleepMP && !this.player.isPlayerSleeping())
        {
            this.displayGuiScreen((GuiScreen)null);
        }

        if (this.currentScreen != null)
        {
            this.leftClickCounter = 10000;
        }
        
        if (this.currentScreen != null)
        {
        	InputPlayback.nextPlaybackTick();
            try
            {
                this.currentScreen.handleInput();
            }
            catch (Throwable throwable1)
            {
                CrashReport crashreport = CrashReport.makeCrashReport(throwable1, "Updating screen events");
                CrashReportCategory crashreportcategory = crashreport.makeCategory("Affected screen");
//                crashreportcategory.addDetail("Screen name", new ICrashReportDetail<String>()
//                {
//                    public String call() throws Exception
//                    {
//                        return Minecraft.getMinecraft().currentScreen.getClass().getCanonicalName();
//                    }
//                });
                Minecraft.LOGGER.fatal(this.currentScreen.getClass().getCanonicalName());
                throw new ReportedException(crashreport);
            }

            if (this.currentScreen != null)
            {
                try
                {
                    this.currentScreen.updateScreen();
                }
                catch (Throwable throwable)
                {
                    CrashReport crashreport1 = CrashReport.makeCrashReport(throwable, "Ticking screen");
                    CrashReportCategory crashreportcategory1 = crashreport1.makeCategory("Affected screen");
//                    crashreportcategory1.addDetail("Screen name", new ICrashReportDetail<String>()
//                    {
//                        public String call() throws Exception
//                        {
//                            return Minecraft.getMinecraft().currentScreen.getClass().getCanonicalName();
//                        }
//                    });
                    Minecraft.LOGGER.fatal(this.currentScreen.getClass().getCanonicalName());
                    throw new ReportedException(crashreport1);
                }
            }
            InputRecorder.recordTick();
        }
        if (this.currentScreen == null || this.currentScreen.allowUserInput)
        {
        	InputPlayback.nextPlaybackTick();
            this.mcProfiler.endStartSection("mouse");
            modifiedRunTickMouse();

            if (this.leftClickCounter > 0)
            {
                --this.leftClickCounter;
            }

            this.mcProfiler.endStartSection("keyboard");
            modifiedRunTickKeyboard();
            InputRecorder.recordTick();
        }

        if (this.world != null)
        {
            if (this.player != null)
            {
                ++this.joinPlayerCounter;

                if (this.joinPlayerCounter == 30)
                {
                    this.joinPlayerCounter = 0;
                    this.world.joinEntityInSurroundings(this.player);
                }
            }

            this.mcProfiler.endStartSection("gameRenderer");

            if (!this.isGamePaused)
            {
                this.entityRenderer.updateRenderer();
            }

            this.mcProfiler.endStartSection("levelRenderer");

            if (!this.isGamePaused)
            {
                this.renderGlobal.updateClouds();
            }

            this.mcProfiler.endStartSection("level");

            if (!this.isGamePaused)
            {
                if (this.world.getLastLightningBolt() > 0)
                {
                    this.world.setLastLightningBolt(this.world.getLastLightningBolt() - 1);
                }

                this.world.updateEntities();
            }
        }
        else if (this.entityRenderer.isShaderActive())
        {
            this.entityRenderer.stopUseShader();
        }

        if (!this.isGamePaused)
        {
            this.mcMusicTicker.update();
            this.mcSoundHandler.update();
        }

        if (this.world != null)
        {
            if (!this.isGamePaused)
            {
                this.world.setAllowedSpawnTypes(this.world.getDifficulty() != EnumDifficulty.PEACEFUL, true);
                this.tutorial.update();

                try
                {
                    this.world.tick();
                }
                catch (Throwable throwable2)
                {
                    CrashReport crashreport2 = CrashReport.makeCrashReport(throwable2, "Exception in world tick");

                    if (this.world == null)
                    {
                        CrashReportCategory crashreportcategory2 = crashreport2.makeCategory("Affected level");
                        crashreportcategory2.addCrashSection("Problem", "Level is null!");
                    }
                    else
                    {
                        this.world.addWorldInfoToCrashReport(crashreport2);
                    }

                    throw new ReportedException(crashreport2);
                }
            }

            this.mcProfiler.endStartSection("animateTick");

            if (!this.isGamePaused && this.world != null)
            {
                this.world.doVoidFogParticles(MathHelper.floor(this.player.posX), MathHelper.floor(this.player.posY), MathHelper.floor(this.player.posZ));
            }

            this.mcProfiler.endStartSection("particles");

            if (!this.isGamePaused)
            {
                this.effectRenderer.updateEffects();
            }
        }
        else if (this.myNetworkManager != null)
        {
            this.mcProfiler.endStartSection("pendingConnection");
            this.myNetworkManager.processReceivedPackets();
        }

        this.mcProfiler.endSection();
        net.minecraftforge.fml.common.FMLCommonHandler.instance().onPostClientTick();
        this.systemTime = Minecraft.getSystemTime();
        ci.cancel();
	}
	@Shadow
	public abstract void runTickKeyboard();
	@Shadow
	public abstract void runTickMouse();
	@Shadow
	public abstract void displayGuiScreen(GuiScreen guiScreen);
	
	
	private void modifiedRunTickKeyboard() throws IOException{

		VirtualInput.prepareKeyboardEvents();
		
		while (Keyboard.next()) {
			VirtualInput.fillKeyboardEvents(Keyboard.getEventKey() == 0 ? Keyboard.getEventCharacter() + 256 : Keyboard.getEventKey(), Keyboard.getEventKeyState(), Keyboard.getEventCharacter());
		}
		
		VirtualInput.fillKeyboardEventsWithPlayback();

		while (VirtualInput.nextKeyboardEvent()) {
			
			int i = VirtualInput.getEventKeyboardButton() == 0 ? VirtualInput.getEventChar() + 256 : VirtualInput.getEventKeyboardButton();
			
			i = VirtualInput.runThroughKeyboard(i, VirtualInput.getEventKeyboardButtonState());
			
			if (this.debugCrashKeyPressTime > 0L) {
				if (Minecraft.getSystemTime() - this.debugCrashKeyPressTime >= 6000L) {
					throw new ReportedException(new CrashReport("Manually triggered debug crash", new Throwable()));
				}

				// Original Line: if (!Keyboard.isKeyDown(46) || !Keyboard.isKeyDown(61))
				if (!VirtualInput.isKeyDown(46) || !VirtualInput.isKeyDown(61)) {
					this.debugCrashKeyPressTime = -1L;
				}
			}
			// Original Line: else if (Keyboard.isKeyDown(46) && Keyboard.isKeyDown(61))
			else if (VirtualInput.isKeyDown(46) && VirtualInput.isKeyDown(61)) {
				this.actionKeyF3 = true;
				this.debugCrashKeyPressTime = Minecraft.getSystemTime();
			}

			this.dispatchKeypresses();

			if (this.currentScreen != null) {
				this.currentScreen.handleKeyboardInput();
			}
			boolean flag = VirtualInput.isKeyDown(i);

			if (flag) {
				if (i == 62 && this.entityRenderer != null) {
					this.entityRenderer.switchUseShader();
				}

				boolean flag1 = false;

				if (this.currentScreen == null) {
					if (i == 1) {
						this.displayInGameMenu();
					}

					flag1 = VirtualInput.isKeyDown(61) && this.processKeyF3(i);
					this.actionKeyF3 |= flag1;

					if (i == 59) {
						this.gameSettings.hideGUI = !this.gameSettings.hideGUI;
					}
				}

				if (flag1) {
					KeyBinding.setKeyBindState(i, false);
				} else {
					KeyBinding.setKeyBindState(i, true);
					KeyBinding.onTick(i);
				}

				if (this.gameSettings.showDebugProfilerChart) {
					if (i == 11) {
						this.updateDebugProfilerName(0);
					}

					for (int j = 0; j < 9; ++j) {
						if (i == 2 + j) {
							this.updateDebugProfilerName(j + 1);
						}
					}
				}
			} else {
				KeyBinding.setKeyBindState(i, false);

				if (i == 61) {
					if (this.actionKeyF3) {
						this.actionKeyF3 = false;
					} else {
						this.gameSettings.showDebugInfo = !this.gameSettings.showDebugInfo;
						this.gameSettings.showDebugProfilerChart = this.gameSettings.showDebugInfo
								&& GuiScreen.isShiftKeyDown();
						this.gameSettings.showLagometer = this.gameSettings.showDebugInfo && GuiScreen.isAltKeyDown();
					}
				}
			}
			net.minecraftforge.fml.common.FMLCommonHandler.instance().fireKeyInput();
		}
		InputContainer.add(VirtualInput.keyboard);
		this.processKeyBinds();
	}
	@Shadow
	protected abstract boolean processKeyF3(int i);
	@Shadow
	protected abstract void dispatchKeypresses();
	@Shadow
	protected abstract void displayInGameMenu();
	@Shadow
	protected abstract void updateDebugProfilerName(int i);
	@Shadow
	protected abstract void processKeyBinds();
	
	private void modifiedRunTickMouse() throws IOException {
		VirtualInput.prepareMouseEvents();
		while (Mouse.next()) {
			VirtualInput.fillMouseEvents(Mouse.getEventButton() - 100, Mouse.getEventButtonState(),Mouse.getEventDWheel(), Mouse.getEventX(), Mouse.getEventY(), -1);
		}
		VirtualInput.fillMouseEventsWithPlayback();
		while (VirtualInput.nextMouseEvent()) {

			if (net.minecraftforge.client.ForgeHooksClient.postMouseEvent()) continue; //Might have to disable this one
			
			int i = VirtualInput.getEventMouseButton() + 100;
			VirtualInput.runThroughKeyboard(i-100, VirtualInput.getEventMouseButtonState());
			KeyBinding.setKeyBindState(i - 100, VirtualInput.getEventMouseButtonState());

			if (VirtualInput.getEventMouseButtonState()) {
				if (this.player.isSpectator() && i == 2) {
					this.ingameGUI.getSpectatorGui().onMiddleClick();
				} else {
					KeyBinding.onTick(i - 100);
				}
			}

			long j = Minecraft.getSystemTime() - this.systemTime;
			// TASMod: Fix Mousewheel
			if (j <= (long) Math.max(4000F / TickrateChangerClient.TICKS_PER_SECOND, 200L)) {
				int k = VirtualInput.getEventDWheel();
				if (k != 0) {
					if (this.player.isSpectator()) {
						k = k < 0 ? -1 : 1;

						if (this.ingameGUI.getSpectatorGui().isMenuActive()) {
							this.ingameGUI.getSpectatorGui().onMouseScroll(-k);
						} else {
							float f = MathHelper.clamp(this.player.capabilities.getFlySpeed() + (float) k * 0.005F,	0.0F, 0.2F);
							this.player.capabilities.setFlySpeed(f);
						}
					} else {
						this.player.inventory.changeCurrentItem(k);
					}
				}

				if (this.currentScreen == null) {
					if (!this.inGameHasFocus && VirtualInput.getEventMouseButtonState()) {
						this.setIngameFocus();
					}
				} else if (this.currentScreen != null) {
					this.currentScreen.handleMouseInput();
				}
			}
			net.minecraftforge.fml.common.FMLCommonHandler.instance().fireMouseInput();
		}
	}
	@Shadow
	protected abstract void setIngameFocus();
	
	@Inject(method = "displayGuiScreen", at = @At(value = "HEAD"), cancellable = true)
	public void injectDisplayGuiScreen(GuiScreen guiScreenIn, CallbackInfo ci) {
		if (guiScreenIn == null && this.world == null) {
			guiScreenIn = new GuiMainMenu();
		} else if (guiScreenIn == null && this.player.getHealth() <= 0.0F) {
			guiScreenIn = new GuiGameOver((ITextComponent) null);
		}

		GuiScreen old = this.currentScreen;
		net.minecraftforge.client.event.GuiOpenEvent event = new net.minecraftforge.client.event.GuiOpenEvent(guiScreenIn);

		if (net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(event))
			return;

		guiScreenIn = event.getGui();
		if (old != null && guiScreenIn != old) {
			old.onGuiClosed();
		}

		if (guiScreenIn instanceof GuiMainMenu || guiScreenIn instanceof GuiMultiplayer) {
			this.gameSettings.showDebugInfo = false;
			this.ingameGUI.getChatGUI().clearChatMessages(true);
		}

		this.currentScreen = guiScreenIn;

		if (guiScreenIn != null) {
            this.setIngameNotInFocus();
            KeyBinding.unPressAllKeys();
			VirtualInput.unpressEverything();
//          while (Mouse.next())
//          {
//              ;
//          }
//          while (Keyboard.next())
//          {
//              ;
//          }
			// Added in TASMod
			while (VirtualInput.nextKeyboardEvent()) {;}
			while (VirtualInput.nextMouseEvent()) {;}
			ScaledResolution scaledresolution = new ScaledResolution((Minecraft) (Object) this);
			int i = scaledresolution.getScaledWidth();
			int j = scaledresolution.getScaledHeight();
			guiScreenIn.setWorldAndResolution((Minecraft) (Object) this, i, j);
			this.skipRenderWorld = false;
		} else {
			this.mcSoundHandler.resumeSounds();
			this.setIngameFocus();
		}
		ci.cancel();
	}

	@Shadow
	protected abstract void setIngameNotInFocus();
}
