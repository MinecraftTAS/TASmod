package com.minecrafttas.tasmod.util;

import static com.minecrafttas.tasmod.registries.TASmodPackets.COMMAND_FLAVORLIST;
import static com.minecrafttas.tasmod.registries.TASmodPackets.COMMAND_TASFILELIST;

import java.io.File;
import java.io.FileFilter;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.minecrafttas.tasmod.mctcommon.networking.exception.PacketNotImplementedException;
import com.minecrafttas.tasmod.mctcommon.networking.exception.WrongSideException;
import com.minecrafttas.tasmod.mctcommon.networking.interfaces.ClientPacketHandler;
import com.minecrafttas.tasmod.mctcommon.networking.interfaces.PacketID;
import com.minecrafttas.tasmod.mctcommon.networking.interfaces.ServerPacketHandler;
import com.minecrafttas.tasmod.TASmod;
import com.minecrafttas.tasmod.TASmodClient;
import com.minecrafttas.tasmod.networking.TASmodBufferBuilder;
import com.minecrafttas.tasmod.registries.TASmodAPIRegistry;
import com.minecrafttas.tasmod.registries.TASmodPackets;

import net.minecraft.client.Minecraft;

public class TabCompletionUtils implements ServerPacketHandler, ClientPacketHandler {

	private volatile CompletableFuture<List<String>> fileList = null;
	private volatile CompletableFuture<List<String>> flavorList = null;

	@Override
	public PacketID[] getAcceptedPacketIDs() {
		return new PacketID[] { COMMAND_TASFILELIST, COMMAND_FLAVORLIST };
	}

	//======== SERVER SIDE

	@Override
	public void onServerPacket(PacketID id, ByteBuffer buf, String username) throws PacketNotImplementedException, WrongSideException, Exception {
		TASmodPackets packet = (TASmodPackets) id;
		switch (packet) {
			case COMMAND_TASFILELIST:
				String filenames = TASmodBufferBuilder.readString(buf);
				fileList.complete(Arrays.asList(filenames.split("\\|")));
				break;
			case COMMAND_FLAVORLIST:
				String flavornames = TASmodBufferBuilder.readString(buf);
				flavorList.complete(Arrays.asList(flavornames.split("\\|")));
				break;
			default:
				break;
		}
	}

	public List<String> getTASfileList(String playername) throws InterruptedException, ExecutionException, TimeoutException {
		fileList = new CompletableFuture<>();
		try {
			TASmod.server.sendTo(playername, new TASmodBufferBuilder(COMMAND_TASFILELIST));
		} catch (Exception e) {
			TASmod.LOGGER.catching(e);
		}
		return fileList.get(2, TimeUnit.SECONDS);
	}

	public List<String> getFlavorList(String playername) throws InterruptedException, ExecutionException, TimeoutException {
		flavorList = new CompletableFuture<>();
		try {
			TASmod.server.sendTo(playername, new TASmodBufferBuilder(COMMAND_FLAVORLIST));
		} catch (Exception e) {
			TASmod.LOGGER.catching(e);
		}
		return flavorList.get(2, TimeUnit.SECONDS);
	}

	//======== CLIENT SIDE

	@Override
	public void onClientPacket(PacketID id, ByteBuffer buf, String username) throws PacketNotImplementedException, WrongSideException, Exception {
		TASmodPackets packet = (TASmodPackets) id;
		switch (packet) {
			case COMMAND_TASFILELIST:
				String filenames = String.join("|", getFilenames());
				TASmodClient.client.send(new TASmodBufferBuilder(COMMAND_TASFILELIST).writeString(filenames));
				break;

			case COMMAND_FLAVORLIST:
				String flavornames = String.join("|", TASmodAPIRegistry.SERIALISER_FLAVOR.getFlavorNames());
				TASmodClient.client.send(new TASmodBufferBuilder(COMMAND_FLAVORLIST).writeString(flavornames));
				break;

			default:
				break;
		}
	}

	private List<String> getFilenames() {
		List<String> tab = new ArrayList<String>();
		File folder = new File(Minecraft.getInstance().gameDirectory, "saves" + File.separator + "tasfiles");

		File[] listOfFiles = folder.listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				return pathname.getName().endsWith(".mctas");
			}
		});
		for (int i = 0; i < listOfFiles.length; i++) {
			tab.add(listOfFiles[i].getName().replaceFirst("\\.mctas$", ""));
		}
		return tab;
	}
}

