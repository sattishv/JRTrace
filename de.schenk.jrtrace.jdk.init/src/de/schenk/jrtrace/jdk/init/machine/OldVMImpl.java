/**
 * (c) 2014 by Christian Schenk
 **/
package de.schenk.jrtrace.jdk.init.machine;

import java.io.IOException;

import com.sun.tools.attach.AgentInitializationException;
import com.sun.tools.attach.AgentLoadException;
import com.sun.tools.attach.AttachNotSupportedException;
import com.sun.tools.attach.VirtualMachine;

import de.schenk.jrtrace.jdk.init.machine.VirtualMachineException;
import de.schenk.jrtrace.jdk.init.machine.VirtualMachineWrapper;


public class OldVMImpl {

	VirtualMachineWrapper vm;
	private String thePID;
	private String servernetworkaddress;

	/**
	 * 
	 * @param pid
	 *            the pid to upload the agent to.
	 * @param servernetwork
	 *            the ip address name on which the server will expect a
	 *            connection or null for local connections
	 */
	public OldVMImpl(String pid, String servernetwork) {
		thePID = pid;
		this.servernetworkaddress = servernetwork;
	}

	/**
	 * 
	 * @return true if successful, excception can be retrieved via
	 *         getException() if not successful
	 */
	public boolean attach(ICancelable stopper) {
		boolean result = attachVM();
		if (!result)
			return result;

		int port = installAgent();
		if (port == -1)
			return false;

		return connectToAgent(port, null, stopper);
	}

	private boolean attachVM() {
		VirtualMachineWrapper theVM = null;
		try {
			theVM = VirtualMachineWrapper.attach(thePID);
		} catch (VirtualMachineException e) {
			lastException = e;
			return false;

		} catch (IOException e) {
			lastException = e;
			return false;
		}

		vm = theVM;
		return true;
	}

	private int installAgent() {

		port = PortUtil.getFreePort();
		try {
			String agent = JarLocator.getJRTraceHelperAgent();
			String helperPath = JarLocator.getHelperLibJar();
			String mynetwork = servernetworkaddress == null ? ""
					: (",server=" + servernetworkaddress);
			vm.loadAgent(agent, String.format("port=%d,bootjar=%s%s", port,
					helperPath, mynetwork));

		} catch (AgentInitializationException e) {
			lastException = e;
			return -1;
		} catch (AgentLoadException e) {
			lastException = e;
			return -1;
		} catch (IOException e) {
			lastException = e;
			return -1;
		}
		return port;
	}

	public boolean detach() {
		boolean result = true;
		boolean result2 = true;

		if (!detachVM()) {
			result2 = false;
		}
		result = stopConnection(false);
		return result & result2;

	}

	private boolean detachVM() {
		try {
			if (vm != null) {
				vm.detach();
			}
		} catch (IOException e) {
			lastException = e;
			return false;
		}
		return true;
	}

	@Override
	public String getConnectionIdentifier() {
		return thePID;
	}

	@Override
	public String toString() {
		return String
				.format("Agent installed into process: %s  (JRTrace Server on port: %d)",
						thePID, port);
	}

}
