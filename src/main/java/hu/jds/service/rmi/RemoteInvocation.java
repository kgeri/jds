package hu.jds.service.rmi;

import java.io.Serializable;
import java.lang.reflect.Method;

/**
 * VO for storing all the parameters required to invoke a remote method
 * dynamically.
 * 
 * @author Gergely Kiss
 * 
 */
public class RemoteInvocation implements Serializable {
	private static final long serialVersionUID = -1641213059501005113L;

	final String methodName;
	final Class<?>[] parameterTypes;
	final Object[] args;

	public RemoteInvocation(Method method, Object... args) {
		this.methodName = method.getName();
		this.parameterTypes = method.getParameterTypes();
		this.args = args;
	}
}
