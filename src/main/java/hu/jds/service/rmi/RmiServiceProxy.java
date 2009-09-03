package hu.jds.service.rmi;

import hu.jds.service.proxy.ServiceProxy;

import java.lang.reflect.Method;

import java.rmi.Remote;


/**
 * RMI service wrapper.
 *
 * @author Gergely Kiss
 */
public class RmiServiceProxy extends ServiceProxy implements Remote {
    private final Object target;

    public RmiServiceProxy(Class<?> iface, Object target) {
        super(iface);
        this.target = target;
    }

    @Override public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        return method.invoke(target, args);
    }
}
