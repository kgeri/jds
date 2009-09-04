package hu.jds.service;


/**
 * A service descriptor for local services.
 * 
 * @author Gergely Kiss
 * 
 */
public class LocalServiceDescriptor extends ServiceDescriptor {
	/** The service object which may be called. */
	protected Object service;

	public LocalServiceDescriptor(Class<?> serviceInterface, String serviceName, Object service) {
		super(serviceInterface, serviceName);
		this.service = service;
	}

	public Object getService() {
		return service;
	}
}
