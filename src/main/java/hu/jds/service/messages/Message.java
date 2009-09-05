package hu.jds.service.messages;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Base class for service messages.
 * 
 * @author Gergely Kiss
 */
public abstract class Message {
	private static final Map<MessageType, Class<? extends Message>> typesMap;

	static {
		typesMap = new HashMap<MessageType, Class<? extends Message>>();
		typesMap.put(MessageType.LIST_REQUEST, ListRequest.class);
		typesMap.put(MessageType.SERVICE_RESPONSE, ServiceResponse.class);
	}

	private static int messageOrd = 0;

	public static final int MAX_BUFSIZE = 4096;

	/** Message types. */
	protected enum MessageType {
		LIST_REQUEST, SERVICE_RESPONSE;
	}

	public final MessageType type;
	public int nodeId;
	public int messageId;

	protected Message(MessageType type) {
		this.type = type;
	}

	public byte[] generate(int nodeId, int rmiPort) throws IOException {
		ByteArrayOutputStream target = new ByteArrayOutputStream(MAX_BUFSIZE);

		// Message type
		target.write((byte) type.ordinal());

		// Source port
		intToByte(target, rmiPort);

		// Node ID
		intToByte(target, nodeId);

		// Message ID
		intToByte(target, messageOrd++);

		// Custom contents
		generateCustomContent(target);

		byte[] msg = target.toByteArray();
		assert (msg.length <= MAX_BUFSIZE);

		return msg;
	}

	public void parse(byte[] msg) throws IOException, ClassNotFoundException {
		ByteArrayInputStream source = new ByteArrayInputStream(msg);

		// Skip final message type
		source.read();

		// Skip port
		byteToInt(source);

		// Node ID
		nodeId = byteToInt(source);

		// Message ID
		messageId = byteToInt(source);

		// Custom contents
		parseCustomContent(source);
	}

	public static int getPort(byte[] data) {
		return byteToInt(data, 1);
	}

	public static Message parseMessage(byte[] msg) throws Exception {
		MessageType type = MessageType.values()[msg[0]];
		Class<? extends Message> clss = typesMap.get(type);

		Message ret = clss.newInstance();
		ret.parse(msg);

		return ret;
	}

	/**
	 * Implementation specific content formatting.
	 * 
	 * @param target
	 * @throws IOException
	 */
	protected void generateCustomContent(ByteArrayOutputStream target) throws IOException {
	}

	/**
	 * Implementation specific content parsing.
	 * 
	 * @param source
	 * 
	 * @throws ClassNotFoundException
	 * @throws IOException
	 */
	protected void parseCustomContent(ByteArrayInputStream source) throws IOException,
			ClassNotFoundException {
	}

	@Override
	public String toString() {
		return nodeId + " - " + type + "#" + messageId;
	}

	protected static void intToByte(ByteArrayOutputStream target, int i) {
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
}
