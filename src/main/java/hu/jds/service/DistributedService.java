package hu.jds.service;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;


/**
 * Annotation for distributed services.
 *
 * @author Gergely Kiss
 *
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface DistributedService {
}
