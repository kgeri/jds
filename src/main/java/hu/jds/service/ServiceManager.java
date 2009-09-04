package hu.jds.service;

import hu.jds.service.messages.MulticastMessageQueue;
import hu.jds.service.proxy.IServiceProxy;
import hu.jds.service.proxy.LoadBalancingServiceProxy;
import hu.jds.service.rmi.RmiServiceClient;
import hu.jds.service.rmi.RmiServiceProxy;
import hu.jds.service.utils.ProcessUtils;

import java.io.IOException;
import java.net.BindException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.RemoteStub;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

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
	private Map<RemoteServiceDescriptor, Remote> publishedServices = new ConcurrentHashMap<RemoteServiceDescriptor, Remote>();

	/** The discovered remote services. */
	private Map<RemoteServiceDescriptor, Object> remoteServices = new ConcurrentHashMap<RemoteServiceDescriptor, Object>();

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
			mq = new MulticastMessageQueue(discoveryGroup, discoveryPort);
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

			Object bean = new RmiServiceClient(service);
			remoteServices.put(service, bean);
			addService(service, bean);
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
			throw new ServiceException("No services were found for interface: " + iface);
		}
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

		Remote svc;

		try {

			if (bean instanceof Remote) {
				svc = (Remote) bean;
			} else {
				svc = new RmiServiceProxy(service.serviceInterface, bean);
			}

			UnicastRemoteObject.exportObject(svc, rmiPort);
			registry.rebind(name, RemoteStub.toStub(svc));

			publishedServices.put(rsd, svc);
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
