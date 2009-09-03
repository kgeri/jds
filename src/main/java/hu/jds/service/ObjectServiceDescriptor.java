package hu.jds.service;

import org.springframework.util.Assert;

/**
 * A service descriptor for local objects.
 * 
 * @author Gergely Kiss
 * 
 */
public class ObjectServiceDescriptor extends ServiceDescriptor {
	/** The service object which may be called. */
	public final Object bean;

	public ObjectServiceDescriptor(Class<?> serviceInterface, Object bean) {
		super(serviceInterface, bean.toString());

		Assert.isInstanceOf(serviceInterface, bean);
		this.bean = bean;
	}
}
