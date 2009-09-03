package hu.jds.service.messages;


/**
 * Service listing request.
 * 
 * @author Gergely Kiss
 * 
 */
public class ListRequest extends Message {

	public ListRequest() {
		super(MessageType.LIST_REQUEST);
	}
}
