package hu.jds.spring;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Marks the annotated class or method as distributed.
 * 
 * @author Gergely Kiss
 * @see LocalServiceDiscoveryBean
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface DistributedService {
}
