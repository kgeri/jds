package org.jds.core.messages;

import java.net.InetAddress;

import org.jds.core.utils.NetworkUtils;
import org.jds.core.utils.ProcessUtils;

/**
 * Base class for message queues.
 * 
 * @author Gergely Kiss
 */
public abstract class AbstractNodeMessageQueue implements IMessageQueue {
	private static int queueSerialNo = 0;

	protected final int processId = ProcessUtils.PID;
	protected final int queueId = queueSerialNo++;

	/**
	 * Returns true if the given message has originated from this queue.
	 * 
	 * @param message
	 *            The parsed message
	 * @param sourceAddress
	 *            The address where this message came from
	 * 
	 * @return
	 */
	protected boolean isLocalMessage(Message message, InetAddress sourceAddress) {
		return message.getProcessId() == processId && message.getQueueId() == queueId
				&& NetworkUtils.getLocalAddresses().contains(sourceAddress);
	}
}
