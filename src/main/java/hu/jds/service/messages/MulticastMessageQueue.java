package hu.jds.service.messages;

import hu.jds.service.utils.NetworkUtils;
import hu.jds.service.utils.ProcessUtils;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A message queue implementation for multicast sockets.
 * 
 * @author Gergely Kiss
 * 
 */
public class MulticastMessageQueue implements IMessageQueue {
	private final Logger log = LoggerFactory.getLogger(MulticastMessageQueue.class);

	private MulticastSocket socket;
	private InetAddress group;
	private int port;

	public MulticastMessageQueue(String group, int port) throws IOException {
		this.port = port;
		this.group = InetAddress.getByName(group);
		socket = new MulticastSocket(port);
		socket.joinGroup(this.group);
	}

	@Override
	public Message pop() throws Exception {
		byte[] buf = new byte[Message.MAX_BUFSIZE];

		while (true) {
			DatagramPacket packet = new DatagramPacket(buf, buf.length);
			socket.receive(packet);

			// Skipping local packets
			String address = packet.getAddress().getHostAddress();
			if (packet.getPort() == port && NetworkUtils.LocalAddresses.contains(address)) {
				continue;
			}

			return Message.parseMessage(packet.getData());
		}
	}

	@Override
	public void push(Message message) {
		try {
			byte[] buf = message.generate(ProcessUtils.PID);
			DatagramPacket packet = new DatagramPacket(buf, buf.length, group, port);

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
