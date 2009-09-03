package hu.jds.service;

import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import hu.jds.service.utils.NetworkUtils;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@ContextConfiguration
@RunWith(SpringJUnit4ClassRunner.class)
public class ServiceBeanTest {
	@Autowired
	ServiceBean svc;

	@Autowired
	ISharedService mock;

	public ServiceBeanTest() {
		// System.setProperty("java.rmi.server.logCalls", "true");
		// System.setProperty("sun.rmi.dgc.logLevel", "VERBOSE");
		// System.setProperty("sun.rmi.server.logLevel", "VERBOSE");
		// System.setProperty("sun.rmi.client.logLevel", "VERBOSE");
	}

	@Test
	public void testServiceBean() throws InterruptedException {
		// Hack: accept packets from localhost
		NetworkUtils.LocalAddresses.clear();
		
		// Wait for bean init
		Thread.sleep(1000);
		
		svc.addLocalService(new ObjectServiceDescriptor(ISharedService.class, mock));

		mock.testCall("test");
		replay(mock);
		
		svc.getService(ISharedService.class).testCall("test");
		verify(mock);
	}
}
