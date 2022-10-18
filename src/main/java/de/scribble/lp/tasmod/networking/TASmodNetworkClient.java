package de.scribble.lp.tasmod.networking;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.logging.log4j.Logger;

import de.scribble.lp.tasmod.TASmod;
import de.scribble.lp.tasmod.networking.packets.ServerBoundQuitPacket;
import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketBuffer;

public class TASmodNetworkClient {
	
	private Logger logger;
	
	private Thread clientThread;
	
	private Socket clientSocket;
	
	private BlockingQueue<Packet> packetsToSend = new LinkedBlockingQueue<>();
	
	public TASmodNetworkClient(Logger logger) {
		this(logger, "127.0.0.1", 3111);
		this.logger = logger;
	}
	
	public TASmodNetworkClient(Logger logger, String serverIP, int port) {
		this.logger = logger;
		createClient(serverIP, port);
	}
	
	public void sendPacket(Packet packet) {
		if(clientThread == null) 
			return;
		if(!clientThread.isAlive())
			return;
		packetsToSend.add(packet);
	}
	
	private void createClient(String serverIp, int port) {
		logger.info("Creating client connection");
		
		clientThread = new Thread(() -> {
			try(Socket cSocket = new Socket()){
				Thread.sleep(2000);
				cSocket.connect(new InetSocketAddress(serverIp, port));
				this.clientSocket = cSocket;
				CommonHandler.handleSocket(clientSocket, packetsToSend);
			} catch (EOFException | SocketException | InterruptedIOException exception) {
				// The custom TASmod client was closed and the end of stream was reached. The socket was shut down properly.
				TASmod.logger.info("Custom TASmod client was shutdown");
			} catch (Exception exception) {
				TASmod.logger.error("Custom TASmod client was unexpectedly shutdown {}", exception);
			}
		});
		clientThread.setName("TASmod Network Client Main");
		clientThread.setDaemon(true);
		clientThread.start();
	}
	
	public void killClient() throws IOException {
		if(clientThread != null) {
			sendPacket(new ServerBoundQuitPacket());
			clientSocket.close();
		}
	}
	
}
