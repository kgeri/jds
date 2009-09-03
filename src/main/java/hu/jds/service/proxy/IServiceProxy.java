package hu.jds.service.proxy;

/**
 * Service facade interface.
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
