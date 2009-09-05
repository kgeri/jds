package hu.jds.service.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;


/**
 * Common base class for service proxies.
 *
 * @author Gergely Kiss
 */
public abstract class ServiceProxy implements InvocationHandler, IServiceProxy {
    protected final Class<?> serviceInterface;
    protected final Object proxy;

	/**
	 * Constructs a service proxy.
	 * 
	 * <p>
	 * The caller must supply at least one interface, which will be the
	 * published service interface. The proxy may implement other interfaces as
	 * well.
	 * </p>
	 * 
	 * @param ifaces
	 */
	public ServiceProxy(Class<?>... ifaces) {
		this.proxy = Proxy.newProxyInstance(ifaces[0].getClassLoader(), ifaces, this);
		this.serviceInterface = ifaces[0];
    }

    public Object getProxy() {
        return proxy;
    }

    public Class<?> getServiceInterface() {
        return serviceInterface;
    }
}
