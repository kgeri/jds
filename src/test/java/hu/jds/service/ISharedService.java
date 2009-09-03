package hu.jds.service;

@DistributedService
public interface ISharedService {
	void testCall(String value);
}
