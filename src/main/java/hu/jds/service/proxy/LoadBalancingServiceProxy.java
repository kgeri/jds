package hu.jds.service.proxy;

import hu.jds.service.ServiceException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.lang.reflect.Method;

import java.util.HashSet;
import java.util.Set;

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
public class LoadBalancingServiceProxy extends ServiceProxy implements IServiceProxy {
	private final Logger log = LoggerFactory.getLogger(LoadBalancingServiceProxy.class);
	private final Set<Object> serviceBeans = new HashSet<Object>();

	public LoadBalancingServiceProxy(Class<?> iface, Object serviceBean) {
		super(iface);
		this.serviceBeans.add(serviceBean);
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		Assert.notEmpty(serviceBeans);

		for (Object bean : serviceBeans) {

			try {
				return method.invoke(bean, args);
			} catch (Throwable e) {
				log.error("Failed to invoke method {} on bean {}", method.getName(), bean);
			}
		}

		throw new ServiceException("All service invocations failed");
	}

	public void addServiceBean(Object serviceBean) {
		serviceBeans.add(serviceBean);
	}
}
