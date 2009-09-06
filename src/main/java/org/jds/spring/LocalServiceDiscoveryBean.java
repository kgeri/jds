package org.jds.spring;

import java.rmi.Remote;


import org.jds.core.IServiceManager;
import org.jds.core.ServiceManager;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ClassUtils;

/**
 * Service locator bean.
 * 
 * <p>
 * Automagically locates and registers remote and local services to the
 * {@link ServiceManager}.
 * </p>
 * 
 * @author Gergely Kiss
 * 
 */
public class LocalServiceDiscoveryBean implements BeanFactoryPostProcessor {

	/**
	 * Locates and configures the {@link DistributedService} beans in the
	 * current bean definition context.
	 */
	@Override
	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory)
			throws BeansException {
		IServiceManager manager = (IServiceManager) beanFactory.getBean("serviceManager",
				IServiceManager.class);
		String[] bdNames = beanFactory.getBeanDefinitionNames();

		for (String bdName : bdNames) {
			BeanDefinition bd = beanFactory.getBeanDefinition(bdName);

			try {
				Class<?> beanType = Class.forName(bd.getBeanClassName());
				DistributedService svc = AnnotationUtils.findAnnotation(beanType,
						DistributedService.class);

				if (svc == null) {
					continue;
				}

				Class<?>[] ifaces = ClassUtils.getAllInterfacesForClass(beanType);

				if (AnnotationUtils.isAnnotationDeclaredLocally(DistributedService.class, beanType)) {

					// The class itself is annotated, save all interfaces
					for (Class<?> iface : ifaces) {
						if (Remote.class.equals(iface)) {
							continue;
						}

						manager.addLocalService(new BeanServiceDescriptor(iface, bdName,
								beanFactory));
					}
				} else {

					// Only save annotated interfaces
					for (Class<?> iface : ifaces) {
						svc = iface.getAnnotation(DistributedService.class);

						if (svc != null) {
							manager.addLocalService(new BeanServiceDescriptor(iface, bdName,
									beanFactory));
						}
					}
				}
			} catch (ClassNotFoundException e) {
			}
		}
	}
}
