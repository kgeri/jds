package hu.jds.service;

import hu.jds.spring.DistributedService;

@DistributedService
public interface ISharedService {
	void testCall(String value);
}
