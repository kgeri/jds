package org.jds.core.proxy;

/**
 * Service facade interface.
 * 
 * <p>
 * An {@link IServiceProxy} is an interface for any object that hides some
 * complex operation involving one or more services behind the original service
 * interface.
 * </p>
 * 
 * @author Gergely Kiss
 */
public interface IServiceProxy {

	/**
	 * Returns the facade.
	 * 
	 * @return
	 */
	Object getProxy();

	/**
	 * Returns the proxied interface.
	 * 
	 * @return
	 */
	Class<?> getServiceInterface();
}
