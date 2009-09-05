package hu.jds.service.rmi;

import java.lang.reflect.Method;
import java.rmi.RemoteException;

/**
 * A wrapper for enabling dynamic service publishing.
 * 
 * @author Gergely Kiss
 * 
 */
public class RemoteServiceWrapper implements IRemoteService {
	private final Object service;
	private final Class<?>[] serviceInterfaces;

	/**
	 * Creates a dynamic wrapper for the given local service, with the given
	 * interfaces.
	 * 
	 * <p>
	 * Please note that the availability of the interfaces is not checked, so
	 * runtime errors may occur if any of the supplied are not implemented by
	 * the service.
	 * </p>
	 * 
	 * @param service
	 * @param serviceInterfaces
	 */
	public RemoteServiceWrapper(Object service, Class<?>... serviceInterfaces) {
		this.serviceInterfaces = serviceInterfaces;
		this.service = service;
	}

	@Override
	public Class<?>[] getServiceInterfaces() {
		return serviceInterfaces;
	}

	@Override
	public Object invoke(RemoteInvocation inv) throws RemoteException {
		try {
			// TODO method caching?
			Method method = service.getClass().getMethod(inv.methodName, inv.parameterTypes);
			return method.invoke(service, inv.args);
		} catch (Exception e) {
			throw new RemoteException("Failed to invoke service method " + inv.methodName + " ("
					+ e.getLocalizedMessage() + ")");
		}
	}
}
