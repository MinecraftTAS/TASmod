package com.minecrafttas.mctcommon.server;

import java.nio.ByteBuffer;

/**
 * Thread safe list (probably) for recycling byte buffers without reallocation
 * @author Pancake
 */
public class SecureList {

	static final int BUFFER_SIZE = 1000 * 20; // 20 kB
	static final int BUFFER_COUNT = 100; 	  // * 100 => 2 MB

	public static SecureList POOL = new SecureList(BUFFER_COUNT, BUFFER_SIZE);

	private final ByteBuffer[] buffers;
	private final boolean[] locked;

	/**
	 * Initialize secure list
	 * @param length Amount of byte buffers
	 * @param size Length of each byte buffer
	 */
	public SecureList(int length, int size) {
		this.buffers = new ByteBuffer[length];
		this.locked = new boolean[length];

		for (int i = 0; i < length; i++)
			this.buffers[i] = ByteBuffer.allocate(size);
	}

	/**
	 * Find available byte buffer
	 * @return Available byte buffer or -1
	 */
	public int available() {
		for (int i = 0; i < this.locked.length; i++)
			if (!this.locked[i])
				return i;
		return -1;
	}
	
	/**
	 * Lock and return byte buffer
	 * @param i Index to lock
	 * @return Byte buffer
	 */
	public ByteBuffer lock(int i) {
		if (this.locked[i])
			throw new RuntimeException("Tried to lock already locked buffer");

		this.locked[i] = true;
		return (ByteBuffer) this.buffers[i].clear();
	}
	
	/**
	 * Unlocke byte buffer
	 * @param index Index to unlock
	 */
	public void unlock(int index) {
		this.locked[index] = false;
	}
	
}
