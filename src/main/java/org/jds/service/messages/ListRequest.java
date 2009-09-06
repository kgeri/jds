package org.jds.service.messages;

/**
 * Service listing request.
 * 
 * <p>
 * Instructs the listening nodes to supply information about their published
 * services.
 * </p>
 * 
 * @author Gergely Kiss
 * @see ServiceResponse
 */
public class ListRequest extends Message {

	public ListRequest() {
		super(MessageType.LIST_REQUEST);
	}
}
