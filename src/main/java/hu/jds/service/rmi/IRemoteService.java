package hu.jds.service.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Remote interface for publishing arbitrary services.
 * 
 * <p>
 * Java RMI is only supported on interfaces which extend java.rmi.Remote,
 * <b>and</b> throw java.rmi.RemoteException. Because of this limitation,
 * remotely invoking an arbitrary interface is only possible if there is a
 * proper remote interface for method invocation itself.
 * </p>
 * 
 * @author Gergely Kiss
 * 
 */
public interface IRemoteService extends Remote {

	/**
	 * Invokes the remote method with the given parameters.
	 * 
	 * @param inv
	 * @return
	 * @throws RemoteException
	 */
	Object invoke(RemoteInvocation inv) throws RemoteException;

	/**
	 * Returns the service interfaces supported by this remote instance.
	 * 
	 * @return
	 * @throws RemoteException
	 */
	Class<?>[] getServiceInterfaces() throws RemoteException;
}
