package org.jds.core.messages;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A message queue implementation for multicast sockets.
 * 
 * @author Gergely Kiss
 */
public class MulticastMessageQueue extends AbstractNodeMessageQueue {
	private static final Logger log = LoggerFactory.getLogger(MulticastMessageQueue.class);
	private static final int MAX_BUFSIZE = 4096;

	private MulticastSocket socket;
	private InetAddress group;
	private int port;

	public MulticastMessageQueue(String group, int portRangeFrom, int portRangeTo)
			throws IOException {
		this.group = InetAddress.getByName(group);

		for (port = portRangeFrom; port < portRangeTo; port++) {
			try {
				socket = new MulticastSocket(port);
				break;
			} catch (IOException e) {
				log.info("Failed to open socket on port {}, retrying...");
			}
		}

		if (socket == null) {
			throw new SocketException("Failed to create server socket in port range: "
					+ portRangeFrom + "-" + portRangeTo);
		}

		socket.setReceiveBufferSize(MAX_BUFSIZE);
		socket.setLoopbackMode(true);
		socket.setTrafficClass(0x04);
		socket.joinGroup(this.group);

		log.info("Connected MulticastMessageQueue at {}:{}", group, port);
	}

	@Override
	public Message pop() throws IOException {
		byte[] buf = new byte[MAX_BUFSIZE];

		while (true) {
			DatagramPacket packet = new DatagramPacket(buf, buf.length);
			socket.receive(packet);

			byte[] data = packet.getData();

			Message msg = Message.parseMessage(new ByteArrayInputStream(data));

			// Skipping local packets
			InetAddress address = packet.getAddress();

			if (isLocalMessage(msg, address)) {
				continue;
			}

			return msg;
		}
	}

	@Override
	public void push(Message message) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream(MAX_BUFSIZE);

		// Setting node specific message parameters
		message.setProcessId(processId);
		message.setQueueId(queueId);

		Message.generateMessage(message, baos);

		byte[] buf = baos.toByteArray();
		DatagramPacket packet = new DatagramPacket(buf, buf.length, group, port);

		socket.send(packet);
	}

	/**
	 * Leaves the multicast group and closes the underlying socket.
	 */
	public void close() {
		try {
			socket.leaveGroup(group);
		} catch (Exception e) {
			log.error("Failed to leave group: " + group);
		}

		try {
			socket.close();
		} catch (Exception e) {
			log.error("Failed to close socket: " + socket);
		}
	}

}
