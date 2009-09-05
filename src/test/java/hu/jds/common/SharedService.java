package hu.jds.common;

import hu.jds.spring.DistributedService;

@DistributedService
public class SharedService implements ISharedService {
	private boolean enabled = true;

	public void testCall(String value) {
		if (enabled) {
			System.err.println("Test Service CALL: " + value);
		} else {
			throw new UnsupportedOperationException();
		}
	}

	public void enabled(boolean enabled) {
		this.enabled = enabled;
	}
}
