package de.scribble.lp.tasmod.networking;

import java.io.EOFException;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import de.scribble.lp.tasmod.TASmod;
import de.scribble.lp.tasmod.networking.exceptions.ServerAlreadyRunningException;
import de.scribble.lp.tasmod.ticksync.TickSyncServer;
import scala.reflect.internal.Trees.If;

/**
 * The TASmod itself has a custom connection running next to the minecraft one.
 * It's necessary since the integrated packet connection is tick-based and therefore cannot communicate inbetween ticks.
 *
 * IMPLEMENTATION NOTICE:
 * The server creates a separate thread to run off so that it's non-blocking. Therefore it uses a queue for outgoing packets.
 * @author Pancake
 */
public class Server {

	/**
	 * This is the thread that runs the server. It will exit once the server has disconnected.
	 * Interrupting it will always close the connection and end the thread.
	 */
	private static Thread instance;

	/**
	 * Count of clients connected to the server
	 */
	private static int connections;

	/**
	 * This is the server socket. Interrupting it will always close the connection and end the thread (instance).
	 */
	private static ServerSocket serverSocket;

	/**
	 * This queue of packets is going to be sent by another thread.
	 */
	private static LinkedBlockingQueue<BlockingQueue<Packet>> packetsToSend = new LinkedBlockingQueue<>(); // Initialize with something so this cannot cause a npe

	/**
	 * Adds a packet to the queue of packets to send to all clients
	 * @param packet Packet to send
	 */
	public static void sendPacket(Packet packet) {
		if (Server.instance == null)
			return;
		if (!Server.instance.isAlive())
			return;
		Server.packetsToSend.forEach(queue -> queue.add(packet));
	}

	/**
	 * Once the server enters launch phase a separate server thread is created.
	 *
	 * IMPLEMENTATION NOTICE:
	 * Called from CommonTASmod.
	 *
	 * @throws IOException Fatal Exception, the socket couldn't be closed
	 * @throws If the last server wasn't succesfully shut down it will throw an exception and forcefully shut down the server
	 */
	public static void createServer() throws ServerAlreadyRunningException, IOException {
		TASmod.logger.info("Start creating a custom server");
		boolean isRunning = Server.instance == null ? false : Server.instance.isAlive();
		// Cancel the currently running server
		if (isRunning)
			Server.serverSocket.close();

		// Clear the amount of connections
		Server.connections = 0;
		// Clear the list of packets to send
		Server.packetsToSend = new LinkedBlockingQueue<>();
		// Start a server
		Server.instance = new Thread(() -> {
			try(ServerSocket serverSocket = new ServerSocket(3111)) {
				Server.serverSocket = serverSocket;
				// Wait until new clients are there and then handle them.
				while (true) {
					Socket socket = serverSocket.accept();
					final LinkedBlockingQueue<Packet> queue = new LinkedBlockingQueue<>();
					Server.packetsToSend.add(queue);
					Thread handler = new Thread(() -> {
						Server.connections++;
						TickSyncServer.shouldTick.set(true);
						try {
							CommonHandler.handleSocket(socket, queue); // this will create a new thread for outstream and use the current thread for instream
						} catch (EOFException exception) {
							// The custom TASmod client connection was closed and the end of stream was reached. The socket was shut down properly.
							TASmod.logger.debug("Custom TASmod client connection was shutdown");
						} catch (Exception exception) {
							TASmod.logger.error("Custom TASmod client connection was unexpectedly shutdown {}", exception);
						}
						TickSyncServer.shouldTick.set(true);
						Server.connections--;
					});
					handler.setDaemon(true);
					handler.start();
				}
			} catch (EOFException | SocketException | InterruptedIOException exception) {
				// The custom TASmod server was closed and the end of stream was reached. The socket was shut down properly.
				TASmod.logger.debug("Custom TASmod server was shutdown");
			} catch (Exception exception) {
				TASmod.logger.error("Custom TASmod server was unexpectedly shutdown {}", exception);
			}
		});
		Server.instance.setName("TASmod Network Handler Server");
		Server.instance.setDaemon(true); // If daemon is set, the jvm will quit without waiting for this thread to finish
		Server.instance.start();

		// Make sure to throw an exception if the server was running
		if (isRunning)
			throw new ServerAlreadyRunningException();
	}

	/**
	 * Kills the custom TASmod server if is running
	 * @throws IOException Thrown if the socket couldn't be closed
	 */
	public static void killServer() throws IOException {
		TASmod.logger.info("Start killing custom server");
		if (Server.instance != null)
			Server.serverSocket.close();
		Server.connections = 0;
	}

	/**
	 * Obtains the amount of connections connected to this socket
	 * @return Connection count
	 */
	public static int getConnectionCount() {
		return Server.connections;
	}

}
