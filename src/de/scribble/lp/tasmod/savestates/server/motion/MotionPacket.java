package de.scribble.lp.tasmod.savestates.server.motion;

import de.scribble.lp.tasmod.savestates.server.motion.ClientMotionServer.Saver;
import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MotionPacket implements IMessage {
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
	public void fromBytes(ByteBuf buf) {
		x = buf.readDouble();
		y = buf.readDouble();
		z = buf.readDouble();
		rx = buf.readFloat();
		ry = buf.readFloat();
		rz = buf.readFloat();
		sprinting = buf.readBoolean();
		jumpMovementVector = buf.readFloat();
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeDouble(x);
		buf.writeDouble(y);
		buf.writeDouble(z);
		buf.writeFloat(rx);
		buf.writeFloat(ry);
		buf.writeFloat(rz);
		buf.writeBoolean(sprinting);
		buf.writeFloat(jumpMovementVector);
	}

	public double getX() {
		return x;
	}

	public double getY() {
		return y;
	}

	public double getZ() {
		return z;
	}

	public float getRx() {
		return rx;
	}

	public float getRy() {
		return ry;
	}

	public float getRz() {
		return rz;
	}

	public boolean isSprinting() {
		return sprinting;
	}

	public float getJumpMovementVector() {
		return jumpMovementVector;
	}

	public static class MotionPacketHandler implements IMessageHandler<MotionPacket, IMessage> {

		@Override
		public IMessage onMessage(MotionPacket message, MessageContext ctx) {
			if (ctx.side.isServer()) {
				ClientMotionServer.getMotion().put(ctx.getServerHandler().player, new Saver(message.getX(), message.getY(), message.getZ(), message.getRx(), message.getRy(), message.getRz(), message.isSprinting(), message.getJumpMovementVector()));
			}
			return null;
		}

	}
}
