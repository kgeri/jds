package org.jds.core;

import java.io.Serializable;


/**
 * Common base class for describing services.
 *
 * @author Gergely Kiss
 */
public abstract class ServiceDescriptor implements Serializable {

    /** The interface of the service. */
	public final Class<?> serviceInterface;

    /** The name of the service. */
    public final String serviceName;

    public ServiceDescriptor(Class<?> serviceInterface, String serviceName) {
        this.serviceInterface = serviceInterface;
        this.serviceName = serviceName;
    }

    /**
     * Returns the fully qualified name of this service.
     *
     * <p>
     * Eg.: the service bean called <code>mysvc</code> with the interface
     * <code>my.services.MyInterface</code><br>
     * becomes: <code>my/services/MyInterface/mysvc</code>
     * </p>
     *
     * @return
     */
    public String getServiceFQN() {
        String svcn = serviceName;
        String svcif = serviceInterface.getName();

        svcn = svcn.replace('#', '-');
        svcif = svcif.replace('.', '/');

        return svcif + "/" + svcn;
    }

    @Override public String toString() {
        return getServiceFQN();
    }

    @Override public int hashCode() {
        return getServiceFQN().hashCode();
    }

    @Override public boolean equals(Object obj) {
        return ((ServiceDescriptor) obj).getServiceFQN().equals(getServiceFQN());
    }
}
