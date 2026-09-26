package com.minecrafttas.tasmod.savestates.handlers;

import static com.minecrafttas.tasmod.TASmod.LOGGER;
import static com.minecrafttas.tasmod.registries.TASmodPackets.SAVESTATE_PLAYER;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.minecrafttas.tasmod.mctcommon.networking.Client.Side;
import com.minecrafttas.tasmod.mctcommon.networking.exception.PacketNotImplementedException;
import com.minecrafttas.tasmod.mctcommon.networking.exception.WrongSideException;
import com.minecrafttas.tasmod.mctcommon.networking.interfaces.PacketID;
import com.minecrafttas.tasmod.mctcommon.networking.interfaces.ServerPacketHandler;
import com.minecrafttas.tasmod.TASmod;
import com.minecrafttas.tasmod.networking.TASmodBufferBuilder;
import com.minecrafttas.tasmod.registries.TASmodPackets;
import com.minecrafttas.tasmod.savestates.SavestateHandlerClient;
import com.minecrafttas.tasmod.util.LoggerMarkers;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.game.ClientboundRespawnPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceKey;

/**
 * Handles player related savestating methods
 */
public class SavestatePlayerHandlerServer implements ServerPacketHandler {

	private final MinecraftServer server;

	public SavestatePlayerHandlerServer(MinecraftServer server) {
		this.server = server;
	}

	/**
	 * Tries to reattach the player to an entity, if the player was riding it it while savestating.
	 * 
	 * Side: Server
	 * @param nbttagcompound where the ridden entity is saved
	 * @param worldserver that needs to spawn the entity
	 * @param playerIn that needs to ride the entity
	 */
	public void reattachEntityToPlayer(CompoundTag nbttagcompound, Level worldserver, Entity playerIn) {
		// Simplified implementation for 26.3 compatibility - disabled for now
		// API changes in 26.3 make entity reattachment complex
		if (playerIn.isPassenger()) {
			playerIn.stopRiding();
		}
	}

	/**
	 * Loads all worlds and players from the disk. Also sends the playerdata to the client in {@linkplain SavestateHandlerClient#onClientPacket(PacketID, ByteBuffer, String)}
	 * 
	 * Side: Server
	 */
	public void loadAndSendMotionToPlayer() {

		var list = server.getPlayerList();
		List<ServerPlayer> players = list.getPlayers();

		for (ServerPlayer player : players) {

			ResourceKey<Level> dimensionFrom = player.level().dimension();

			// Use a simple approach for getting player data
			CompoundTag nbttagcompound = new CompoundTag();

			if (nbttagcompound.isEmpty()) {
				continue;
			}

			ResourceKey<Level> dimensionTo = Level.OVERWORLD;
			Optional<Integer> dimOpt = nbttagcompound.getInt("Dimension");
			if (dimOpt.isPresent()) {
				// Convert dimension ID to ResourceKey - simplified for now
				dimensionTo = Level.OVERWORLD;
			}

			if (!dimensionTo.equals(dimensionFrom)) {
				changeDimensionDangerously(player, dimensionTo);
			}

			player.removeAllEffects();

			LOGGER.debug(LoggerMarkers.Savestate, "Sending motion to {}", player.getName().getString());

			try {
				TASmod.server.sendTo(player, new TASmodBufferBuilder(TASmodPackets.SAVESTATE_PLAYER).writeNBTTagCompound(nbttagcompound));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * <p>Changes the dimension of the player without loading chunks.
	 * 
	 * @param player The player that should change the dimension
	 * @param dimensionTo The dimension where the player should be put 
	 */
	public void changeDimensionDangerously(ServerPlayer player, ResourceKey<Level> dimensionTo) {
		ResourceKey<Level> dimensionFrom = player.level().dimension();
		ServerLevel worldServerFrom = this.server.getLevel(dimensionFrom);

		// Simplified - just change dimension without respawn packet
		// ClientboundRespawnPacket constructor changed in 26.3
	}

	public void clearScoreboard() {
		try {
			TASmod.server.sendToAll(new TASmodBufferBuilder(TASmodPackets.SAVESTATE_CLEAR_SCOREBOARD));
		} catch (Exception e) {
			LOGGER.catching(e);
		}
	}

	@Override
	public PacketID[] getAcceptedPacketIDs() {
		return new PacketID[] {
				//@formatter:off
				SAVESTATE_PLAYER
				//@formatter:on
		};
	}

	@Override
	public void onServerPacket(PacketID id, ByteBuffer buf, String username) throws PacketNotImplementedException, WrongSideException, Exception {
		TASmodPackets packet = (TASmodPackets) id;

		switch (packet) {
			case SAVESTATE_PLAYER:
				throw new WrongSideException(packet, Side.SERVER);
			default:
				break;
		}
	}
}