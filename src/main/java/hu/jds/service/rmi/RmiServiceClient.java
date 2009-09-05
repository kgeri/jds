package hu.jds.service.rmi;

import hu.jds.service.RemoteServiceDescriptor;
import hu.jds.service.ServiceException;
import hu.jds.service.proxy.IServiceProxy;

import java.rmi.Naming;

/**
 * RMI client proxy for invoking service methods over RMI.
 * 
 * @author Gergely Kiss
 */
public class RmiServiceClient implements IServiceProxy {
	private final RemoteServiceDescriptor descriptor;
	private Object stub;

	public RmiServiceClient(RemoteServiceDescriptor descriptor) {
		this.descriptor = descriptor;
		lookupStub();
	}

	protected void lookupStub() {

		try {
			stub = Naming.lookup(descriptor.getServiceURL());
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
			throw new ServiceException("Failed to lookup service: " + descriptor.getServiceURL(), e);
		}
	}

	@Override
	public Object getProxy() {
		if (stub == null) {
			lookupStub();
		}

		return stub;
	}

	@Override
	public Class<?> getServiceInterface() {
		return descriptor.serviceInterface;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((descriptor == null) ? 0 : descriptor.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RmiServiceClient other = (RmiServiceClient) obj;
		if (descriptor == null) {
			if (other.descriptor != null)
				return false;
		} else if (!descriptor.equals(other.descriptor))
			return false;
		return true;
	}
}
