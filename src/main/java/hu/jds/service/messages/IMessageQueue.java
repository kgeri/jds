package hu.jds.service.messages;

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
	 * This method blocks until the next message is received.
	 * 
	 * @return The received message, or an exception if an error has occurred
	 */
	Message pop() throws Exception;
}
