package hu.jds.service;



/**
 * An interface for beans which can manage remote service instances.
 *
 * @author Gergely Kiss
 *
 */
interface IServiceManager extends IServiceLocator {

    /**
     * Returns the descriptors for the services this node can provide.
     *
     * @return
     */
    RemoteServiceDescriptor[] getPublicServices();

	/**
	 * Registers the local service.
	 * 
	 * @param service
	 */
	void addLocalService(LocalServiceDescriptor service);

	/**
	 * Registers the remote service.
	 * 
	 * @param service
	 */
    void addRemoteService(RemoteServiceDescriptor service);

}
