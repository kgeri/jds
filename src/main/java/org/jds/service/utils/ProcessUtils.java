package org.jds.service.utils;

import java.lang.management.ManagementFactory;

/**
 * Process handling functions.
 * 
 * @author Gergely Kiss
 * 
 */
public class ProcessUtils {
	/** The current process ID. */
	public static final int PID;

	static {
		String id = ManagementFactory.getRuntimeMXBean().getName();
		PID = Integer.parseInt(id.substring(0, id.indexOf('@')));
	}
	
}