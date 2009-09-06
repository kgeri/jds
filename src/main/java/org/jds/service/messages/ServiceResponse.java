package org.jds.service.messages;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.jds.service.RemoteServiceDescriptor;

/**
 * Service listing response.
 * 
 * Response for the {@link ListRequest}.
 * 
 * @author Gergely Kiss
 * @see ListRequest
 */
public class ServiceResponse extends Message {
	private RemoteServiceDescriptor[] services;

	public ServiceResponse() {
		super(MessageType.SERVICE_RESPONSE);
	}

	@Override
	protected void generateCustomContent(ByteArrayOutputStream target) throws IOException {
		ObjectOutputStream oos = new ObjectOutputStream(target);
		oos.writeObject(services);
	}

	@Override
	protected void parseCustomContent(ByteArrayInputStream source) throws IOException,
			ClassNotFoundException {
		ObjectInputStream ois = new ObjectInputStream(source);
		services = (RemoteServiceDescriptor[]) ois.readObject();
	}

	public void setServices(RemoteServiceDescriptor[] services) {
		this.services = services;
	}

	public RemoteServiceDescriptor[] getServices() {
		return services;
	}
}
