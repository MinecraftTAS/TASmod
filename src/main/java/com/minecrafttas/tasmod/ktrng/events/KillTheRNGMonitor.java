package com.minecrafttas.tasmod.ktrng.events;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import com.minecrafttas.mctcommon.events.EventServer;
import com.minecrafttas.tasmod.TASmod;
import com.minecrafttas.tasmod.events.EventKillTheRNGServer;
import com.minecrafttas.tasmod.events.EventPlaybackServer;
import com.minecrafttas.tasmod.ktrng.RandomBase.RNGSide;
import com.minecrafttas.tasmod.playback.PlaybackControllerClient.TASstate;
import com.minecrafttas.tasmod.savestates.SavestateIndexer;
import com.minecrafttas.tasmod.util.FileThread;

import net.minecraft.server.MinecraftServer;

public class KillTheRNGMonitor implements EventPlaybackServer.EventControllerStateChange, EventPlaybackServer.EventRecordClear, EventServer.EventServerTick, EventKillTheRNGServer.EventRNG {

	@Override
	public void onServerTick(MinecraftServer server) {
		if (isActive()) {
			thread.addLine("");
		}
	}

	@Override
	public void onControllerStateChange(TASstate newstate, TASstate oldstate) {
		if (!isEnabled()) {
			return;
		}

		if (TASmod.getServerInstance() == null)
			return;

		Path serverDirs = TASmod.getServerInstance().getDataDirectory().toPath();
		Path monitorDir = serverDirs.resolve("ktrngMonitor");

		if (!Files.exists(monitorDir)) {
			try {
				Files.createDirectory(monitorDir);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		if (isActive()) {
			thread.close();
			thread = null;
		}

		if (newstate == TASstate.RECORDING) {
			onRecordingClear();
			if (!Files.exists(monitorDir)) {
				try {
					Files.createDirectory(monitorDir);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			try {
				thread = new FileThread(monitorDir.resolve("ktrng_recording.txt"), false);
				thread.start();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else if (newstate == TASstate.PLAYBACK) {

			Path playbackFile = monitorDir.resolve("ktrng_playback.txt");

			if (Files.exists(playbackFile)) {
				int i = 0;
				do {
					i++;
					playbackFile = monitorDir.resolve(String.format("ktrng_playback%s.txt", i));
				} while (Files.exists(playbackFile));
			}

			try {
				thread = new FileThread(playbackFile, false);
				thread.start();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void onRecordingClear() {
		if (!isEnabled()) {
			return;
		}
		Path serverDirs = TASmod.getServerInstance().getDataDirectory().toPath();
		Path monitorDir = serverDirs.resolve("ktrngMonitor");
		SavestateIndexer.deleteFolder(monitorDir);
	}

	@Override
	public void onRNGCall(RNGSide side, String eventType, long seed, String value, String rngClass, int offset) {

		if (!isActive() || !isEnabled()) {
			return;
		}

		StackTraceElement[] stackTraceElements = null;
		stackTraceElements = Thread.currentThread().getStackTrace();

		List<String> classOut = new ArrayList<>();
		if (stackTraceElements != null && stackTraceElements.length != 0) {
			int start = offset;
			for (int i = start; i < stackTraceElements.length; i++) {
				String out = formatStackTraceElement(stackTraceElements[i]);
				if (out != null)
					classOut.add(out);
			}
		}
		String out = String.format("%s %s %s\t%s\t%s", eventType, seed, value, rngClass, String.join(", ", classOut));
		writeDebug(out);
	}

	private FileThread thread = null;

	public void writeDebug(String out) {
		if (isEnabled() && isActive()) {
			thread.addLine(out);
		}
	}

	private boolean isEnabled() {
		return System.getProperty("tasmod.killtherng.trace", "false").equals("true");
	}

	public boolean isActive() {
		return thread != null;
	}

	private String formatStackTraceElement(StackTraceElement stackTraceElement) {
		String methodName = stackTraceElement.getMethodName();
		String[] classNames = stackTraceElement.getClassName().split("\\.");
		String className = classNames[classNames.length - 1];
		if (methodName.equals("showBarrierParticles"))
			return null;
		String classOut = className + "." + methodName +
				(stackTraceElement.isNativeMethod() ? "(Native Method)" : (stackTraceElement.getFileName() != null && stackTraceElement.getLineNumber() >= 0 ? "(" + stackTraceElement.getFileName() + ":" + stackTraceElement.getLineNumber()
						+ ")" : (stackTraceElement.getFileName() != null ? "(" + stackTraceElement.getFileName() + ")" : "(Unknown Source)")));
		return classOut;
	}
}
