package mctcommon.server;

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
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.minecrafttas.mctcommon.events.CompactPacketHandler;
import com.minecrafttas.mctcommon.server.ByteBufferBuilder;
import com.minecrafttas.mctcommon.server.Client;
import com.minecrafttas.mctcommon.server.Client.Side;
import com.minecrafttas.mctcommon.server.PacketHandlerRegistry;
import com.minecrafttas.mctcommon.server.Server;
import com.minecrafttas.mctcommon.server.exception.PacketNotImplementedException;
import com.minecrafttas.mctcommon.server.exception.WrongSideException;
import com.minecrafttas.mctcommon.server.interfaces.ClientPacketHandler;
import com.minecrafttas.mctcommon.server.interfaces.PacketID;
import com.minecrafttas.mctcommon.server.interfaces.ServerPacketHandler;

/**
 * An integration test for the {@link Server} class by setting up a connection.
 * Disabled due to gihub actions failing to execute the tests
 */
@Disabled
class ServerTest {

	// The time to live for how long the tests should wait for the asynchronous server
	private static int ttl = 1;
	
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

		@Override
		public boolean shouldTrace() {
			return false;
		}

	}

	private static Client.Side side = null;

	private static class TestingClass implements ClientPacketHandler, ServerPacketHandler {

		@Override
		public PacketID[] getAcceptedPacketIDs() {
			return new TestPacketIDs[] { TestPacketIDs.TEST_INTERFACE_INT, TestPacketIDs.TEST_INTERFACE_STRING };
		}

		@Override
		public void onServerPacket(PacketID id, ByteBuffer buf, String username) throws PacketNotImplementedException, WrongSideException, Exception {
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
				throw new PacketNotImplementedException(id, this.getClass(), Side.SERVER);
			}
		}

		@Override
		public void onClientPacket(PacketID id, ByteBuffer buf, String username) throws PacketNotImplementedException, WrongSideException, Exception {
			TestPacketIDs packet = (TestPacketIDs) id;
			switch (packet) {
			case TEST_INTERFACE_INT:
				result = ByteBufferBuilder.readInt(buf);
				side = Side.CLIENT;
				latch.countDown();
				break;
			default:
				throw new PacketNotImplementedException(id, this.getClass(), Side.CLIENT);
			}
		}

	}

	private static Server server;
	private static Client client;

	private static Integer result = null;
	private static String result2 = null;

	private static TestingClass clazz = new TestingClass();

	/**
	 * Setting up a local connection between client and server
	 * @throws Exception
	 */
	@BeforeAll
	static void setUpBeforeClass() throws Exception {
		try {
			server = new Server(25566, TestPacketIDs.values());
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			client = new Client("127.0.0.1", 25566, TestPacketIDs.values(), "TASBot", true);
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

	/**
	 * Test sending an int packet to {@link TestingClass#onServerPacket(PacketID, ByteBuffer, UUID)}
	 */
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

	/**
	 * Test sending an int packet to {@link TestingClass#onClientPacket(PacketID, ByteBuffer, UUID)} to all clients currently connected
	 */
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
	
	/**
	 * Test sending an int packet to {@link TestingClass#onClientPacket(PacketID, ByteBuffer, UUID)} to only one client
	 */
	@Test
	void testSendToClientInterface() {
		try {
			server.sendTo("TASBot", new ByteBufferBuilder(TestPacketIDs.TEST_INTERFACE_INT).writeInt(3));
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
	
	/**
	 * Test sending an string packet to {@link TestingClass#onClientPacket(PacketID, ByteBuffer, UUID)} to only one client
	 */
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
	
	/**
	 * Test sending an int packet to {@link TestPacketIDs#TEST_LAMBDA_SERVER}
	 */
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
	
	/**
	 * Test sending an int packet to {@link TestPacketIDs#TEST_LAMBDA_CLIENT} to all clients
	 */
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
	
	/**
	 * Test sending an int packet to {@link TestPacketIDs#TEST_LAMBDA_CLIENT} to one client
	 */
	@Test
	void testSendToClientLambda() {
		try {
			server.sendTo("TASBot", new ByteBufferBuilder(TestPacketIDs.TEST_LAMBDA_CLIENT).writeInt(6));
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
