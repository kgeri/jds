package hu.jds.service.rmi;

import hu.jds.service.RemoteServiceDescriptor;
import hu.jds.service.ServiceException;
import hu.jds.service.proxy.ServiceProxy;

import java.lang.reflect.Method;

import java.rmi.Naming;


/**
 * RMI client proxy.
 *
 * @author Gergely Kiss
 */
public class RmiServiceClient extends ServiceProxy {
    private final RemoteServiceDescriptor descriptor;
    private Object stub;

    public RmiServiceClient(RemoteServiceDescriptor descriptor) {
        super(descriptor.serviceInterface);
        this.descriptor = descriptor;
        lookupStub();
    }

    protected void lookupStub() {

        try {
            stub = Naming.lookup(descriptor.getServiceURL());
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
            throw new ServiceException("Failed to lookup service: " + descriptor.getServiceURL(),
                e);
        }
    }

    @Override public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        if (stub == null) {
            lookupStub();
        }

        return method.invoke(stub, args);
    }

}
