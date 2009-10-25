package org.jds.core.messages;

import java.io.IOException;

/**
 * A simple queue interface for the communication of the nodes.
 * 
 * @author Gergely Kiss
 * 
 */
public interface IMessageQueue {

	/**
	 * Pushes a new message in the queue.
	 * 
	 * @param message
	 */
	void push(Message message);

	/**
	 * Reads the next message from the queue.
	 * 
	 * This method blocks until the next (valid) message is received.
	 * 
	 * @return The received message
	 * 
	 * @throws IOException
	 *             If the queue failed to read a message
	 */
	Message pop() throws IOException;
}
