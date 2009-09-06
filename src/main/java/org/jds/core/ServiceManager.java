package org.jds.core;


import java.io.IOException;
import java.net.BindException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.ExportException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.jds.core.messages.MulticastMessageQueue;
import org.jds.core.proxy.IServiceProxy;
import org.jds.core.proxy.LoadBalancingServiceProxy;
import org.jds.core.rmi.IRemoteService;
import org.jds.core.rmi.RemoteClientProxy;
import org.jds.core.rmi.RemoteServiceWrapper;
import org.jds.core.utils.ProcessUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A distributed service manager.
 * 
 * <p>
 * The manager makes it possible to register and lookup local and remote
 * services.
 * </p>
 * 
 * @author Gergely Kiss
 * @see IServiceManager
 */
public class ServiceManager implements IServiceManager, IServiceLocator {
	private final Logger log = LoggerFactory.getLogger(ServiceManager.class);

	private int discoveryPort = 4000;
	private String discoveryGroup = "230.0.0.1";

	/** The currently cached services. */
	private transient Map<Class<?>, IServiceProxy> services = new ConcurrentHashMap<Class<?>, IServiceProxy>();

	/** The locally available services. */
	private Set<ServiceDescriptor> localServices = new HashSet<ServiceDescriptor>();

	/** The locally available, published services */
	private Map<RemoteServiceDescriptor, RemoteServiceWrapper> publishedServices = new ConcurrentHashMap<RemoteServiceDescriptor, RemoteServiceWrapper>();

	/** The discovered remote services. */
	private Map<RemoteServiceDescriptor, RemoteClientProxy> remoteServices = new ConcurrentHashMap<RemoteServiceDescriptor, RemoteClientProxy>();

	/** The localhost's address. */
	private String localAddress;

	/** The public service RMI port. */
	private int rmiPort;

	/** The RMI registry to use when publishing services. */
	private Registry registry;

	/** The message queue for sending and receiving service messages. */
	private MulticastMessageQueue mq;

	private ServiceManager() {

		try {
			localAddress = InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			throw new ServiceException("Failed to determine local host address: "
					+ e.getLocalizedMessage());
		}

		int port;

		for (port = 1099; port < 1199; port++) {

			try {
				registry = LocateRegistry.createRegistry(port);
				log.info("Successfully connected to RMI registry on port {}", port);

				break;
			} catch (RemoteException e) {

				if (e.getCause() instanceof BindException) {
					log.info("Port {} reserved, retrying...", port);
				} else if (e instanceof ExportException) {
					log.info("Port {} reserved by another RMI registry, retrying...", port);
				} else {
					log.error("Failed to bind to port: {}", port);
					log.debug("Failure trace", e);
				}
			}
		}

		rmiPort = port;

		if (registry == null) {
			throw new ServiceException("Failed to get RMI registry");
		}

		try {
			mq = new MulticastMessageQueue(discoveryGroup, discoveryPort, rmiPort);
			new ServiceListener(this, mq).start();
		} catch (IOException e) {
			throw new ServiceException("Failed to create connection", e);
		}

		log.info("Successfully initialized service node [PID:{}][RMI:{}:{}]", new Object[] {
				ProcessUtils.PID, localAddress, rmiPort });
	}

	public void setDiscoveryPort(int discoveryPort) {
		this.discoveryPort = discoveryPort;
	}

	public void setDiscoveryGroup(String discoveryGroup) {
		this.discoveryGroup = discoveryGroup;
	}

	@Override
	public void addRemoteService(RemoteServiceDescriptor service) {

		synchronized (remoteServices) {
			if (remoteServices.containsKey(service)) {
				return;
			}

			log.debug("Adding remote service: " + service);

			IRemoteService stub = null;

			try {
				stub = (IRemoteService) Naming.lookup(service.getServiceURL());
			} catch (Exception e) {
				log.error("Failed to lookup IRemoteService RMI stub at: {}", service
						.getServiceURL());
				log.debug("Failure trace", e);
				return;
			}

			RemoteClientProxy client = new RemoteClientProxy(stub, service.serviceInterface);

			remoteServices.put(service, client);
			addService(service, client.getProxy());
			log.info("Remote service added: " + service);
		}
	}

	@Override
	public void addLocalService(LocalServiceDescriptor service) {
		Object bean = ((LocalServiceDescriptor) service).getService();

		synchronized (localServices) {

			if (localServices.contains(service)) {
				return;
			}

			log.debug("Adding local service: " + service);

			localServices.add(service);
			publishService(service, bean);
			addService(service, bean);
			log.info("Local service added: " + service);
		}
	}

	@Override
	public RemoteServiceDescriptor[] getPublicServices() {
		return publishedServices.keySet().toArray(
				new RemoteServiceDescriptor[publishedServices.size()]);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getService(Class<T> iface) {
		IServiceProxy proxy = services.get(iface);

		if (proxy != null) {
			return (T) proxy.getProxy();
		} else {
			return null;
		}
	}

	/**
	 * Returns the service proxy, or null if the given interface is not proxied.
	 * 
	 * @param iface
	 * @return
	 */
	public IServiceProxy getProxy(Class<?> iface) {
		return services.get(iface);
	}

	/**
	 * Unregisters every service.
	 */
	public void removeAllServices() {
		publishedServices.clear();
		localServices.clear();
		remoteServices.clear();
		services.clear();
	}

	/**
	 * Publishes the specified service via the RMI registry.
	 * 
	 * @param service
	 * @param bean
	 */
	private void publishService(ServiceDescriptor service, Object bean) {
		RemoteServiceDescriptor rsd = new RemoteServiceDescriptor(service.serviceInterface,
				service.serviceName, localAddress, rmiPort);
		String name = rsd.getServiceFQN();

		try {
			RemoteServiceWrapper wrapper = new RemoteServiceWrapper(bean, service.serviceInterface);

			UnicastRemoteObject.exportObject(wrapper, rmiPort);
			registry.rebind(name, wrapper);

			publishedServices.put(rsd, wrapper);
			log.info("Successfully published service: " + name);
		} catch (Exception e) {
			log.error("Failed to publish service: " + name, e);
		}
	}

	private void addService(ServiceDescriptor service, Object bean) {
		IServiceProxy proxy = services.get(service.serviceInterface);

		if (proxy == null) {
			proxy = new LoadBalancingServiceProxy(service.serviceInterface, bean);
			services.put(service.serviceInterface, proxy);
		} else {
			((LoadBalancingServiceProxy) proxy).addServiceBean(bean);
		}
	}
}
