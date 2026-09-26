package com.minecrafttas.tasmod.savestates.storage.builtin;

import static com.minecrafttas.tasmod.TASmod.LOGGER;
import static com.minecrafttas.tasmod.registries.TASmodPackets.SAVESTATE_REQUEST_MOTION;
import static com.minecrafttas.tasmod.registries.TASmodPackets.SAVESTATE_SET_MOTION;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.minecrafttas.tasmod.mctcommon.networking.Client.Side;
import com.minecrafttas.tasmod.mctcommon.networking.exception.PacketNotImplementedException;
import com.minecrafttas.tasmod.mctcommon.networking.exception.WrongSideException;
import com.minecrafttas.tasmod.mctcommon.networking.interfaces.ClientPacketHandler;
import com.minecrafttas.tasmod.mctcommon.networking.interfaces.PacketID;
import com.minecrafttas.tasmod.mctcommon.networking.interfaces.ServerPacketHandler;
import com.minecrafttas.tasmod.TASmod;
import com.minecrafttas.tasmod.TASmodClient;
import com.minecrafttas.tasmod.networking.TASmodBufferBuilder;
import com.minecrafttas.tasmod.registries.TASmodPackets;
import com.minecrafttas.tasmod.savestates.exceptions.SavestateException;
import com.minecrafttas.tasmod.savestates.storage.SavestateStorageExtensionBase;
import com.minecrafttas.tasmod.util.LoggerMarkers;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.players.PlayerList;

public class ClientMotionStorage extends SavestateStorageExtensionBase implements ClientPacketHandler, ServerPacketHandler {

	private final Map<ServerPlayer, CompletableFuture<MotionData>> futures;

	public ClientMotionStorage() {
		super("clientMotion.json");
		futures = new HashMap<>();
	}

	@Override
	public JsonObject onSavestate(MinecraftServer server, JsonObject dataToSave) {
		LOGGER.trace(LoggerMarkers.Savestate, "Request motion from client");

		this.futures.clear();

		List<ServerPlayer> playerList = server.getPlayerList().getPlayers();
		playerList.forEach(player -> {
			futures.put(player, new CompletableFuture<>());
		});

		try {
			// request client motion
			TASmod.server.sendToAll(new TASmodBufferBuilder(SAVESTATE_REQUEST_MOTION));
		} catch (Exception e) {
			e.printStackTrace();
		}

futures.forEach((player, future) -> {
			try {
				MotionData data = future.get(5L, TimeUnit.SECONDS);

				String uuid = player.getUUID().toString();
				if (!server.isDedicatedServer() && player.getName().getString().equals(server.getWorldData().getLevelName())) {
					uuid = "singleplayer";
				}
				dataToSave.add(uuid, json.toJsonTree(data));

			} catch (TimeoutException e) {
				throw new SavestateException(e, "Writing client motion for %s timed out!", player.getName().getString());
			} catch (ExecutionException | InterruptedException e) {
				throw new SavestateException(e, "Writing client motion for %s", player.getName().getString());
			}
		});

		return dataToSave;
	}

@Override
	public void onLoadstate(MinecraftServer server, JsonObject loadedData) {
		PlayerList list = server.getPlayerList();

		for (Entry<String, JsonElement> motionDataJsonElement : loadedData.entrySet()) {
			String playerUUID = motionDataJsonElement.getKey();
			MotionData motionData = json.fromJson(motionDataJsonElement.getValue(), MotionData.class);

			ServerPlayer player;
			if (playerUUID.equals("singleplayer")) {
				if (server.isDedicatedServer()) {
					continue;
				}
				String ownerName = server.getWorldData().getLevelName();
				if (ownerName == null) {
					continue;
				}
				player = list.getPlayerByName(ownerName);
			} else {
				player = list.getPlayer(UUID.fromString(playerUUID));
			}

			if (player == null) {
				continue;
			}

			try {
				TASmod.server.sendTo(player, new TASmodBufferBuilder(SAVESTATE_SET_MOTION).writeMotionData(motionData));
			} catch (Exception e) {
				logger.catching(e);
			}
		}
	}

	@Override
	public PacketID[] getAcceptedPacketIDs() {
		return new PacketID[] { SAVESTATE_REQUEST_MOTION, SAVESTATE_SET_MOTION };
	}

@Environment(EnvType.CLIENT)
	@Override
	public void onClientPacket(PacketID id, ByteBuffer buf, String username) throws PacketNotImplementedException, WrongSideException, Exception {
		TASmodPackets packet = (TASmodPackets) id;
		Minecraft mc = Minecraft.getInstance();
		LocalPlayer player = mc.player;

		switch (packet) {
			case SAVESTATE_REQUEST_MOTION:

				if (player != null) {
				//@formatter:off
				MotionData motionData = new MotionData(
						player.getDeltaMovement().x,
						player.getDeltaMovement().y,
						player.getDeltaMovement().z,
						player.zza,
						0, // moveVertical not available
						player.xxa,
						player.isSprinting(), 
						0.02f // jumpMovementFactor not available in newer mappings
						);
				//@formatter:on
					TASmodClient.client.send(new TASmodBufferBuilder(TASmodPackets.SAVESTATE_REQUEST_MOTION).writeMotionData(motionData));
				}
				break;
			case SAVESTATE_SET_MOTION:
				LOGGER.trace(LoggerMarkers.Savestate, "Loading client motion");

				MotionData data = TASmodBufferBuilder.readMotionData(buf);
				player.setDeltaMovement(data.motionX, data.motionY, data.motionZ);

				player.zza = data.deltaX;
				// player.moveVertical = data.deltaY; // Not available
				player.xxa = data.deltaZ;

				player.setSprinting(data.sprinting);
				// player.jumpMovementFactor = data.jumpMovementFactor; // Not available in newer mappings
				break;
			default:
				break;
		}
	}

@Override
	public void onServerPacket(PacketID id, ByteBuffer buf, String username) throws PacketNotImplementedException, WrongSideException, Exception {
		TASmodPackets packet = (TASmodPackets) id;
		ServerPlayer player = TASmod.getServerInstance().getPlayerList().getPlayerByName(username);

		switch (packet) {
			case SAVESTATE_REQUEST_MOTION:
				MotionData data = TASmodBufferBuilder.readMotionData(buf);
				CompletableFuture<MotionData> future = this.futures.get(player);
				future.complete(data);
				break;
			case SAVESTATE_SET_MOTION:
				throw new WrongSideException(packet, Side.SERVER);
			default:
				break;
		}
	}

	public static class MotionData {

		private double motionX;
		private double motionY;
		private double motionZ;
		private float deltaX;
		private float deltaY;
		private float deltaZ;
		private boolean sprinting;
		private float jumpMovementFactor;

		public MotionData(double x, double y, double z, float rx, float ry, float rz, boolean sprinting, float jumpMovementVector) {
			motionX = x;
			motionY = y;
			motionZ = z;
			deltaX = rx;
			deltaY = ry;
			deltaZ = rz;
			this.sprinting = sprinting;
			this.jumpMovementFactor = jumpMovementVector;
		}

		public MotionData() {
			this(0D, 0D, 0D, 0f, 0f, 0f, false, 0f);
		}

		public double getClientX() {
			return motionX;
		}

		public double getClientY() {
			return motionY;
		}

		public double getClientZ() {
			return motionZ;
		}

		public float getClientrX() {
			return deltaX;
		}

		public float getClientrY() {
			return deltaY;
		}

		public float getClientrZ() {
			return deltaZ;
		}

		public boolean isSprinting() {
			return sprinting;
		}

		public float getJumpMovementVector() {
			return jumpMovementFactor;
		}
	}

	@Override
	public String getExtensionName() {
		return "ClientMotionStorage";
	}
}

