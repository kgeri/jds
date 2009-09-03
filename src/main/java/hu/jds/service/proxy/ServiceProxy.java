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

    public ServiceProxy(Class<?> iface) {
        this.proxy = Proxy.newProxyInstance(iface.getClassLoader(), new Class<?>[] { iface }, this);
        this.serviceInterface = iface;
    }

    public Object getProxy() {
        return proxy;
    }

    public Class<?> getServiceInterface() {
        return serviceInterface;
    }
}
