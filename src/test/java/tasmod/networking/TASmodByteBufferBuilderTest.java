package tasmod.networking;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.minecrafttas.common.events.CompactPacketHandler;
import com.minecrafttas.common.server.Client.Side;
import com.minecrafttas.common.server.interfaces.PacketID;
import com.minecrafttas.tasmod.networking.TASmodBufferBuilder;

import net.minecraft.nbt.NBTTagCompound;

class TASmodByteBufferBuilderTest {

	private enum TestPacketIDs implements PacketID {
		TESTID_1;

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
	 * Test if NBTTagCompounds get correctly stored in a ByteBuffer
	 */
	@Test
	void testNBT() {
		
		NBTTagCompound tag = new NBTTagCompound();
		NBTTagCompound tag2 = new NBTTagCompound();
		
		tag.setString("String", "What");
		tag.setShort("Short", (short) 3);
		tag.setLong("Long", 8008132L);
		tag.setInteger("Int", -5);
		tag.setIntArray("IntArray", new int[] {1, 2, 3});
		tag.setDouble("Double", 1.2D);
		tag.setByte("Byte", (byte) 1);
		tag.setUniqueId("UUID", UUID.fromString("b8abdafc-5002-40df-ab68-63206ea4c7e8"));
		tag.setFloat("Float", 1.0F);
		tag.setBoolean("Boolean", true);
		tag.setByteArray("ByteArray", new byte[] {1, 0, 0});
		
		tag2.setTag("Data", tag);
		
		TASmodBufferBuilder bufferBuilder = new TASmodBufferBuilder(TestPacketIDs.TESTID_1).writeNBTTagCompound(tag2);
		
		ByteBuffer buf = bufferBuilder.build();
		
		buf.position(4);
		
		NBTTagCompound tag3 = null;
		try {
			tag3 = TASmodBufferBuilder.readNBTTagCompound(buf);
		} catch (IOException e) {
			fail(e);
			return;
		}
		
		NBTTagCompound tag4 = tag3.getCompoundTag("Data");
		
		assertEquals("What", tag4.getString("String"));
		assertEquals((short)3, tag4.getShort("Short"));
		assertEquals(8008132L, tag4.getLong("Long"));
		assertEquals(-5, tag4.getInteger("Int"));
		assertArrayEquals(new int[] {1, 2, 3}, tag4.getIntArray("IntArray"));
		assertEquals(1.2D, tag4.getDouble("Double"));
		assertEquals((byte) 1, tag4.getByte("Byte"));
		assertEquals(UUID.fromString("b8abdafc-5002-40df-ab68-63206ea4c7e8"), tag4.getUniqueId("UUID"));
		assertEquals(1.0F, tag4.getFloat("Float"));
		assertEquals(true, tag4.getBoolean("Boolean"));
		assertArrayEquals(new byte[] {1, 0, 0}, tag4.getByteArray("ByteArray"));
	}

}
