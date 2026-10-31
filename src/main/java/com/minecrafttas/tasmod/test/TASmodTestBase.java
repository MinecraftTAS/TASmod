package com.minecrafttas.tasmod.test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

import com.minecrafttas.mctcommon.events.EventClient.EventPlayerJoinedClientSide;
import com.minecrafttas.mctcommon.test.TestBase;
import com.minecrafttas.tasmod.savestates.SavestateIndexer;

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiIngameMenu;
import net.minecraft.client.gui.GuiMainMenu;

public abstract class TASmodTestBase extends TestBase implements EventPlayerJoinedClientSide {
	protected final Path defaultTestWorldFolder;

	public TASmodTestBase() {
		this(Paths.get("../src/test/resources/testrunner"));
	}

	public TASmodTestBase(Path defaultTestWorldFolder) {
		this.defaultTestWorldFolder = defaultTestWorldFolder;
	}

	protected void launchWorld(String worldname) {
		launchWorld(worldname, defaultTestWorldFolder.resolve(worldname + ".zip"));
	}

	protected synchronized void launchWorld(String worldname, Path pathToWorldZip) {
		Minecraft mc = Minecraft.getMinecraft();

		Path worldInSavesFolder = mc.gameDir.toPath().resolve("saves");

		ZipFile zipFile = new ZipFile(pathToWorldZip.toFile());
		try {
			zipFile.extractAll(worldInSavesFolder.toString());
		} catch (ZipException e) {
			e.printStackTrace();
		}
		try {
			zipFile.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		mc.addScheduledTask(() -> {
			mc.launchIntegratedServer(worldname, worldname, null);
		});
		try {
			this.wait(TimeUnit.MINUTES.toMillis(1));
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onPlayerJoinedClientSide(EntityPlayerSP player) {
		synchronized (this) {
			notify();
		}
	}

	protected synchronized void quitWorld() {
		Minecraft mc = Minecraft.getMinecraft();
		String worldname = mc.getIntegratedServer().getFolderName();
		mc.addScheduledTask(() -> {
			mc.displayGuiScreen(new GuiIngameMenu());
			mc.world.sendQuittingDisconnectingPacket();
			mc.loadWorld(null);
			mc.displayGuiScreen(new GuiMainMenu());
			synchronized (this) {
				this.notify();
			}
		});
		try {
			this.wait(TimeUnit.SECONDS.toMillis(10));
			Thread.sleep(TimeUnit.SECONDS.toMillis(1));
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		Path worldInSavesFolder = mc.gameDir.toPath().resolve("saves").resolve(worldname);
		SavestateIndexer.deleteFolder(worldInSavesFolder);
	}
}
