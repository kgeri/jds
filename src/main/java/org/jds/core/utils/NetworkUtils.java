package org.jds.core.utils;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class NetworkUtils {
	private static final Logger log = LoggerFactory.getLogger(NetworkUtils.class);

	/** The local IP addresses of the current process. */
	private static Set<InetAddress> LocalAddresses;

	/**
	 * Returns all addresses from all currently available interfaces.
	 * 
	 * <p>
	 * The addresses are cached. The cache may be reloaded with
	 * reloadLocalAddresses(), in case a new interface has jus appeared.
	 * </p>
	 * 
	 * @return
	 */
	public static Set<InetAddress> getLocalAddresses() {
		if (LocalAddresses == null) {
			reloadLocalAddresses();
		}

		return LocalAddresses;
	}

	/**
	 * (Re)loads the local address cache.
	 * 
	 * <p>
	 * The local address cache contains all <code>InetAddress</code>es from
	 * every available network interface.
	 * </p>
	 */
	public static void reloadLocalAddresses() {
		try {
			log.debug("Looking for network interfaces...");

			LocalAddresses = new HashSet<InetAddress>();
			Enumeration<NetworkInterface> nis;
			nis = NetworkInterface.getNetworkInterfaces();
			while (nis.hasMoreElements()) {
				NetworkInterface ni = (NetworkInterface) nis.nextElement();
				Enumeration<InetAddress> addrs = ni.getInetAddresses();

				log.debug("Found interface: {}", ni.getName());

				while (addrs.hasMoreElements()) {
					InetAddress addr = (InetAddress) addrs.nextElement();

					log.debug("\t address: {}", addr.getHostAddress());
					LocalAddresses.add(addr);
				}
			}
		} catch (SocketException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Silently closes the specified socket.
	 * 
	 * @param socket
	 */
	public static void close(Socket socket) {
		if (socket == null) {
			return;
		}

		try {
			socket.close();
		} catch (IOException e) {
		}
	}
}
