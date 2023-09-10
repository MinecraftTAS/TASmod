package com.minecrafttas.tasmod.ktrng;

import static com.minecrafttas.tasmod.TASmod.LOGGER;

import java.nio.ByteBuffer;

import com.minecrafttas.common.events.EventClient.EventPlayerJoinedClientSide;
import com.minecrafttas.common.events.EventServer.EventServerTick;
import com.minecrafttas.common.server.Client.Side;
import com.minecrafttas.common.server.exception.PacketNotImplementedException;
import com.minecrafttas.common.server.exception.WrongSideException;
import com.minecrafttas.common.server.interfaces.ClientPacketHandler;
import com.minecrafttas.common.server.interfaces.PacketID;
import com.minecrafttas.common.server.interfaces.ServerPacketHandler;
import com.minecrafttas.killtherng.KillTheRNG;
import com.minecrafttas.killtherng.SeedingModes;
import com.minecrafttas.tasmod.TASmod;
import com.minecrafttas.tasmod.TASmodClient;
import com.minecrafttas.tasmod.networking.TASmodBufferBuilder;
import com.minecrafttas.tasmod.networking.TASmodPackets;
import com.minecrafttas.tasmod.playback.PlaybackControllerClient.TASstate;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.server.MinecraftServer;

/**
 * Easy access to the KillTheRNG library without littering the rest of the code
 * 
 * @author Scribble
 *
 */
public class KillTheRNGHandler implements EventServerTick, EventPlayerJoinedClientSide, ClientPacketHandler, ServerPacketHandler {

	private boolean isLoaded;

	/**
	 * Instantiates a KillTheRNGHandler instance
	 * 
	 * @param isLoaded If the KillTheRNG mod is loaded
	 */
	public KillTheRNGHandler(boolean isLoaded) {

		this.isLoaded = isLoaded;

		if (isLoaded) {
			KillTheRNG.LOGGER.info("Connection established with TASmod");
			KillTheRNG.isLibrary = true;
			KillTheRNG.mode = SeedingModes.TickChange;

			KillTheRNG.annotations.register(new KTRNGMonitor());
		} else {
			LOGGER.info("KillTheRNG doesn't appear to be loaded");
		}
	}

	public long advanceGlobalSeedServer() {
		if (isLoaded()) {
			return KillTheRNG.commonRandom.nextSeed();
		} else {
			return 0;
		}
	}

	public long getGlobalSeedServer() {
		if (isLoaded()) {
			return KillTheRNG.commonRandom.GlobalServer.getSeed();
		} else {
			return 0;
		}
	}

	public boolean isLoaded() {
		return isLoaded;
	}

	// =================================================Setting the seed
	/**
	 * @return The global seed of the client
	 */
	@Environment(EnvType.CLIENT)
	public long getGlobalSeedClient() {
		if (isLoaded())
			return KillTheRNG.clientRandom.GlobalClient.getSeed();
		else
			return 0;
	}

	/**
	 * Set the global seed on the client
	 * 
	 * @param seedIn The seed on the client
	 */
	@Environment(EnvType.CLIENT)
	public void setGlobalSeedClient(long seedIn) {
		if (isLoaded()) {
			KillTheRNG.clientRandom.setSeedAll(seedIn);
		}
	}

	public void setGlobalSeedServer(long seedIn) {
		if (isLoaded()) {
			KillTheRNG.commonRandom.setSeedAll(seedIn);
		}
	}

	/**
	 * Sends a packet to the server, setting the global seed
	 * 
	 * @param seedIn The seed on the server
	 */
	@Environment(EnvType.CLIENT)
	public void sendGlobalSeedToServer(long seedIn) {
		if (isLoaded()) {
			if (TASmodClient.client != null)
				try {
					TASmodClient.client.send(new TASmodBufferBuilder(TASmodPackets.KILLTHERNG_SEED).writeLong(seedIn));
				} catch (Exception e) {
					e.printStackTrace();
				}
			else
				setGlobalSeedClient(seedIn);
		}
	}
	// =================================================TASmod integration

	/**
	 * Executed every tick on the server
	 */
	@Override
	public void onServerTick(MinecraftServer server) {
		if (isLoaded()) {
			if (TASmod.playbackControllerServer.getState() != TASstate.PAUSED)
				try {
					TASmod.server.sendToAll(new TASmodBufferBuilder(TASmodPackets.KILLTHERNG_SEED).writeLong(advanceGlobalSeedServer()));
				} catch (Exception e) {
					e.printStackTrace();
				}
		}
	}

	// ================================================= Seedsync

	public void broadcastStartSeed() {
		if (isLoaded()) {
			long seed = getGlobalSeedServer();
			try {
				TASmod.server.sendToAll(new TASmodBufferBuilder(TASmodPackets.KILLTHERNG_SEED).writeLong(seed));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Environment(EnvType.CLIENT)
	public void setInitialSeed(long initialSeed) {
		if (TASmodClient.client != null) {
			LOGGER.info("Sending initial client seed: {}", initialSeed);
			try {
				TASmodClient.client.send(new TASmodBufferBuilder(TASmodPackets.KILLTHERNG_STARTSEED).writeLong(initialSeed)); // TODO Every new player in multiplayer will currently send the initial seed,
																																// which is BAD
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			TASmod.ktrngHandler.setGlobalSeedClient(initialSeed);
		}
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void onPlayerJoinedClientSide(EntityPlayerSP player) {
		setInitialSeed(getGlobalSeedClient());
	}

	@Override
	public PacketID[] getAcceptedPacketIDs() {
		return new TASmodPackets[] { TASmodPackets.KILLTHERNG_SEED, TASmodPackets.KILLTHERNG_STARTSEED };
	}

	@Override
	public void onServerPacket(PacketID id, ByteBuffer buf, String username) throws PacketNotImplementedException, WrongSideException, Exception {
		long seed = TASmodBufferBuilder.readLong(buf);
		TASmodPackets packet = (TASmodPackets) id;

		switch (packet) {
			case KILLTHERNG_SEED:
				setGlobalSeedServer(seed);
				break;

			case KILLTHERNG_STARTSEED:
				TASmod.tickSchedulerServer.add(() -> {
					TASmod.ktrngHandler.setGlobalSeedServer(seed);
				});
				break;

			default:
				throw new PacketNotImplementedException(packet, this.getClass(), Side.SERVER);
		}
	}

	@Override
	public void onClientPacket(PacketID id, ByteBuffer buf, String username) throws PacketNotImplementedException, WrongSideException, Exception {
		long seed = TASmodBufferBuilder.readLong(buf);
		TASmodPackets packet = (TASmodPackets) id;

		switch (packet) {
			case KILLTHERNG_SEED:
				setGlobalSeedClient(seed);
				break;

			case KILLTHERNG_STARTSEED:
				TASmodClient.virtual.getContainer().setStartSeed(seed);
				break;

			default:
				throw new PacketNotImplementedException(packet, this.getClass(), Side.CLIENT);
		}
	}

}
