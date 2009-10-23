package org.jds.core;


import java.net.SocketException;

import org.jds.core.messages.IMessageQueue;
import org.jds.core.messages.ListRequest;
import org.jds.core.messages.Message;
import org.jds.core.messages.ServiceResponse;
import org.jds.core.utils.ProcessUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A thread for listening to service discovery requests.
 * 
 * @author Gergely Kiss
 */
class ServiceListener extends Thread {
	private final Logger log = LoggerFactory.getLogger(ServiceListener.class);

	private final IServiceManager manager;
	private final IMessageQueue mq;

	private ServiceDiscovery discoverer;

	public ServiceListener(IServiceManager manager, IMessageQueue mq) {
		super("JDS Listener");
		setDaemon(true);

		this.manager = manager;
		this.mq = mq;
		this.discoverer = new ServiceDiscovery(mq);
	}

	@Override
	public void run() {
		discoverer.start();
		log.info(getName() + " started");

		while (true) {
			try {
				Message msg = mq.pop();
				handle(msg);
			} catch (SocketException e) {
				log.error("Socket error: " + e.getLocalizedMessage());
				break;
			} catch (Exception e) {
				log.error("Failed to receive message", e);
			}
		}
	}

	private void handle(Message msg) {

		try {
			log.debug("Received: " + msg);

			if (msg instanceof ListRequest) {
				RemoteServiceDescriptor[] services = manager.getPublicServices();
				log.trace("Sending service response ({} services)", services.length);

				ServiceResponse resp = new ServiceResponse();
				resp.setNodeId(ProcessUtils.PID);
				resp.setServices(services);

				mq.push(resp);
			} else if (msg instanceof ServiceResponse) {
				ServiceResponse resp = (ServiceResponse) msg;

				log.trace("Received {} remote service descriptors", resp.getServices().length);

				for (RemoteServiceDescriptor service : resp.getServices()) {
					manager.addRemoteService(service);
				}

				discoverer.responseReceived();
			}
		} catch (Exception e) {
			log.error("Failed to handle message: " + msg, e);
		}
	}
}
