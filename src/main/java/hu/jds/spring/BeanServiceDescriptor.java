package hu.jds.spring;

import org.springframework.beans.factory.BeanFactory;

import hu.jds.service.LocalServiceDescriptor;

/**
 * A service descriptor for Spring beans.
 * 
 * @author Gergely Kiss
 * 
 */
class BeanServiceDescriptor extends LocalServiceDescriptor {

	public BeanServiceDescriptor(Class<?> serviceInterface, String beanName, BeanFactory ctx) {
		super(serviceInterface, beanName, ctx);
	}

	@Override
	public Object getService() {
		return ((BeanFactory) service).getBean(serviceName);
	}
}
