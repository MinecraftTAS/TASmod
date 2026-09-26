package com.minecrafttas.tasmod.networking;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import com.minecrafttas.tasmod.mctcommon.networking.ByteBufferBuilder;
import com.minecrafttas.tasmod.mctcommon.networking.interfaces.PacketID;
import com.minecrafttas.tasmod.savestates.storage.builtin.ClientMotionStorage.MotionData;

import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;

public class TASmodBufferBuilder extends ByteBufferBuilder {

	public TASmodBufferBuilder(int id) {
		super(id);
	}

	public TASmodBufferBuilder(PacketID packet) {
		super(packet);
	}

	public TASmodBufferBuilder(ByteBuffer buf) {
		super(buf);
	}

	public TASmodBufferBuilder writeNBTTagCompound(CompoundTag compound) {

		ByteArrayOutputStream out = new ByteArrayOutputStream();

		DataOutputStream dataout = new DataOutputStream(out);

		try {
			NbtIo.writeCompressed(compound, dataout);
		} catch (IOException e) {
			e.printStackTrace();
		}

		this.writeByteArray(out.toByteArray());

		try {
			out.close();
			dataout.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return this;
	}

	public TASmodBufferBuilder writeMotionData(MotionData data) {
		writeDouble(data.getClientX());
		writeDouble(data.getClientY());
		writeDouble(data.getClientZ());
		writeFloat(data.getClientrX());
		writeFloat(data.getClientrY());
		writeFloat(data.getClientrZ());
		writeFloat(data.getJumpMovementVector());
		writeBoolean(data.isSprinting());
		return this;
	}

	public static CompoundTag readNBTTagCompound(ByteBuffer buf) throws IOException {
		ByteArrayInputStream input = new ByteArrayInputStream(readByteArray(buf));

		DataInputStream datain = new DataInputStream(input);

		CompoundTag compound = NbtIo.readCompressed(datain, new NbtAccounter(1024L * 1024 * 1024, 0));

		input.close();
		datain.close();

		return compound;
	}

	public static MotionData readMotionData(ByteBuffer buf) {
		double x = TASmodBufferBuilder.readDouble(buf);
		double y = TASmodBufferBuilder.readDouble(buf);
		double z = TASmodBufferBuilder.readDouble(buf);
		float rx = TASmodBufferBuilder.readFloat(buf);
		float ry = TASmodBufferBuilder.readFloat(buf);
		float rz = TASmodBufferBuilder.readFloat(buf);
		float jumpMovementVector = TASmodBufferBuilder.readFloat(buf);
		boolean sprinting = TASmodBufferBuilder.readBoolean(buf);

		return new MotionData(x, y, z, rx, ry, rz, sprinting, jumpMovementVector);
	}

}

