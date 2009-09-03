package hu.jds.service.utils;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

public abstract class NetworkUtils {
	/** The local IP addresses of the current process. */
	public static final Set<String> LocalAddresses;

	static {
		try {
			LocalAddresses = new HashSet<String>();
			Enumeration<NetworkInterface> nis;
			nis = NetworkInterface.getNetworkInterfaces();
			while (nis.hasMoreElements()) {
				NetworkInterface ni = (NetworkInterface) nis.nextElement();
				Enumeration<InetAddress> addrs = ni.getInetAddresses();
				while (addrs.hasMoreElements()) {
					InetAddress addr = (InetAddress) addrs.nextElement();
					LocalAddresses.add(addr.getHostAddress());
				}
			}
		} catch (SocketException e) {
			throw new RuntimeException(e);
		}
	}
}
