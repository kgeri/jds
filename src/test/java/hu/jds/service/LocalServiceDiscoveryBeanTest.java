package hu.jds.service;

import static org.junit.Assert.assertNotNull;

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

	@Test
	public void testLocalServiceDiscovery() {
		ISharedService service = manager.getService(ISharedService.class);
		assertNotNull(service);
	}
}
