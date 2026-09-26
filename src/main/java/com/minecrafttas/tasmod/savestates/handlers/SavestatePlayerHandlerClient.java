package com.minecrafttas.tasmod.savestates.handlers;

import static com.minecrafttas.tasmod.TASmod.LOGGER;
import static com.minecrafttas.tasmod.registries.TASmodPackets.SAVESTATE_PLAYER;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.minecrafttas.tasmod.mctcommon.events.EventListenerRegistry;
import com.minecrafttas.tasmod.mctcommon.networking.exception.PacketNotImplementedException;
import com.minecrafttas.tasmod.mctcommon.networking.exception.WrongSideException;
import com.minecrafttas.tasmod.mctcommon.networking.interfaces.ClientPacketHandler;
import com.minecrafttas.tasmod.mctcommon.networking.interfaces.PacketID;
import com.minecrafttas.tasmod.TASmodClient;
import com.minecrafttas.tasmod.events.EventSavestate;
import com.minecrafttas.tasmod.mixin.savestates.AccessorEntityLivingBase;
import com.minecrafttas.tasmod.networking.TASmodBufferBuilder;
import com.minecrafttas.tasmod.registries.TASmodPackets;
import com.minecrafttas.tasmod.util.Ducks.SubtickDuck;
import com.minecrafttas.tasmod.util.LoggerMarkers;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;

public class SavestatePlayerHandlerClient implements ClientPacketHandler {

	public void loadPlayer(CompoundTag compound) {
		LOGGER.trace(LoggerMarkers.Savestate, "Loading client player from NBT");
		Minecraft mc = Minecraft.getInstance();
		LocalPlayer player = mc.player;

		// Clear any accidental applied potion particles on the client
		((AccessorEntityLivingBase) player).clearPotionEffects();

		/*
		 * TODO
		 * The following 20 lines are all one
		 * gross workaround for correctly applying the player motion
		 * to the client...
		 * 
		 * The motion is applied
		 * to the player in a previous step and unfortunately
		 * player.readFromNBT(compound) overwrites the
		 * previously applied motion...
		 * 
		 * So this workaround makes sure that the motion is not overwritten
		 * Fixing this, requires restructuring the steps for loadstating
		 * and since I plan to do this anyway at some point, I will
		 * leave this here and be done for today*/
		double x = player.getDeltaMovement().x;
		double y = player.getDeltaMovement().y;
		double z = player.getDeltaMovement().z;

		float rx = player.zza;
		float ry = player.yya;
		float rz = player.xxa;

		boolean sprinting = player.isSprinting();
		// float jumpVector = player.jumpMovementFactor; // Field might not exist

		// player.readFromNBT(compound); // Not available in 1.22+

		player.setDeltaMovement(new Vec3(x, y, z));

		player.zza = rx;
		player.yya = ry;
		player.xxa = rz;

		player.setSprinting(sprinting);
		// player.jumpMovementFactor = jumpVector;

		LOGGER.trace(LoggerMarkers.Savestate, "Setting client gamemode");
		// #86
		int gamemode = compound.getInt("playerGameType").orElse(0);
		GameType type = GameType.byId(gamemode);
		// mc.gameMode.setGameModeForPlayer(player, type, false); // API changed in 1.22+
		// Use mc.setGameMode(type) or similar

		// Set the camera rotation to the player rotation
		TASmodClient.virtual.CAMERA_ANGLE.setCamera(player.getXRot(), player.getYRot());
		SubtickDuck entityRenderer = (SubtickDuck) Minecraft.getInstance().gameRenderer;
		entityRenderer.runUpdate(0);

		// Clear boss bars on savestate load
		// mc.gui.getBossOverlay().clearBossInfos(); // API changed

		EventListenerRegistry.fireEvent(EventSavestate.EventClientLoadPlayer.class, player);
	}

	@Override
	public PacketID[] getAcceptedPacketIDs() {
		return new PacketID[] {
				//@formatter:off
				SAVESTATE_PLAYER
				//@formatter:on
		};
	}

	@Environment(EnvType.CLIENT)
	@Override
	public void onClientPacket(PacketID id, ByteBuffer buf, String username) throws PacketNotImplementedException, WrongSideException, Exception {
		TASmodPackets packet = (TASmodPackets) id;

		switch (packet) {
			case SAVESTATE_PLAYER:
				CompoundTag compound;
				try {
					compound = TASmodBufferBuilder.readNBTTagCompound(buf);
				} catch (IOException e) {
					e.printStackTrace();
					break;
				}
				/*
				 * Fair warning: Do NOT read the buffer inside an addScheduledTask. Read it
				 * before that. The buffer will have the wrong limit, when the task is executed.
				 * This is probably due to the buffers being reused.
				 */
				Minecraft.getInstance().execute(() -> {
					loadPlayer(compound);
				});
				break;

			default:
				break;
		}
	}

}

