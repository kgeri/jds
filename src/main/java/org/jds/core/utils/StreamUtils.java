package org.jds.core.utils;

import java.io.Closeable;

/**
 * Stream handling utilities.
 * 
 * @author Gergely Kiss
 */
public class StreamUtils {

	/**
	 * Silently closes the specified stream.
	 * 
	 * @param stream
	 */
	public static void close(Closeable stream) {
		if (stream == null) {
			return;
		}

		try {
			stream.close();
		} catch (Exception e) {
		}
	}
}
