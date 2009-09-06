package org.jds.spring;

import org.jds.service.LocalServiceDescriptor;
import org.springframework.beans.factory.BeanFactory;


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
