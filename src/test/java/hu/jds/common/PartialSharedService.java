package hu.jds.common;


public class PartialSharedService implements IPartialSharedService {
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
