package de.scribble.lp.tasmod.mixin;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Queue;
import java.util.concurrent.FutureTask;

import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import de.scribble.lp.tasmod.CommonProxy;
import de.scribble.lp.tasmod.savestates.SavestateHandler;
import de.scribble.lp.tasmod.tickratechanger.TickrateChangerServer;
import de.scribble.lp.tasmod.ticksync.TickSyncPackage;
import de.scribble.lp.tasmod.ticksync.TickSyncServer;
import net.minecraft.crash.CrashReport;
import net.minecraft.network.ServerStatusResponse;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ReportedException;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.WorldServer;

@Mixin(MinecraftServer.class)
public abstract class MixinMinecraftServer {
	@Shadow
	public abstract boolean init();
	@Shadow
	private long currentTime;
	@Shadow
	private ServerStatusResponse statusResponse;
	@Shadow
	public abstract void applyServerIconToResponse(ServerStatusResponse response);
	@Shadow
	private boolean serverRunning;
	@Shadow
	private String motd;
	@Shadow
	private long timeOfLastWarning;
	@Shadow
	private WorldServer[] worlds;
	@Shadow
	public abstract void tick();
	@Shadow
	private boolean serverIsRunning;
	@Shadow
	public abstract void finalTick(CrashReport report);
	@Shadow
	public abstract CrashReport addServerInfoToCrashReport(CrashReport report);
	@Shadow
	public abstract File getDataDirectory();
	@Shadow
	public abstract void stopServer();
	@Shadow
	private boolean serverStopped;
	@Shadow
	public abstract void systemExitNow();
	@Shadow
	private Queue < FutureTask<? >> futureTaskQueue;
	@Shadow
	private static Logger LOGGER;
	
	/**
	 * This mixin changes the ticking behaviour of minecraft servers.<br>
	 * The way this is usually done is set the Thread.sleep from 50 Milliseconds to your desired tickrate in milliseconds<br>
	 * Not the best course of action if you try to implement tickrate 0, which is setting the milliseconds per tick to Long.MAX_VALUE...<br>
	 * Yes indeed, this freezes the server entirely, but there is also no way to unfreeze the server... <br>
	 * <br>
	 * To fix this, there is a for loop with a Thread.sleep(1L). The for loop runs for the desired milliseconds to tick and can also be interrupted.<br>
	 * And the future task queue is also still being processed while in tickrate 0.<br>
	 * <br>
	 * Original author of the tickrate 0 mod is Cubitect. He demonstrated this mod in mcp and uploaded the changes here {@link https://github.com/Cubitect/Cubitick}
	 * @param ci
	 * 
	 * @author Cubitect, ScribbleLP
	 */
	@Inject(method="run", at=@At("HEAD"), cancellable=true)
	public void redoRun(CallbackInfo ci) {
	 try
     {
         if (this.init())
         {
             net.minecraftforge.fml.common.FMLCommonHandler.instance().handleServerStarted();
             this.currentTime = MinecraftServer.getCurrentTimeMillis();
             long i = 0L;
             this.statusResponse.setServerDescription(new TextComponentString(this.motd));
             this.statusResponse.setVersion(new ServerStatusResponse.Version("1.12.2", 340));
             this.applyServerIconToResponse(this.statusResponse);

             while (this.serverRunning)
             {
                 long k = MinecraftServer.getCurrentTimeMillis();
                 long j = k - this.currentTime;

                 if (j > 2000L && this.currentTime - this.timeOfLastWarning >= 15000L)
                 {
                     LOGGER.warn("Can't keep up! Did the system time change, or is the server overloaded? Running {}ms behind, skipping {} tick(s)", Long.valueOf(j), Long.valueOf(j / 50L));
                     j = 2000L;
                     this.timeOfLastWarning = this.currentTime;
                 }

                 if (j < 0L)
                 {
                	 LOGGER.warn("Time ran backwards! Did the system time change?");
                     j = 0L;
                 }

                 i += j;
                 this.currentTime = k;

                 if (this.worlds[0].areAllPlayersAsleep())
                 {
                     this.tick();
                     i = 0L;
                 }
                 else
                 {
                 	//Changed from 50L
                     while (i > TickrateChangerServer.MILISECONDS_PER_TICK)
                     {
                         i -= TickrateChangerServer.MILISECONDS_PER_TICK;
                         this.tick();
                         
                         if(SavestateHandler.wasLoading) {
                        	 SavestateHandler.wasLoading=false;
                        	 SavestateHandler.playerLoadSavestateEventServer();
                         }
                         
                         if (TickSyncServer.isEnabled()) {
     						if (TickSyncServer.getServertickcounter() == Integer.MAX_VALUE - 1) {
     							TickSyncServer.resetTickCounter();
     							CommonProxy.NETWORK.sendToAll(new TickSyncPackage(TickSyncServer.getServertickcounter(), true, true));
     						} else {
     							TickSyncServer.incrementServerTickCounter();
     							CommonProxy.NETWORK.sendToAll(new TickSyncPackage(TickSyncServer.getServertickcounter(), false, true));
     						}
     					} else {
     						TickSyncServer.incrementServerTickCounter();
     						CommonProxy.NETWORK.sendToAll(new TickSyncPackage(TickSyncServer.getServertickcounter(), false, false));
     					}
     					if(TickrateChangerServer.ADVANCE_TICK) {
     						TickrateChangerServer.changeServerTickrate(0F);
     						TickrateChangerServer.ADVANCE_TICK=false;
     					}
                     }
                 }
                 //Added Methods similar to Cubitick Mod
                 //Original line Thread.sleep(Math.max(1L, 50L - i));
					long msToTick = (long) (TickrateChangerServer.MILISECONDS_PER_TICK - i);
					if (msToTick <= 0L) {
						if (TickrateChangerServer.TICKS_PER_SECOND > 20.0)
							msToTick = 0L;
						else
							msToTick = 1L;
					}
					for (long o = 0; o < msToTick; o++) {
						if (TickrateChangerServer.INTERRUPT) {
							LOGGER.debug("Interrupting " + o + " " + msToTick);
							msToTick = 1L;
							currentTime = System.currentTimeMillis();
							TickrateChangerServer.INTERRUPT = false;
						}
						synchronized (this.futureTaskQueue) {
							while (!this.futureTaskQueue.isEmpty()) {
								try {
									LOGGER.debug("Processing Future Task Queue");
									((FutureTask) this.futureTaskQueue.poll()).run();
								} catch (Throwable var9) {
									var9.printStackTrace();
								}
							}
						}
						try {
							Thread.sleep(1L);
						} catch (InterruptedException e) {
							LOGGER.error("Thread.sleep in MixinMinecraftServer couldn't be processed!");
							LOGGER.catching(e);
						}
					}
                 this.serverIsRunning = true;
             }
             net.minecraftforge.fml.common.FMLCommonHandler.instance().handleServerStopping();
             net.minecraftforge.fml.common.FMLCommonHandler.instance().expectServerStopped(); // has to come before finalTick to avoid race conditions
         }
         else
         {
             net.minecraftforge.fml.common.FMLCommonHandler.instance().expectServerStopped(); // has to come before finalTick to avoid race conditions
             this.finalTick((CrashReport)null);
         }
     }
     catch (net.minecraftforge.fml.common.StartupQuery.AbortedException e)
     {
         // ignore silently
         net.minecraftforge.fml.common.FMLCommonHandler.instance().expectServerStopped(); // has to come before finalTick to avoid race conditions
     }
     catch (Throwable throwable1)
     {
         LOGGER.error("Encountered an unexpected exception", throwable1);
         CrashReport crashreport = null;

         if (throwable1 instanceof ReportedException)
         {
             crashreport = this.addServerInfoToCrashReport(((ReportedException)throwable1).getCrashReport());
         }
         else
         {
             crashreport = this.addServerInfoToCrashReport(new CrashReport("Exception in server tick loop", throwable1));
         }

         File file1 = new File(new File(this.getDataDirectory(), "crash-reports"), "crash-" + (new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss")).format(new Date()) + "-server.txt");

         if (crashreport.saveToFile(file1))
         {
             LOGGER.error("This crash report has been saved to: {}", (Object)file1.getAbsolutePath());
         }
         else
         {
        	 LOGGER.error("We were unable to save this crash report to disk.");
         }

         net.minecraftforge.fml.common.FMLCommonHandler.instance().expectServerStopped(); // has to come before finalTick to avoid race conditions
         this.finalTick(crashreport);
     }
     finally
     {
         try
         {
             this.stopServer();
             this.serverStopped = true;
         }
         catch (Throwable throwable)
         {
             LOGGER.error("Exception stopping the server", throwable);
         }
         finally
         {
             net.minecraftforge.fml.common.FMLCommonHandler.instance().handleServerStopped();
             this.serverStopped = true;
             this.systemExitNow();
         }
     }
	 ci.cancel();
	}
}