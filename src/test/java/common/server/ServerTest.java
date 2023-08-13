package common.server;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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

import net.minecraft.client.renderer.BufferBuilder;

class ServerTest {

	private enum TestPacketIDs implements PacketID {
		TEST_INTERFACE, TEST_LAMBDA_CLIENT(Side.CLIENT, (buf, clientID) -> {
			latch.countDown();
		}), TEST_LAMBDA_SERVER(Side.SERVER, (buf, clientID) -> {
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

	private static Client.Side side;

	private static class TestingClass implements ClientPacketHandler, ServerPacketHandler {

		@Override
		public PacketID[] getAcceptedPacketIDs() {
			return new TestPacketIDs[] { TestPacketIDs.TEST_INTERFACE };
		}

		@Override
		public void onServerPacket(PacketID id, ByteBuffer buf, UUID clientID) throws PacketNotImplementedException, WrongSideException, Exception {
			TestPacketIDs packet = (TestPacketIDs) id;
			switch (packet) {
			case TEST_INTERFACE:
				latch.countDown();
				side = Side.SERVER;
				break;
			default:
				throw new PacketNotImplementedException(id, this.getClass());
			}
		}

		@Override
		public void onClientPacket(PacketID id, ByteBuffer buf, UUID clientID) throws PacketNotImplementedException, WrongSideException, Exception {
			TestPacketIDs packet = (TestPacketIDs) id;
			switch (packet) {
			case TEST_INTERFACE:
				latch.countDown();
				side = Side.SERVER;
				break;
			default:
				throw new PacketNotImplementedException(id, this.getClass());
			}
		}

	}

	private static Server server;
	private static Client client;

	private Integer result = null;
	
	private static TestingClass clazz=new TestingClass();
	
	@BeforeAll
	static void setUpBeforeClass() throws Exception {
		try {
			server = new Server(25565, TestPacketIDs.values());
		} catch (Exception e) {
			e.printStackTrace();
		}

		UUID uuid = UUID.fromString("b8abdafc-5002-40df-ab68-63206ea4c7e8");

		client = new Client("127.0.0.1", 25565, TestPacketIDs.values(), uuid);
		
		PacketHandlerRegistry.register(clazz);
	}

	@AfterAll
	static void tearDownAfterClass() throws Exception {
		server.close();
		client.close();
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
	void testSendClient() {
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		try {
			client.send(new ByteBufferBuilder(TestPacketIDs.TEST_INTERFACE).writeInt(1));
		} catch (Exception e) {
			fail(e);
			return;
		}
		try {
			latch.await(2, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			fail(e);
			return;
		}
		assertEquals(1, result);
	}

}
