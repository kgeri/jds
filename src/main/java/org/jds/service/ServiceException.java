package org.jds.service;

/**
 * A common service exception.
 * 
 * <p>
 * May be thrown on unsuccessful service or {@link ServiceManager}
 * initializaton, lookup, or failed service method call.
 * </p>
 * 
 * @author Gergely Kiss
 */
public class ServiceException extends RuntimeException {
    public ServiceException() {
    }

    public ServiceException(String message) {
        super(message);
    }

    public ServiceException(Throwable cause) {
        super(cause);
    }

    public ServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
