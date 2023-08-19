package com.minecrafttas.tasmod.networking;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.nio.ByteBuffer;

import com.minecrafttas.common.server.ByteBufferBuilder;
import com.minecrafttas.common.server.interfaces.PacketID;
import com.minecrafttas.tasmod.playback.PlaybackController.TASstate;
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
	
	public TASmodBufferBuilder writeTASState(TASstate state) {
		this.writeShort((short)state.ordinal());
		return this;
	}
	
	public TASmodBufferBuilder writeNBTTagCompound(NBTTagCompound compound) {
		DataOutput out = new DataOutput() {
			
			@Override
			public void writeUTF(String s) throws IOException {
				writeString(s);
			}
			
			@Override
			public void writeShort(int v) throws IOException {
				buffer.putShort((short) v);
			}
			
			@Override
			public void writeLong(long v) throws IOException {
				buffer.putLong(v);
			}
			
			@Override
			public void writeInt(int v) throws IOException {
				buffer.putInt(v);
			}
			
			@Override
			public void writeFloat(float v) throws IOException {
				buffer.putFloat(v);
			}
			
			@Override
			public void writeDouble(double v) throws IOException {
				buffer.putDouble(v);
			}
			
			@Override
			public void writeChars(String s) throws IOException {
				writeString(s);
			}
			
			@Override
			public void writeChar(int v) throws IOException {
				buffer.putChar((char) v);
			}
			
			@Override
			public void writeBytes(String s) throws IOException {
				writeString(s);
			}
			
			@Override
			public void writeByte(int v) throws IOException {
				buffer.put((byte) v);
			}
			
			@Override
			public void writeBoolean(boolean v) throws IOException {
				writeBoolean(v);
			}
			
			@Override
			public void write(byte[] b, int off, int len) throws IOException {
				throw new IOException("Not implemented");
			}
			
			@Override
			public void write(byte[] b) throws IOException {
				buffer.put(b);
			}
			
			@Override
			public void write(int b) throws IOException {
				buffer.put((byte) b);
			}
		};
		
		try {
			CompressedStreamTools.write(compound, out);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return this;
	}
	
	public TASmodBufferBuilder writeTickratePauseState(TickratePauseState state) {
		writeShort((short) state.ordinal());
		return this;
	}
	
	public static TASstate readTASState(ByteBuffer buf) {
		return TASstate.values()[buf.getShort()];
	}
	
	public static NBTTagCompound readNBTTagCompound(ByteBuffer buf) throws IOException {
		DataInput input = new DataInput() {
			
			@Override
			public int skipBytes(int n) throws IOException {
				throw new IOException("Not implemented");
			}
			
			@Override
			public int readUnsignedShort() throws IOException {
				throw new IOException("Not implemented");
			}
			
			@Override
			public int readUnsignedByte() throws IOException {
				throw new IOException("Not implemented");
			}
			
			@Override
			public String readUTF() throws IOException {
				return TASmodBufferBuilder.readString(buf);
			}
			
			@Override
			public short readShort() throws IOException {
				return TASmodBufferBuilder.readShort(buf);
			}
			
			@Override
			public long readLong() throws IOException {
				return TASmodBufferBuilder.readLong(buf);
			}
			
			@Override
			public String readLine() throws IOException {
				throw new IOException("Not implemented");
			}
			
			@Override
			public int readInt() throws IOException {
				return TASmodBufferBuilder.readInt(buf);
			}
			
			@Override
			public void readFully(byte[] b, int off, int len) throws IOException {
				buf.get(b, off, len);
			}
			
			@Override
			public void readFully(byte[] b) throws IOException {
				buf.get(b);
			}
			
			@Override
			public float readFloat() throws IOException {
				return TASmodBufferBuilder.readFloat(buf);
			}
			
			@Override
			public double readDouble() throws IOException {
				return TASmodBufferBuilder.readDouble(buf);
			}
			
			@Override
			public char readChar() throws IOException {
				return buf.getChar();
			}
			
			@Override
			public byte readByte() throws IOException {
				return buf.get();
			}
			
			@Override
			public boolean readBoolean() throws IOException {
				return TASmodBufferBuilder.readBoolean(buf);
			}
		};
		
		return CompressedStreamTools.read(input, NBTSizeTracker.INFINITE);
	}
	
	public TickratePauseState readTickratePauseState(ByteBuffer buf) {
		return TickratePauseState.values()[buf.getShort()];
	}
}
