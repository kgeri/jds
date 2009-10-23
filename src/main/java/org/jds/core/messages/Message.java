package org.jds.core.messages;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Base class for service messages.
 * 
 * @author Gergely Kiss
 */
public abstract class Message {
	private static int messageOrd = 0;

	public final MessageType type;
	private int sourcePort;
	private int nodeId;
	private int messageId;

	protected Message(MessageType type) {
		this.type = type;
	}

	/**
	 * Serializes the given message to the specified output stream.
	 * 
	 * @param rmiPort
	 * @param msg
	 * @param target
	 * 
	 * @throws IOException
	 */
	public static void generateMessage(Message msg, OutputStream target)
			throws IOException {
		// Message type
		target.write((byte) msg.type.ordinal());

		// Source port
		intToByte(target, msg.sourcePort);

		// Node ID
		intToByte(target, msg.nodeId);

		// Message ID
		intToByte(target, messageOrd++);

		// Custom contents
		msg.generateCustomContent(target);
	}

	/**
	 * Parses a message from the specified input stream.
	 * 
	 * @param source
	 * 
	 * @return
	 * @throws Exception
	 */
	public static Message parseMessage(InputStream source) throws Exception {
		MessageType type = MessageType.values()[source.read()];
		Message ret = type.javaType.newInstance();

		// Skip port
		ret.sourcePort = byteToInt(source);

		// Node ID
		ret.nodeId = byteToInt(source);

		// Message ID
		ret.messageId = byteToInt(source);

		// Custom contents
		ret.parseCustomContent(source);

		return ret;
	}

	/**
	 * Implementation specific content formatting.
	 * 
	 * @param target
	 * 
	 * @throws IOException
	 */
	protected void generateCustomContent(OutputStream target) throws IOException {
	}

	/**
	 * Implementation specific content parsing.
	 * 
	 * @param source
	 * 
	 * @throws ClassNotFoundException
	 * @throws IOException
	 */
	protected void parseCustomContent(InputStream source) throws IOException,
			ClassNotFoundException {
	}

	@Override
	public String toString() {
		return nodeId + " - " + type + "#" + messageId;
	}

	protected static void intToByte(OutputStream target, int i) throws IOException {
		byte[] b = new byte[4];

		b[0] = (byte) (0xFF & ((i >> 24) - 128));
		b[1] = (byte) (0xFF & ((i >> 16) - 128));
		b[2] = (byte) (0xFF & ((i >> 8) - 128));
		b[3] = (byte) (0xFF & (i - 128));

		target.write(b, 0, b.length);
	}

	protected static final int byteToInt(InputStream source) throws IOException {
		byte[] b = new byte[4];
		source.read(b);

		int ret = 0;
		ret += (b[0] + 128) << 24;
		ret += (b[1] + 128) << 16;
		ret += (b[2] + 128) << 8;
		ret += (b[3] + 128);

		return ret;
	}

	protected static final int byteToInt(byte[] buf, int offset) {
		int ret = 0;
		ret += (buf[0 + offset] + 128) << 24;
		ret += (buf[1 + offset] + 128) << 16;
		ret += (buf[2 + offset] + 128) << 8;
		ret += (buf[3 + offset] + 128);

		return ret;
	}

	public int getNodeId() {
		return nodeId;
	}

	public void setNodeId(int nodeId) {
		this.nodeId = nodeId;
	}

	public int getMessageId() {
		return messageId;
	}

	public int getSourcePort() {
		return sourcePort;
	}

	public void setSourcePort(int sourcePort) {
		this.sourcePort = sourcePort;
	}
}
