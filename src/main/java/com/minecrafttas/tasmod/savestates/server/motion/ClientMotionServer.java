package com.minecrafttas.tasmod.savestates.server.motion;

import com.google.common.collect.Maps;
import com.minecrafttas.server.SecureList;
import com.minecrafttas.tasmod.TASmod;
import com.minecrafttas.tasmod.util.LoggerMarkers;
import lombok.var;
import net.minecraft.entity.player.EntityPlayerMP;

import java.util.Map;

public class ClientMotionServer {

	private static Map<EntityPlayerMP, Saver> motion = Maps.<EntityPlayerMP, Saver>newHashMap();

	public static Map<EntityPlayerMP, Saver> getMotion() {
		return motion;
	}

	public static void requestMotionFromClient() {
		TASmod.LOGGER.trace(LoggerMarkers.Savestate, "Request motion from client");
		motion.clear();
		try {
			// packet 14: request client motion
			var bufIndex = SecureList.POOL.available();
			TASmod.server.writeAll(SecureList.POOL.lock(bufIndex).putInt(14));
		} catch (Exception e) {
			TASmod.LOGGER.error("Unable to send packet to all clients:", e);
		}

		// TODO: is this still necessary?
		int i = 1;
		while (motion.size() != TASmod.getServerInstance().getPlayerList().getCurrentPlayerCount()) {
			i++;
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			if(i % 30 == 1) {
				TASmod.LOGGER.debug(LoggerMarkers.Savestate, "Resending motion packet");
				try {
					// packet 14: request client motion
					var bufIndex = SecureList.POOL.available();
					TASmod.server.writeAll(SecureList.POOL.lock(bufIndex).putInt(14));
				} catch (Exception e) {
					TASmod.LOGGER.error("Unable to send packet to all clients:", e);
				}
			}
			if (i == 1000) {
				TASmod.LOGGER.warn(LoggerMarkers.Savestate, "Client motion timed out!");
				break;
			}

		}
	}

	// ===========================================================

	public static class Saver {
		private double clientX;
		private double clientY;
		private double clientZ;
		private float clientrX;
		private float clientrY;
		private float clientrZ;
		private boolean sprinting;
		private float jumpMovementVector;

		public Saver(double x, double y, double z, float rx, float ry, float rz, boolean sprinting, float jumpMovementVector) {
			clientX = x;
			clientY = y;
			clientZ = z;
			clientrX = rx;
			clientrY = ry;
			clientrZ = rz;
			this.sprinting = sprinting;
			this.jumpMovementVector = jumpMovementVector;
		}

		public double getClientX() {
			return clientX;
		}

		public double getClientY() {
			return clientY;
		}

		public double getClientZ() {
			return clientZ;
		}

		public float getClientrX() {
			return clientrX;
		}

		public float getClientrY() {
			return clientrY;
		}

		public float getClientrZ() {
			return clientrZ;
		}

		public boolean isSprinting() {
			return sprinting;
		}

		public float getJumpMovementVector() {
			return jumpMovementVector;
		}
	}
}
