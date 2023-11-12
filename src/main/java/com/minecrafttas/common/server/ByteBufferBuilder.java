package com.minecrafttas.common.server;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.UUID;

import com.minecrafttas.common.server.interfaces.PacketID;

/**
 * Helper method for creating byte buffers which get pooled from a
 * {@link SecureList}
 * 
 * @author Scribble
 */
public class ByteBufferBuilder {

	private int bufferIndex;
	protected ByteBuffer buffer;

	/* Debug PacketName */
	private PacketID bufferPacketId;
	/* Debug PacketContent */
	private ArrayList<String> bufferContent = new ArrayList<>();

	public ByteBufferBuilder(int id) {
		bufferIndex = SecureList.POOL.available();
		buffer = SecureList.POOL.lock(bufferIndex);
		buffer.putInt(id);
	}

	public ByteBufferBuilder(PacketID packet) {
		this(packet.getID());
		this.bufferPacketId = packet;
	}

	public ByteBufferBuilder(ByteBuffer buf) {
		bufferIndex = SecureList.POOL.available();
		buffer = SecureList.POOL.lock(bufferIndex);
		buffer.put(buf);
	}

	private ByteBufferBuilder(int bufferIndex, ByteBuffer buffer, PacketID bufferName, ArrayList<String> bufferContent) {
		this.bufferIndex = bufferIndex;
		this.buffer = buffer;
		this.bufferPacketId = bufferName;
		this.bufferContent = new ArrayList<>(bufferContent);
	}

	public ByteBuffer build() {
		if (buffer == null)
			throw new IllegalStateException("This buffer is already closed");
		return this.buffer;
	}

	public ByteBufferBuilder writeInt(int value) {
		if (buffer == null)
			throw new IllegalStateException("This buffer is already closed");
		buffer.putInt(value);
		bufferContent.add("int " + value);
		return this;
	}

	public ByteBufferBuilder writeDouble(double value) {
		if (buffer == null)
			throw new IllegalStateException("This buffer is already closed");
		buffer.putDouble(value);
		bufferContent.add("double " + value);
		return this;
	}

	public ByteBufferBuilder writeFloat(float value) {
		if (buffer == null)
			throw new IllegalStateException("This buffer is already closed");
		buffer.putFloat(value);
		bufferContent.add("float " + value);
		return this;
	}

	public ByteBufferBuilder writeLong(long value) {
		if (buffer == null)
			throw new IllegalStateException("This buffer is already closed");
		buffer.putLong(value);
		bufferContent.add("long " + value);
		return this;
	}

	public ByteBufferBuilder writeShort(short value) {
		if (buffer == null)
			throw new IllegalStateException("This buffer is already closed");
		buffer.putShort(value);
		bufferContent.add("short " + value);
		return this;
	}

	public ByteBufferBuilder writeBoolean(boolean value) {
		if (buffer == null)
			throw new IllegalStateException("This buffer is already closed");
		buffer.put((byte) (value ? 1 : 0));
		bufferContent.add("boolean " + value);
		return this;
	}

	public ByteBufferBuilder writeString(String value) {
		if (buffer == null)
			throw new IllegalStateException("This buffer is already closed");
		byte[] stringbytes = value.getBytes();
		buffer.putInt(stringbytes.length);
		buffer.put(stringbytes);
		bufferContent.add("String " + value);
		return this;
	}

	public ByteBufferBuilder writeUUID(UUID uuid) {
		if (buffer == null)
			throw new IllegalStateException("This buffer is already closed");
		buffer.putLong(uuid.getMostSignificantBits());
		buffer.putLong(uuid.getLeastSignificantBits());
		bufferContent.add("UUID " + uuid);
		return this;
	}

	public ByteBufferBuilder writeByteArray(byte[] value) {
		buffer.putInt(value.length);
		buffer.put(value);

		bufferContent.add("ByteArray length(" + value.length + ")");
		return this;
	}

	/**
	 * Unlocks the buffer from the pool making it available for other uses
	 */
	public void close() {
		if (buffer != null) {
			SecureList.POOL.unlock(bufferIndex);
			this.buffer = null;
		}
	}

	@Override
	public ByteBufferBuilder clone() throws CloneNotSupportedException {
		int current = this.buffer.position();
		int sid = SecureList.POOL.available();
		ByteBuffer clone = SecureList.POOL.lock(sid);

		this.buffer.limit(current).position(0);

		clone.put(this.buffer);

		this.buffer.position(current);

		return new ByteBufferBuilder(sid, clone, this.bufferPacketId, this.bufferContent);
	}

	public static int readInt(ByteBuffer buf) {
		return buf.getInt();
	}

	public static double readDouble(ByteBuffer buf) {
		return buf.getDouble();
	}

	public static float readFloat(ByteBuffer buf) {
		return buf.getFloat();
	}

	public static long readLong(ByteBuffer buf) {
		return buf.getLong();
	}

	public static short readShort(ByteBuffer buf) {
		return buf.getShort();
	}

	public static boolean readBoolean(ByteBuffer buf) {
		return buf.get() == 1 ? true : false;
	}

	public static UUID readUUID(ByteBuffer buf) {
		return new UUID(buf.getLong(), buf.getLong());
	}

	public static String readString(ByteBuffer buf) {
		byte[] nameBytes = new byte[buf.getInt()];
		buf.get(nameBytes);
		return new String(nameBytes);
	}

	public static byte[] readByteArray(ByteBuffer buf) {
		int length = buf.getInt();
		byte[] array = new byte[length];
		buf.get(array);
		return array;
	}

	/**
	 * Debug packetname
	 * 
	 * @return
	 */
	public PacketID getPacketID() {
		return bufferPacketId;
	}

	public String getPacketContent() {
		return String.join("\n", this.bufferContent);
	}

	@Override
	public String toString() {
		return getPacketID().getName() + getPacketContent();
	}
}
