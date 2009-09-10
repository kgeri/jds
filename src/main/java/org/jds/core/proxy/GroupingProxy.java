package org.jds.core.proxy;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.jds.core.ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A service grouping facade.
 * 
 * <p>
 * Hides the complexity of grouped method calls behind the service interface.
 * </p>
 * 
 * @author Gergely Kiss
 */
public class GroupingProxy extends ServiceProxy {
	private final Logger log = LoggerFactory.getLogger(GroupingProxy.class);
	private final List<Object> serviceBeans = new ArrayList<Object>(2);

	public GroupingProxy(Class<?> iface, Object service) {
		super(iface);
		addService(service);
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		assert (serviceBeans.size() > 0);

		// TODO support different types of load balancing
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

	public void addService(Object service) {
		serviceBeans.add(service);
	}
}
