package org.jds.core.messages;

/** Supported message types. */
enum MessageType {
	LIST_REQUEST(ListRequest.class), SERVICE_RESPONSE(ServiceResponse.class);

	final Class<? extends Message> javaType;

	MessageType(Class<? extends Message> javaType) {
		this.javaType = javaType;
	}
}