package common.server;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.nio.ByteBuffer;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.minecrafttas.common.events.CompactPacketHandler;
import com.minecrafttas.common.server.ByteBufferBuilder;
import com.minecrafttas.common.server.Client;
import com.minecrafttas.common.server.Client.Side;
import com.minecrafttas.common.server.PacketHandlerRegistry;
import com.minecrafttas.common.server.Server;
import com.minecrafttas.common.server.exception.PacketNotImplementedException;
import com.minecrafttas.common.server.exception.WrongSideException;
import com.minecrafttas.common.server.interfaces.ClientPacketHandler;
import com.minecrafttas.common.server.interfaces.PacketID;
import com.minecrafttas.common.server.interfaces.ServerPacketHandler;

class ServerTest {

	private static int ttl = 5;
	
	private enum TestPacketIDs implements PacketID {
		TEST_INTERFACE_INT,
		TEST_INTERFACE_STRING,
		TEST_LAMBDA_CLIENT(Side.CLIENT, (buf, clientID) -> {
			result = ByteBufferBuilder.readInt(buf);
			ServerTest.side = Side.CLIENT;
			latch.countDown();
		}), 
		TEST_LAMBDA_SERVER(Side.SERVER, (buf, clientID) -> {
			result = ByteBufferBuilder.readInt(buf);
			ServerTest.side = Side.SERVER;
			latch.countDown();
		});

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

	}

	private static Client.Side side = null;

	private static class TestingClass implements ClientPacketHandler, ServerPacketHandler {

		@Override
		public PacketID[] getAcceptedPacketIDs() {
			return new TestPacketIDs[] { TestPacketIDs.TEST_INTERFACE_INT, TestPacketIDs.TEST_INTERFACE_STRING };
		}

		@Override
		public void onServerPacket(PacketID id, ByteBuffer buf, UUID clientID) throws PacketNotImplementedException, WrongSideException, Exception {
			TestPacketIDs packet = (TestPacketIDs) id;
			switch (packet) {
			case TEST_INTERFACE_INT:
				result = ByteBufferBuilder.readInt(buf);
				side = Side.SERVER;
				latch.countDown();
				break;
			case TEST_INTERFACE_STRING:
				result2 = ByteBufferBuilder.readString(buf);
				side = Side.SERVER;
				latch.countDown();
				break;
			default:
				throw new PacketNotImplementedException(id, this.getClass());
			}
		}

		@Override
		public void onClientPacket(PacketID id, ByteBuffer buf, UUID clientID) throws PacketNotImplementedException, WrongSideException, Exception {
			TestPacketIDs packet = (TestPacketIDs) id;
			switch (packet) {
			case TEST_INTERFACE_INT:
				result = ByteBufferBuilder.readInt(buf);
				side = Side.CLIENT;
				latch.countDown();
				break;
			default:
				throw new PacketNotImplementedException(id, this.getClass());
			}
		}

	}

	private static Server server;
	private static Client client;

	private static Integer result = null;
	private static String result2 = null;

	private static TestingClass clazz = new TestingClass();

	@BeforeAll
	static void setUpBeforeClass() throws Exception {
		try {
			server = new Server(25566, TestPacketIDs.values());
		} catch (Exception e) {
			e.printStackTrace();
		}
		UUID uuid = UUID.fromString("b8abdafc-5002-40df-ab68-63206ea4c7e8");

		try {
			client = new Client("127.0.0.1", 25566, TestPacketIDs.values(), uuid);
		} catch (Exception e) {
			e.printStackTrace();
		}
		PacketHandlerRegistry.register(clazz);
	}

	@AfterAll
	static void tearDownAfterClass() throws Exception {
		PacketHandlerRegistry.unregister(clazz);
	}

	private static CountDownLatch latch;

	@BeforeEach
	void setUp() throws Exception {
		latch = new CountDownLatch(1);
	}

	@AfterEach
	void tearDown() throws Exception {
		side = null;
		result = null;
	}

	@Test
	void testSendToServerInterface() {
		try {
			client.send(new ByteBufferBuilder(TestPacketIDs.TEST_INTERFACE_INT).writeInt(1));
		} catch (Exception e) {
			fail(e);
			return;
		}
		try {
			latch.await(ttl, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			fail(e);
			return;
		}
		assertEquals(ttl, result);
		assertEquals(Client.Side.SERVER, side);
	}

	@Test
	void testSendToAllClientsInterface() {
		try {
			server.sendToAll(new ByteBufferBuilder(TestPacketIDs.TEST_INTERFACE_INT).writeInt(2));
		} catch (Exception e) {
			fail(e);
			return;
		}
		try {
			latch.await(ttl, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			fail(e);
			return;
		}
		assertEquals(2, result);
		assertEquals(Client.Side.CLIENT, side);
	}
	
	@Test
	void testSendToClientInterface() {
		try {
			server.sendTo(UUID.fromString("b8abdafc-5002-40df-ab68-63206ea4c7e8"), new ByteBufferBuilder(TestPacketIDs.TEST_INTERFACE_INT).writeInt(3));
		} catch (Exception e) {
			fail(e);
			return;
		}
		try {
			latch.await(ttl, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			fail(e);
			return;
		}
		assertEquals(3, result);
		assertEquals(Client.Side.CLIENT, side);
	}
	
	@Test
	void testSendToServerInterface2() {
		try {
			client.send(new ByteBufferBuilder(TestPacketIDs.TEST_INTERFACE_STRING).writeString("TEST"));
		} catch (Exception e) {
			fail(e);
			return;
		}
		try {
			latch.await(ttl, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			fail(e);
			return;
		}
		assertEquals("TEST", result2);
		assertEquals(Client.Side.SERVER, side);
	}
	
	// ============================ Lambda
	
	@Test
	void testSendToServerLambda() {
		try {
			client.send(new ByteBufferBuilder(TestPacketIDs.TEST_LAMBDA_SERVER).writeInt(4));
		} catch (Exception e) {
			fail(e);
			return;
		}
		try {
			latch.await(ttl, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			fail(e);
			return;
		}
		assertEquals(4, result);
		assertEquals(Client.Side.SERVER, side);
	}
	
	@Test
	void testSendToAllClientsLambda() {
		try {
			server.sendToAll(new ByteBufferBuilder(TestPacketIDs.TEST_LAMBDA_CLIENT).writeInt(5));
		} catch (Exception e) {
			fail(e);
			return;
		}
		try {
			latch.await(ttl, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			fail(e);
			return;
		}
		assertEquals(5, result);
		assertEquals(Client.Side.CLIENT, side);
	}
	
	@Test
	void testSendToClientLambda() {
		try {
			server.sendTo(UUID.fromString("b8abdafc-5002-40df-ab68-63206ea4c7e8"), new ByteBufferBuilder(TestPacketIDs.TEST_LAMBDA_CLIENT).writeInt(6));
		} catch (Exception e) {
			fail(e);
			return;
		}
		try {
			latch.await(ttl, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			fail(e);
			return;
		}
		assertEquals(6, result);
		assertEquals(Client.Side.CLIENT, side);
	}
}
