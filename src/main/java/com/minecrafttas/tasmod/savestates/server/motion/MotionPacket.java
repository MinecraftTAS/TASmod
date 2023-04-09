package com.minecrafttas.tasmod.savestates.server.motion;

import com.minecrafttas.tasmod.networking.Packet;
import com.minecrafttas.tasmod.networking.PacketSide;
import com.minecrafttas.tasmod.savestates.server.motion.ClientMotionServer.Saver;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.PacketBuffer;

public class MotionPacket implements Packet {
	private double x = 0;
	private double y = 0;
	private double z = 0;
	private float rx = 0;
	private float ry = 0;
	private float rz = 0;
	private boolean sprinting;
	private float jumpMovementVector = 0.2F;

	public MotionPacket() {
	}

	public MotionPacket(double x, double y, double z, float moveForward, float moveVertical, float moveStrafe, boolean isSprinting, float jumpMovementVector) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.rx = moveForward;
		this.ry = moveVertical;
		this.rz = moveStrafe;
		sprinting = isSprinting;
		this.jumpMovementVector = jumpMovementVector;
	}

	@Override
	public void handle(PacketSide side, EntityPlayer playercommon) {
		if (side.isServer()) {
			EntityPlayerMP player = (EntityPlayerMP) playercommon;
			ClientMotionServer.getMotion().put(player, new Saver(x, y, z, rx, ry, rz, sprinting, jumpMovementVector));
		}
	}

	@Override
	public void serialize(PacketBuffer buf) {
		buf.writeDouble(x);
		buf.writeDouble(y);
		buf.writeDouble(z);
		buf.writeFloat(rx);
		buf.writeFloat(ry);
		buf.writeFloat(rz);
		buf.writeBoolean(sprinting);
		buf.writeFloat(jumpMovementVector);
	}

	@Override
	public void deserialize(PacketBuffer buf) {
		x = buf.readDouble();
		y = buf.readDouble();
		z = buf.readDouble();
		rx = buf.readFloat();
		ry = buf.readFloat();
		rz = buf.readFloat();
		sprinting = buf.readBoolean();
		jumpMovementVector = buf.readFloat();
	}
}
