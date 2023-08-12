package com.minecrafttas.server;

import java.nio.ByteBuffer;
import java.util.UUID;

import com.minecrafttas.server.interfaces.PacketID;

/**
 * Helper method for creating byte buffers which get pooled from a {@link SecureList}
 * @author Scribble
 */
public class ByteBufferBuilder{
	
	private int bufferIndex;
	private ByteBuffer buffer;
	
	public ByteBufferBuilder(int id) {
		bufferIndex = SecureList.POOL.available();
		buffer = SecureList.POOL.lock(bufferIndex);
		buffer.putInt(id);
	}
	
	public ByteBufferBuilder(PacketID packet) {
		this(packet.getID());
	}
	
	private ByteBufferBuilder(int bufferIndex, ByteBuffer buffer) {
		this.bufferIndex = bufferIndex;
		this.buffer = buffer;
	}
	
	public ByteBuffer build() {
		if(buffer == null) throw new IllegalStateException("This buffer is already closed");
		return this.buffer;
	}
	
	public ByteBufferBuilder writeInt(int value) {
		if(buffer == null) throw new IllegalStateException("This buffer is already closed");
		buffer.putInt(value);
		return this;
	}
	
	public ByteBufferBuilder writeDouble(double value) {
		if(buffer == null) throw new IllegalStateException("This buffer is already closed");
		buffer.putDouble(value);
		return this;
	}
	
	public ByteBufferBuilder writeFloat(float value) {
		if(buffer == null) throw new IllegalStateException("This buffer is already closed");
		buffer.putFloat(value);
		return this;
	}
	
	public ByteBufferBuilder writeLong(long value) {
		if(buffer == null) throw new IllegalStateException("This buffer is already closed");
		buffer.putLong(value);
		return this;
	}
	
	public ByteBufferBuilder writeShort(short value) {
		if(buffer == null) throw new IllegalStateException("This buffer is already closed");
		buffer.putShort(value);
		return this;
	}
	
	public ByteBufferBuilder writeBoolean(boolean value) {
		if(buffer == null) throw new IllegalStateException("This buffer is already closed");
		buffer.put((byte)(value ?1:0));
		return this;
	}
	
	public ByteBufferBuilder writeUUID(UUID uuid) {
		if(buffer == null) throw new IllegalStateException("This buffer is already closed");
		buffer.putLong(uuid.getMostSignificantBits());
		buffer.putLong(uuid.getLeastSignificantBits());
		return this;
	}

	/**
	 * Unlocks the buffer from the pool making it available for other uses
	 */
	public void close() {
		if(buffer !=null) {
			SecureList.POOL.unlock(bufferIndex);
			this.buffer = null;
		}
	}
	
	@Override
	protected ByteBufferBuilder clone() throws CloneNotSupportedException {
		int limit = this.buffer.position();
		int sid = SecureList.POOL.available();
		return new ByteBufferBuilder(sid, SecureList.POOL.lock(sid).put((ByteBuffer) this.buffer.position(0).limit(limit)));
	}
}
