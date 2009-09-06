package org.jds.service;

/**
 * An interface for beans which can retrieve remote services.
 *
 * @author Gergely Kiss
 *
 */
public interface IServiceLocator {

	/**
	 * Returns a service proxy of the specified interface.
	 * 
	 * @param <T>
	 * @param iface
	 * 
	 * @return A (possibly load balanced) proxy for the given service interface,
	 *         or null if the service can not be found
	 */
    <T> T getService(Class<T> iface);
}
