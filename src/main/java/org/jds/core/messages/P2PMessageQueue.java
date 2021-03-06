package org.jds.core.messages;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

import org.jds.core.utils.NetworkUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A message queue implementation for simple (Point-to-Point) sockets.
 * 
 * @author Gergely Kiss
 */
public class P2PMessageQueue extends AbstractNodeMessageQueue {
	private static final Logger log = LoggerFactory.getLogger(P2PMessageQueue.class);

	private ServerSocket socket;
	private int port;

	public P2PMessageQueue(int portRangeFrom, int portRangeTo) throws SocketException {
		for (port = portRangeFrom; port < portRangeTo; port++) {
			try {
				socket = new ServerSocket(port, 50);
				break;
			} catch (IOException e) {
				log.info("Failed to open socket on port {}, retrying...");
			}
		}

		if (socket == null) {
			throw new SocketException("Failed to create server socket in port range: "
					+ portRangeFrom + "-" + portRangeTo);
		}

		log.info("Connected P2PMessageQueue at {}:{}", socket, port);
	}

	@Override
	public Message pop() throws IOException {
		while (true) {
			Socket client = null;

			try {
				client = socket.accept();

				Message msg = Message.parseMessage(client.getInputStream());

				// Skipping local packets
				InetAddress address = client.getInetAddress();

				if (isLocalMessage(msg, address)) {
					continue;
				}

				return msg;
			} finally {
				NetworkUtils.close(client);
			}
		}
	}

	@Override
	public void push(Message message) {
		// TODO
		throw new UnsupportedOperationException();
	}
}
