package org.jds.spring;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import org.jds.common.IPartialSharedService;
import org.jds.common.ISharedService;
import org.jds.common.PartialSharedService;
import org.jds.common.SharedService;
import org.jds.core.LocalServiceDescriptor;
import org.jds.core.ServiceManager;
import org.jds.core.proxy.IServiceProxy;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@ContextConfiguration
@RunWith(SpringJUnit4ClassRunner.class)
public class LocalServiceDiscoveryBeanTest {
	@Autowired
	ServiceManager manager;

	@Autowired
	SharedService sharedService;

	@Autowired
	PartialSharedService partialSharedService;

	SharedService localService = new SharedService();

	@Before
	public void setUp() {
		manager.addLocalService(new LocalServiceDescriptor(ISharedService.class, "localService",
				localService));
	}

	@After
	public void tearDown() {
		manager.removeAllServices();
	}

	@Test
	public void testLocalServiceDiscovery() {
		ISharedService service = manager.getService(ISharedService.class);
		assertNotNull(service);

		IPartialSharedService partialService = manager.getService(IPartialSharedService.class);
		assertNotNull(partialService);
	}

	@Test
	public void testServiceCall() {
		ISharedService service = manager.getService(ISharedService.class);
		service.testCall("woot");
	}

	@Test
	public void testGetProxy() {
		IServiceProxy proxy = manager.getProxy(ISharedService.class);
		assertNotNull(proxy);
		assertEquals(proxy.getServiceInterface(), ISharedService.class);
	}

	@Test
	public void testFailedServiceCall() {
		localService.enabled(false);
		sharedService.enabled(false);
		partialSharedService.enabled(false);

		try {
			ISharedService service = manager.getService(ISharedService.class);
			assertNotNull(service);
			service.testCall("woot");
			fail("Expected exception");
		} catch (Exception e) {
		}
	}
}
