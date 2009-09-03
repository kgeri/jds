package hu.jds.service;

/**
 * A service descriptor for local beans.
 * 
 * @author Gergely Kiss
 */
public class BeanServiceDescriptor extends ServiceDescriptor {

	public BeanServiceDescriptor(Class<?> serviceInterface, String beanName) {
		super(serviceInterface, beanName);
	}
}
