package common.server;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

import java.nio.ByteBuffer;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.minecrafttas.common.events.CompactPacketHandler;
import com.minecrafttas.common.server.ByteBufferBuilder;
import com.minecrafttas.common.server.Client.Side;
import com.minecrafttas.common.server.interfaces.PacketID;

class ByteBufferBuilderTest {


	private enum TestPacketIDs implements PacketID {
		TESTID_1,
		TESTID_2,
		TESTID_3;

		private Side side;
		private CompactPacketHandler lambda;

		private TestPacketIDs() {
		}
		
		private TestPacketIDs(Side side, CompactPacketHandler lambda) {
			this.side = side;
			this.lambda = lambda;
		}
		
		@Override
		public int getID() {
			return this.ordinal();
		}

		@Override
		public CompactPacketHandler getLambda() {
			return this.lambda;
		}

		@Override
		public Side getSide() {
			return this.side;
		}

		@Override
		public String getName() {
			return this.name();
		}

		@Override
		public boolean shouldTrace() {
			return false;
		}
		
	}
	
	/**
	 * Test creating a new ByteBuffer from a ByteBufferbuilder and getting the packetid
	 */
	@Test
	void testId() {
		ByteBufferBuilder builder = new ByteBufferBuilder(TestPacketIDs.TESTID_1);
		ByteBuffer buf = builder.build();
		buf.position(0);
		
		assertEquals(0, buf.getInt());
	}

	/**
	 * Test creating a new ByteBuffer from a ByteBufferbuilder and getting the packetid
	 */
	@Test
	void testId2() {
		ByteBufferBuilder builder = new ByteBufferBuilder(TestPacketIDs.TESTID_2);
		ByteBuffer buf = builder.build();
		buf.position(0);
		
		assertEquals(1, buf.getInt());
	}
	
	/**
	 * Test creating a new ByteBuffer from a ByteBufferbuilder and getting the packetid
	 */
	@Test
	void testId3() {
		ByteBufferBuilder builder = new ByteBufferBuilder(TestPacketIDs.TESTID_3);
		ByteBuffer buf = builder.build();
		buf.position(0);
		
		assertEquals(2, buf.getInt());
	}
	
	/**
	 * Test creating a new ByteBuffer from a ByteBufferbuilder and getting an integer
	 */
	@Test
	void testInt() {
		ByteBufferBuilder builder = new ByteBufferBuilder(TestPacketIDs.TESTID_1);
		
		builder.writeInt(1234);
		
		ByteBuffer buf = builder.build();
		buf.position(4);
		
		assertEquals(1234, ByteBufferBuilder.readInt(buf));
	}
	
	/**
	 * Test creating a new ByteBuffer from a ByteBufferbuilder and getting a float
	 */
	@Test
	void testFloat() {
		ByteBufferBuilder builder = new ByteBufferBuilder(TestPacketIDs.TESTID_1);
		
		builder.writeFloat(12.2F);
		
		ByteBuffer buf = builder.build();
		buf.position(4);
		
		assertEquals(12.2F, ByteBufferBuilder.readFloat(buf));
	}
	
	/**
	 * Test creating a new ByteBuffer from a ByteBufferbuilder and getting a double
	 */
	@Test
	void testDouble() {
		ByteBufferBuilder builder = new ByteBufferBuilder(TestPacketIDs.TESTID_1);
		
		builder.writeDouble(60.9D);
		
		ByteBuffer buf = builder.build();
		buf.position(4);
		
		assertEquals(60.9D, ByteBufferBuilder.readDouble(buf));
	}
	
	/**
	 * Test creating a new ByteBuffer from a ByteBufferbuilder and getting a long
	 */
	@Test
	void testLong() {
		ByteBufferBuilder builder = new ByteBufferBuilder(TestPacketIDs.TESTID_1);
		
		builder.writeLong(800815L);
		
		ByteBuffer buf = builder.build();
		buf.position(4);
		
		assertEquals(800815L, ByteBufferBuilder.readLong(buf));
	}
	
	/**
	 * Test creating a new ByteBuffer from a ByteBufferbuilder and getting a short
	 */
	@Test
	void testShort() {
		ByteBufferBuilder builder = new ByteBufferBuilder(TestPacketIDs.TESTID_1);
		
		builder.writeShort((short)12);
		
		ByteBuffer buf = builder.build();
		buf.position(4);
		
		assertEquals(12, ByteBufferBuilder.readShort(buf));
	}
	
	/**
	 * Test creating a new ByteBuffer from a ByteBufferbuilder and getting a boolean
	 */
	@Test
	void testBoolean() {
		ByteBufferBuilder builder = new ByteBufferBuilder(TestPacketIDs.TESTID_1);
		
		builder.writeBoolean(true);
		
		ByteBuffer buf = builder.build();
		buf.position(4);
		
		assertEquals(true, ByteBufferBuilder.readBoolean(buf));
	}
	
	/**
	 * Test creating a new ByteBuffer from a ByteBufferbuilder and getting a boolean
	 */
	@Test
	void testBoolean2() {
		ByteBufferBuilder builder = new ByteBufferBuilder(TestPacketIDs.TESTID_1);
		
		builder.writeBoolean(false);
		
		ByteBuffer buf = builder.build();
		buf.position(4);
		
		assertEquals(false, ByteBufferBuilder.readBoolean(buf));
	}
	
	/**
	 * Test creating a new ByteBuffer from a ByteBufferbuilder and getting a string
	 */
	@Test
	void testString() {
		ByteBufferBuilder builder = new ByteBufferBuilder(TestPacketIDs.TESTID_1);
		
		builder.writeString("Test");
		
		ByteBuffer buf = builder.build();
		buf.position(4);
		
		assertEquals("Test", ByteBufferBuilder.readString(buf));
	}
	
	/**
	 * Test
	 */
	@Test
	void testByteArray() {
		ByteBufferBuilder builder = new ByteBufferBuilder(TestPacketIDs.TESTID_1);
		
		builder.writeByteArray(new byte[] {1,1,0,0,1,1,0});
		
		ByteBuffer buf = builder.build();
		buf.position(4);
		
		assertArrayEquals(new byte[] {1,1,0,0,1,1,0}, ByteBufferBuilder.readByteArray(buf));
	}
	
	/**
	 * Test creating a new ByteBuffer from a ByteBufferbuilder and getting a uuid
	 */
	@Test
	void testUUID() {
		ByteBufferBuilder builder = new ByteBufferBuilder(TestPacketIDs.TESTID_1);
		
		builder.writeUUID(UUID.fromString("b8abdafc-5002-40df-ab68-63206ea4c7e8"));
		
		ByteBuffer buf = builder.build();
		buf.position(4);
		
		assertEquals("b8abdafc-5002-40df-ab68-63206ea4c7e8", ByteBufferBuilder.readUUID(buf).toString());
	}
	
	// ====================================
	
	/**
	 * Test creating a clone from an existing ByteBufferBuilder
	 */
	@Test
	void testClone() {
		ByteBufferBuilder builder = new ByteBufferBuilder(TestPacketIDs.TESTID_1);
		builder.writeInt(1234);
		
		ByteBufferBuilder clone;
		try {
			clone = builder.clone();
		} catch (CloneNotSupportedException e) {
			fail(e);
			return;
		}
		
		ByteBuffer buf = clone.build();
		buf.position(4);
		
		assertEquals(1234, ByteBufferBuilder.readInt(buf));
	}
	
	// =====================================
	
	/**
	 * Test an exception for all types if a ByteBufferBuilder is already closed
	 */
	@Test
	void testClosed() {
		ByteBufferBuilder builder = new ByteBufferBuilder(TestPacketIDs.TESTID_1);
		builder.close();
		builder.close();
		
		Exception exception;
	    exception = assertThrows(IllegalStateException.class, () -> {
	        builder.writeInt(0);
	    });
	    assertEquals("This buffer is already closed", exception.getMessage());
	    exception = assertThrows(IllegalStateException.class, () -> {
	        builder.writeDouble(0D);
	    });
	    assertEquals("This buffer is already closed", exception.getMessage());
	    exception = assertThrows(IllegalStateException.class, () -> {
	    	builder.writeFloat(0F);
	    });
	    assertEquals("This buffer is already closed", exception.getMessage());
	    exception = assertThrows(IllegalStateException.class, () -> {
	    	builder.writeLong(0L);
	    });
	    assertEquals("This buffer is already closed", exception.getMessage());
	    exception = assertThrows(IllegalStateException.class, () -> {
	    	builder.writeShort((short)0);
	    });
	    assertEquals("This buffer is already closed", exception.getMessage());
	    exception = assertThrows(IllegalStateException.class, () -> {
	    	builder.writeBoolean(true);
	    });
	    assertEquals("This buffer is already closed", exception.getMessage());
	    exception = assertThrows(IllegalStateException.class, () -> {
	    	builder.writeString("");
	    });
	    assertEquals("This buffer is already closed", exception.getMessage());
	    exception = assertThrows(IllegalStateException.class, () -> {
	    	builder.writeUUID(UUID.randomUUID());
	    });
	    assertEquals("This buffer is already closed", exception.getMessage());
	    exception = assertThrows(IllegalStateException.class, () -> {
	    	builder.build();
	    });
	    assertEquals("This buffer is already closed", exception.getMessage());
	}
}
