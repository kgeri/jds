package org.jds.core.proxy;


import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.jds.core.ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A load balancing service facade.
 * 
 * <p>
 * Hides the complexity of load balanced method calls behind the service
 * interface.
 * </p>
 * 
 * @author Gergely Kiss
 */
public class LoadBalancingServiceProxy extends ServiceProxy {
	private final Logger log = LoggerFactory.getLogger(LoadBalancingServiceProxy.class);
	private final List<Object> serviceBeans = new ArrayList<Object>(2);

	public LoadBalancingServiceProxy(Class<?> iface, Object serviceBean) {
		super(iface);
		addServiceBean(serviceBean);
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		assert (serviceBeans.size() > 0);

		for (Object bean : serviceBeans) {

			try {
				return method.invoke(bean, args);
			} catch (Throwable e) {
				log.error("Failed to invoke method {} on bean {}", method.getName(), bean);
				log.debug("Failure trace", e);
			}
		}

		throw new ServiceException("All service invocations failed");
	}

	public void addServiceBean(Object serviceBean) {
		serviceBeans.add(serviceBean);
	}
}
