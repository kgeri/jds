package org.jds.core.messages;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

import org.jds.core.RemoteServiceDescriptor;

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
	protected void generateCustomContent(OutputStream target) throws IOException {
		ObjectOutputStream oos = new ObjectOutputStream(target);
		oos.writeObject(services);
	}

	@Override
	protected void parseCustomContent(InputStream source) throws IOException {
		try {
			ObjectInputStream ois = new ObjectInputStream(source);
			services = (RemoteServiceDescriptor[]) ois.readObject();
		} catch (ClassNotFoundException e) {
			throw new IOException(e);
		}
	}

	public void setServices(RemoteServiceDescriptor[] services) {
		this.services = services;
	}

	public RemoteServiceDescriptor[] getServices() {
		return services;
	}
}
