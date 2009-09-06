package org.jds.service;


/**
 * Remote service descriptor.
 *
 * @author Gergely Kiss
 *
 */
public class RemoteServiceDescriptor extends ServiceDescriptor {

    /** The host address of the service. */
    public final String serverAddress;

    /** The port of the service. */
    public final int servicePort;

    public RemoteServiceDescriptor(Class<?> serviceInterface, String serviceName,
        String serverAddress, int servicePort) {

        super(serviceInterface, serviceName);
        this.servicePort = servicePort;
        this.serverAddress = serverAddress;
    }

    /**
     * Returns the RMI URL of the service.
     *
     * Eg.: rmi://my.host.org:1099/my/services/MyInterface/myServiceName
     *
     * @return
     */
    public String getServiceURL() {
        StringBuilder buf = new StringBuilder();
        buf.append("rmi://");
        buf.append(serverAddress).append(':').append(servicePort);
        buf.append('/').append(getServiceFQN());

        return buf.toString();
    }

    @Override public String toString() {
        return getServiceURL();
    }

    @Override public int hashCode() {
        return getServiceURL().hashCode();
    }

    @Override public boolean equals(Object obj) {
        return ((RemoteServiceDescriptor) obj).getServiceURL().equals(getServiceURL());
    }
}
