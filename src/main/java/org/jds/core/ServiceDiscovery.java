package org.jds.core;

import org.jds.core.messages.IMessageQueue;
import org.jds.core.messages.ListRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * A thread for finding distributed services on the local network.
 * 
 * <p>
 * Service listing requests are sent every <code>servicePollTime</code> seconds,
 * but whenever a response is received, the poll time increases (doubled). The
 * current poll time is limited to <code>servicePollTime * 10</code>.
 * </p>
 * 
 * @author Gergely Kiss
 */
class ServiceDiscovery extends Thread {
	private final Logger log = LoggerFactory.getLogger(ServiceDiscovery.class);

	private final IMessageQueue mq;
	private int servicePollTime = 5;
	private int currentServicePollTime;

	public ServiceDiscovery(IMessageQueue mq) {
		super("JDS Discovery");

		this.mq = mq;
		this.currentServicePollTime = servicePollTime;
		setDaemon(true);
	}

	@Override
	public void run() {
		log.info(getName() + " started");

		try {

			while (true) {
				log.trace("Sending request message...");

				ListRequest req = new ListRequest();
				mq.push(req);

				for (int i = 0; i < currentServicePollTime; i++) {
					sleep(1000);
				}

				if (currentServicePollTime > servicePollTime) {
					currentServicePollTime /= 2;
				}
			}
		} catch (InterruptedException e) {
			log.error(getName() + " interrupted", e);
		}
	}

	/**
	 * Triggers a request send delay whenever a response is received.
	 */
	void responseReceived() {
		if (currentServicePollTime < servicePollTime * 10) {
			currentServicePollTime *= 2;
		}
	}
}
