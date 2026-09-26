package com.minecrafttas.tasmod.playback.metadata.builtin;

import static com.minecrafttas.tasmod.TASmod.LOGGER;

import java.nio.ByteBuffer;

import com.minecrafttas.tasmod.mctcommon.networking.exception.PacketNotImplementedException;
import com.minecrafttas.tasmod.mctcommon.networking.exception.WrongSideException;
import com.minecrafttas.tasmod.mctcommon.networking.interfaces.PacketID;
import com.minecrafttas.tasmod.mctcommon.networking.interfaces.ServerPacketHandler;
import com.minecrafttas.tasmod.TASmod;
import com.minecrafttas.tasmod.TASmodClient;
import com.minecrafttas.tasmod.events.EventPlaybackClient.EventControllerStateChange;
import com.minecrafttas.tasmod.networking.TASmodBufferBuilder;
import com.minecrafttas.tasmod.playback.PlaybackControllerClient.TASstate;
import com.minecrafttas.tasmod.playback.metadata.PlaybackMetadata;
import com.minecrafttas.tasmod.playback.metadata.PlaybackMetadata.PlaybackMetadataExtension;
import com.minecrafttas.tasmod.playback.tasfile.exception.PlaybackLoadException;
import com.minecrafttas.tasmod.registries.TASmodPackets;
import com.minecrafttas.tasmod.util.LoggerMarkers;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.server.level.ServerPlayer;

/**
 * Adds a "start position" entry in the playback metadata.<br>
 * <br>
 * Records the position of the player when starting a recording,<br>
 * and teleports the player, at the start of the playback.
 * 
 * @author Scribble
 */
public class StartpositionMetadataExtension extends PlaybackMetadataExtension implements EventControllerStateChange, ServerPacketHandler {

	/**
	 * The startposition of the playback
	 */
	protected StartPosition startPosition = null;

	public static class StartPosition {

		public final double x;
		public final double y;
		public final double z;
		public final float pitch;
		public final float yaw;

		public StartPosition(double x, double y, double z, float pitch, float yaw) {
			this.x = x;
			this.y = y;
			this.z = z;
			this.pitch = pitch;
			this.yaw = yaw;
		}

		@Override
		public String toString() {
			return String.format("%e,%e,%e,%e,%e", x, y, z, pitch, yaw);
		}
	}

	@Override
	public String getExtensionName() {
		return "Start Position";
	}

	@Override
	public void onCreate() {
		// Unused atm
	}

	public enum StartPositionFields {
		X("x"),
		Y("y"),
		Z("z"),
		Pitch("pitch"),
		Yaw("yaw");

		private final String name;

		private StartPositionFields(String name) {
			this.name = name;
		}

		@Override
		public String toString() {
			return name;
		}
	}

	@Override
	public PlaybackMetadata onStore() {
		PlaybackMetadata metadata = new PlaybackMetadata(this);

		StartPosition startPositionToStore = new StartPosition(0, 0, 0, 0, 0);
		if (this.startPosition != null) {
			startPositionToStore = startPosition;
		}
		metadata.setValue(StartPositionFields.X, Double.toString(startPositionToStore.x));
		metadata.setValue(StartPositionFields.Y, Double.toString(startPositionToStore.y));
		metadata.setValue(StartPositionFields.Z, Double.toString(startPositionToStore.z));
		metadata.setValue(StartPositionFields.Pitch, Float.toString(startPositionToStore.pitch));
		metadata.setValue(StartPositionFields.Yaw, Float.toString(startPositionToStore.yaw));
		return metadata;
	}

	@Override
	public void onLoad(PlaybackMetadata metadata) {
		double x = getDouble(StartPositionFields.X, metadata);
		double y = getDouble(StartPositionFields.Y, metadata);
		double z = getDouble(StartPositionFields.Z, metadata);
		float pitch = getFloat(StartPositionFields.Pitch, metadata);
		float yaw = getFloat(StartPositionFields.Yaw, metadata);

		this.startPosition = new StartPosition(x, y, z, pitch, yaw);
	}

	private double getDouble(Object key, PlaybackMetadata metadata) {
		return getDouble(key.toString(), metadata);
	}

	private double getDouble(String key, PlaybackMetadata metadata) {
		String out = metadata.getValue(key);
		if (out != null) {
			try {
				return Double.parseDouble(out);
			} catch (NumberFormatException e) {
				throw new PlaybackLoadException(e);
			}
		} else {
			throw new PlaybackLoadException(String.format("Missing key %s in Start Position metadata", key));
		}
	}

	private float getFloat(Object key, PlaybackMetadata metadata) {
		return getFloat(key.toString(), metadata);
	}

	private float getFloat(String key, PlaybackMetadata metadata) {
		String out = metadata.getValue(key);
		if (out != null) {
			try {
				return Float.parseFloat(out);
			} catch (NumberFormatException e) {
				throw new PlaybackLoadException(e);
			}
		} else {
			throw new PlaybackLoadException(String.format("Missing key %s in Start Position metadata", key));
		}
	}

	@Override
	public void onClear() {
		startPosition = null;
	}

	@Override
	public void onControllerStateChange(TASstate newstate, TASstate oldstate) {
		Minecraft mc = Minecraft.getInstance();

		if (mc.player != null) {
			if (oldstate == TASstate.NONE && newstate == TASstate.RECORDING && startPosition == null) { // If a recording is started, the player is in a world and startposition is uninitialized
				updateStartPosition();

			} else if (oldstate == TASstate.NONE && newstate == TASstate.PLAYBACK && startPosition != null) {
				LOGGER.debug(LoggerMarkers.Playback, "Teleporting the player to the start location");
				TASmodBufferBuilder packetBuilder = new TASmodBufferBuilder(TASmodPackets.PLAYBACK_TELEPORT);

				packetBuilder.writeDouble(startPosition.x).writeDouble(startPosition.y).writeDouble(startPosition.z).writeFloat(startPosition.pitch).writeFloat(startPosition.yaw);

				try {
					TASmodClient.client.send(packetBuilder);
				} catch (Exception e) {
					LOGGER.error("Unable to teleport player to start location", e);
				}
			}
		}
	}

	public void updateStartPosition() {
		LOGGER.debug(LoggerMarkers.Playback, "Setting start location");
		Minecraft mc = Minecraft.getInstance();
		LocalPlayer player = mc.player;
		if (player != null)
			startPosition = new StartPosition(player.getX(), player.getY(), player.getZ(), player.getXRot(), player.getYRot());
		else
			LOGGER.warn("Start position not set, the player was null! This will create problems when storing inputs!");

	}

	@Override
	public PacketID[] getAcceptedPacketIDs() {
		return new PacketID[] { TASmodPackets.PLAYBACK_TELEPORT };
	}

	@Override
	public void onServerPacket(PacketID id, ByteBuffer buf, String username) throws PacketNotImplementedException, WrongSideException, Exception {
		TASmodPackets packet = (TASmodPackets) id;

		if (packet == TASmodPackets.PLAYBACK_TELEPORT) {
			double x = TASmodBufferBuilder.readDouble(buf);
			double y = TASmodBufferBuilder.readDouble(buf);
			double z = TASmodBufferBuilder.readDouble(buf);
			float angleYaw = TASmodBufferBuilder.readFloat(buf);
			float anglePitch = TASmodBufferBuilder.readFloat(buf);

			ServerPlayer player = TASmod.getServerInstance().getPlayerList().getPlayerByName(username);
			player.level().getServer().execute(() -> {
				player.setXRot(anglePitch);
				player.setYRot(angleYaw);
				player.teleportTo(x, y, z);
			});
		}
	}
}

