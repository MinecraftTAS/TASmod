package com.minecrafttas.tasmod.networking;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import com.minecrafttas.common.server.ByteBufferBuilder;
import com.minecrafttas.common.server.interfaces.PacketID;
import com.minecrafttas.tasmod.playback.PlaybackController.TASstate;
import com.minecrafttas.tasmod.savestates.SavestateHandlerServer.PlayerHandler.MotionData;
import com.minecrafttas.tasmod.tickratechanger.TickrateChangerServer.TickratePauseState;

import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTSizeTracker;
import net.minecraft.nbt.NBTTagCompound;

public class TASmodBufferBuilder extends ByteBufferBuilder{

	public TASmodBufferBuilder(int id) {
		super(id);
	}
	
	public TASmodBufferBuilder(PacketID packet) {
		super(packet);
	}
	
	public TASmodBufferBuilder(ByteBuffer buf) {
		super(buf);
	}
	
	public TASmodBufferBuilder writeTASState(TASstate state) {
		this.writeShort((short)state.ordinal());
		return this;
	}
	
	public TASmodBufferBuilder writeNBTTagCompound(NBTTagCompound compound) {
		
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		
		DataOutputStream dataout = new DataOutputStream(out);
		
		try {
			CompressedStreamTools.write(compound, dataout);
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
	
	public TASmodBufferBuilder writeTickratePauseState(TickratePauseState state) {
		writeShort((short) state.ordinal());
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
	
	public static TASstate readTASState(ByteBuffer buf) {
		return TASstate.values()[buf.getShort()];
	}
	
	public static NBTTagCompound readNBTTagCompound(ByteBuffer buf) throws IOException {
		ByteArrayInputStream input = new ByteArrayInputStream(readByteArray(buf));
		
		DataInputStream datain = new DataInputStream(input);
		
		NBTTagCompound compound = CompressedStreamTools.read(datain, NBTSizeTracker.INFINITE);
		
		input.close();
		datain.close();
		
		return compound;
	}
	
	public static TickratePauseState readTickratePauseState(ByteBuffer buf) {
		return TickratePauseState.values()[buf.getShort()];
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
