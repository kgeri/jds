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

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ClassUtils;

/**
 * A bean for managing distributed services.
 * 
 * @author Gergely Kiss
 * 
 */
public class ServiceBean implements IServiceManager, IServiceLocator {
	private final Logger log = Logger.getLogger(ServiceBean.class);

	@Autowired
	private ApplicationContext ctx;

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

	@PostConstruct
	public void init() {

		try {
			localAddress = InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			throw new BeanInitializationException("Failed to determine local host address: "
					+ e.getLocalizedMessage());
		}

		int port;

		for (port = 1099; port < 1199; port++) {

			try {
				registry = LocateRegistry.createRegistry(port);
				log.info(String.format("Successfully connected to RMI registry on port %d", port));

				break;
			} catch (RemoteException e) {

				if (e.getCause() instanceof BindException) {
					log.info(String.format("Port %d reserved, retrying...", port));
				} else {
					log.error(String.format("Failed to bind to port: %d", port), e);
				}
			}
		}

		rmiPort = port;

		if (registry == null) {
			throw new BeanInitializationException("Failed to get RMI registry");
		}

		try {
			mq = new MulticastMessageQueue(discoveryGroup, discoveryPort);
			new ServiceListener(this, mq).start();

			findLocalServices();
		} catch (IOException e) {
			throw new BeanInitializationException("Failed to create connection", e);
		}

		log.info(String.format("Successfully initialized service node [PID:%d][RMI:%s:%d]",
				ProcessUtils.PID, localAddress, rmiPort));
	}

	@PreDestroy
	public void destroy() throws IOException {
		mq.close();
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
	public void addLocalService(ServiceDescriptor service) {
		Object bean;

		if (service instanceof ObjectServiceDescriptor) {
			bean = ((ObjectServiceDescriptor) service).bean;
		} else if (service instanceof BeanServiceDescriptor) {
			bean = ctx.getBean(service.serviceName, service.serviceInterface);
		} else {
			throw new IllegalStateException("Invalid local service type: " + service.getClass());
		}
		
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
			throw new ServiceException("No beans were found for service: " + iface);
		}
	}

	/**
	 * Locates and configures the {@link DistributedService} beans in the
	 * current bean definition context.
	 */
	private void findLocalServices() {
		BeanDefinitionRegistry bdr = (BeanDefinitionRegistry) ctx;
		String[] bdNames = bdr.getBeanDefinitionNames();

		for (String bdName : bdNames) {
			BeanDefinition bd = bdr.getBeanDefinition(bdName);

			try {
				Class<?> beanType = Class.forName(bd.getBeanClassName());
				DistributedService svc = AnnotationUtils.findAnnotation(beanType,
						DistributedService.class);

				if (svc == null) {
					continue;
				}

				Class<?>[] ifaces = ClassUtils.getAllInterfacesForClass(beanType);

				if (AnnotationUtils.isAnnotationDeclaredLocally(DistributedService.class, beanType)) {

					// The class itself is annotated, save all interfaces
					for (Class<?> iface : ifaces) {
						localServices.add(new ObjectServiceDescriptor(iface, bdName));
					}
				} else {

					// Only save annotated interfaces
					for (Class<?> iface : ifaces) {
						svc = iface.getAnnotation(DistributedService.class);

						if (svc != null) {
							addLocalService(new ObjectServiceDescriptor(iface, bdName));
						}
					}
				}

			} catch (ClassNotFoundException e) {
				log.warn("Unknown bean class: " + bd.getBeanClassName());
			}
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
