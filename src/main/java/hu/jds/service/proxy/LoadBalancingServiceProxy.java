package hu.jds.service.proxy;

import hu.jds.service.ServiceException;

import org.apache.log4j.Logger;

import org.springframework.util.Assert;

import java.lang.reflect.Method;

import java.util.HashSet;
import java.util.Set;


/**
 * A load balancing service facade.
 *
 * @author Gergely Kiss
 */
public class LoadBalancingServiceProxy extends ServiceProxy implements IServiceProxy {
    private final Logger log = Logger.getLogger(LoadBalancingServiceProxy.class);
    private final Set<Object> serviceBeans = new HashSet<Object>();

    public LoadBalancingServiceProxy(Class<?> iface, Object serviceBean) {
        super(iface);
        this.serviceBeans.add(serviceBean);
    }

    @Override public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Assert.notEmpty(serviceBeans);

        for (Object bean : serviceBeans) {

            try {
                return method.invoke(bean, args);
            } catch (Throwable e) {
                log.error(String.format("Failed to invoke method %s on bean %s", method.getName(),
                        bean));
            }
        }

        throw new ServiceException("All service invocations failed");
    }

    public void addServiceBean(Object serviceBean) {
        serviceBeans.add(serviceBean);
    }
}
