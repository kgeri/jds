package org.jds.core.rmi;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * A proxy for enabling dynamic method invocation on the client side.
 * 
 * @author Gergely Kiss
 * 
 */
public class RemoteClientProxy implements InvocationHandler {
	private final IRemoteService stub;
	private Object proxy;

	/**
	 * Creates a client proxy for the given remote dynamic service with the
	 * given interfaces.
	 * 
	 * <p>
	 * Please note that the availability of the supplied interfaces is not
	 * checked, so runtime errors may occur if any of the supplied interfaces
	 * are not implemented by the remote object.
	 * </p>
	 * 
	 * @param stub
	 * @param serviceInterfaces
	 */
	public RemoteClientProxy(IRemoteService stub, Class<?>... serviceInterfaces) {
		this.stub = stub;
		this.proxy = Proxy.newProxyInstance(serviceInterfaces[0].getClassLoader(),
				serviceInterfaces, this);
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		return stub.invoke(new RemoteInvocation(method, args));
	}

	/**
	 * Returns a proxy for the remote object.
	 * 
	 * @return
	 */
	public Object getProxy() {
		return proxy;
	}
}
