package de.scribble.lp.tasmod.savestates.server.motion;

import java.util.Map;

import com.google.common.collect.Maps;

import de.scribble.lp.tasmod.CommonProxy;
import de.scribble.lp.tasmod.TASmod;
import net.minecraft.entity.player.EntityPlayerMP;

public class ClientMotionServer {

	private static Map<EntityPlayerMP, Saver> motion = Maps.<EntityPlayerMP, Saver>newHashMap();

	public static Map<EntityPlayerMP, Saver> getMotion() {
		return motion;
	}

	public static void requestMotionFromClient() {
		motion.clear();
		CommonProxy.NETWORK.sendToAll(new RequestMotionPacket());

		int i = 0;
		while (motion.size() != TASmod.getServerInstance().getPlayerList().getCurrentPlayerCount()) {
			i++;
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			if (i == 3000) {
				CommonProxy.logger.warn("Client motion timed out!");
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
