package hu.jds.service;

import hu.jds.spring.DistributedService;

@DistributedService
public class SharedService implements ISharedService {

	public void testCall(String value) {
		System.err.println("Test Service CALL");
	}
}
