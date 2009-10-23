package org.jds.core.messages;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

import org.jds.core.utils.NetworkUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A message queue implementation for multicast sockets.
 * 
 * @author Gergely Kiss
 * 
 */
public class MulticastMessageQueue implements IMessageQueue {
	private static final Logger log = LoggerFactory.getLogger(MulticastMessageQueue.class);
	private static final int MAX_BUFSIZE = 4096;

	private final MulticastSocket socket;
	private final InetAddress group;
	private final int multicastPort;
	private final int rmiPort;

	public MulticastMessageQueue(String group, int multicastPort, int rmiPort) throws IOException {
		this.multicastPort = multicastPort;
		this.rmiPort = rmiPort;
		this.group = InetAddress.getByName(group);
		socket = new MulticastSocket(multicastPort);
		socket.joinGroup(this.group);
	}

	@Override
	public Message pop() throws Exception {
		byte[] buf = new byte[MAX_BUFSIZE];

		while (true) {
			DatagramPacket packet = new DatagramPacket(buf, buf.length);
			socket.receive(packet);
			
			byte[] data = packet.getData();
			
			Message msg = Message.parseMessage(new ByteArrayInputStream(data));

			// Skipping local packets
			String address = packet.getAddress().getHostAddress();
			
			if (msg.getSourcePort() == rmiPort && NetworkUtils.LocalAddresses.contains(address)) {
				continue;
			}

			return msg;
		}
	}

	@Override
	public void push(Message message) {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream(MAX_BUFSIZE);

			message.setSourcePort(rmiPort);
			Message.generateMessage(message, baos);

			byte[] buf = baos.toByteArray();
			DatagramPacket packet = new DatagramPacket(buf, buf.length, group, multicastPort);

			socket.send(packet);
		} catch (IOException e) {
			log.error("Failed to send message: " + message, e);
		}
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
