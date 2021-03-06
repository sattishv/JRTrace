/**
 * (c) 2014 by Christian Schenk
 **/
package de.schenk.jrtrace.service.test.utils;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;

import de.schenk.jrtrace.helper.JRTraceNameUtil;
import de.schenk.jrtrace.service.ClassUtil;
import de.schenk.jrtrace.service.JarLocator;
import de.schenk.jrtrace.service.internal.PortUtil;

public class JavaUtil {

	private static Process javaProcess = null;
	private int killPort;

	public Thread startTestProcessInSameVM() {
		Thread t = new Thread() {
			@Override
			public void run() {
				TestProcess
						.main(new String[] { String.format("%d", killPort) });
			}
		};
		t.start();
		return t;
	}

	/**
	 * launches the java process without any specific agent.
	 * 
	 */
	public void launchJavaProcess() throws IOException, URISyntaxException,
			InterruptedException {

		launchJavaProcess("", null);
	}

	/**
	 * convenience to call launchJavaProcessWithAgent(outputFileName,0)
	 * 
	 * @param outputFileName
	 * @return
	 * @throws IOException
	 * @throws URISyntaxException
	 * @throws InterruptedException
	 */
	public int launchJavaProcessWithAgent(String outputFileName)
			throws IOException, URISyntaxException, InterruptedException {
		return launchJavaProcessWithAgent(outputFileName, 0);
	}

	/**
	 * launches the java test process with an agent listening on a free port
	 * 
	 * @param outputFileName
	 *            : if, set the testprocess will write an outputfile to this
	 *            location containing the value int
	 * 
	 * @param buffersize
	 *            to set a non-default notification buffer size for JMX
	 *            messages, 0 for default.
	 * @return
	 * @throws IOException
	 * @throws URISyntaxException
	 * @throws InterruptedException
	 * @returns the port with which the agent was started.
	 */
	public int launchJavaProcessWithAgent(String outputFileName, int buffersize)
			throws IOException, URISyntaxException, InterruptedException {
		int freePort = PortUtil.getFreePort();
		String agentPath = JarLocator.getJRTraceHelperAgent();
		String bootjarPath = JarLocator.getHelperLibJar();
		String prop = String.format("-javaagent:%s=port=%d,bootjar=%s",
				agentPath, freePort, bootjarPath);
		if (buffersize != 0)
			prop = String.format("%s,notificationBufferSize=%d", prop,
					buffersize);
		launchJavaProcess(prop, outputFileName);
		return freePort;
	}

	/**
	 * 
	 * 
	 * @param runAgent
	 *            if true, passes the agent on the command line already
	 * @throws IOException
	 * @throws URISyntaxException
	 * @throws InterruptedException
	 */
	public void launchJavaProcess(String parameters, String parameters2)
			throws IOException, URISyntaxException, InterruptedException {
		String javaHome = System.getProperty("java.home");

		String fullPath = getClassPathForClass(TestProcess.class);
		ArrayList<String> commandParameters = new ArrayList<String>();
		commandParameters.add(javaHome + File.separator + "bin"
				+ File.separator + "java");
		if (parameters != null && !parameters.isEmpty()) {
			commandParameters.add(parameters);
		}
		// commandParameters
		// .add("-Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=12345");

		commandParameters.add("-cp");
		commandParameters.add(fullPath);
		commandParameters.add(TestProcess.class.getName());
		obtainKillPort();
		commandParameters.add(String.format("%d", killPort));
		if (parameters2 != null)
			commandParameters.add(parameters2);

		ProcessBuilder processBuilder = new ProcessBuilder(commandParameters);
		processBuilder.redirectErrorStream(true);
		processBuilder.inheritIO();
		javaProcess = processBuilder.start();

		InputStream inputStream = javaProcess.getInputStream();

		OutputStreamPrinter outputStreamPrinter = new OutputStreamPrinter(
				inputStream);
		outputStreamPrinter.start();

	}

	/**
	 * example: for <anyPackage>.class1 in plug.in will return:
	 * g:\workspace\plug.in\bin works only for non-jar plugins.
	 * 
	 * @param class1
	 *            the class
	 * @return the valid classpath entry to launch java exe for this class
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	private String getClassPathForClass(Class<TestProcess> class1)
			throws URISyntaxException, IOException {
		String file = getFilePathFromClassOfTestBundle(class1);
		String internalclassname = JRTraceNameUtil.getInternalName(class1
				.getName());
		String pathLikeClassName = internalclassname.replace("/",
				File.separator);
		int index = file.indexOf(pathLikeClassName);

		return file.substring(0, index - 1);
	}

	/**
	 * Makes the classfile of the specified class available as file in the
	 * filesystem.
	 * 
	 * @param classname
	 *            the class
	 * @return a valid file system path to the file containing the bytecode
	 * @throws URISyntaxException
	 * @throws IOException
	 */
	public String getPathForClass(Class<?> theclass) throws URISyntaxException,
			IOException {
		String fullPath = null;

		fullPath = getFilePathFromClassOfTestBundle(theclass);

		int index = fullPath.lastIndexOf("/");
		fullPath = fullPath.substring(0, index);

		return fullPath;
	}

	/**
	 * return a file for the given class in this bundle
	 * 
	 * @param theclass
	 * @return
	 * @throws URISyntaxException
	 * @throws IOException
	 */
	public String getFilePathFromClassOfTestBundle(Class<?> theclass)
			throws URISyntaxException, IOException {
		return getFilepathForClass(theclass, "de.schenk.jrtrace.service.test");
	}

	/**
	 * return a file for the given class from the specified bundle.
	 * 
	 * @param theclass
	 * @param bundleid
	 * @return
	 * @throws URISyntaxException
	 * @throws IOException
	 */
	public String getFilepathForClass(Class<?> theclass, String bundleid)
			throws URISyntaxException, IOException {

		File theFile = getFileForClass(theclass, bundleid);
		String fullPath = theFile.getAbsolutePath();
		return fullPath;
	}

	public File getFileForClass(Class<?> theclass, String bundleid) {
		String classname = theclass.getSimpleName();
		return getFileForClass(classname, bundleid);
	}

	/**
	 * Searches for the class file of a class in a given bundle and returns a
	 * File for it.
	 * 
	 * @param classname
	 *            simple name of a class, e.g. String or Processor$1 (not the
	 *            fully qualified name.
	 * @param bundleid
	 *            the bundle id of the bundle to search for this class.
	 * @return a File that contains the class.
	 */
	public File getFileForClass(String classname, String bundleid) {

		Bundle bundle = Platform.getBundle(bundleid);
		Enumeration<URL> urls = bundle.findEntries("/", classname + ".class",
				true);

		File theFile;
		try {
			theFile = new File(FileLocator.toFileURL(urls.nextElement())
					.toURI());
		} catch (URISyntaxException | IOException e) {
			throw new RuntimeException(e);
		}
		return theFile;
	}

	/**
	 * Sends a stop request to the TestProcess and waits for completion.
	 * 
	 * @return exit value of the process or -1 if the javaprocess didn't exist
	 *         (was null).
	 * @throws InterruptedException
	 */
	public void sendKillAndWaitForEnd() throws InterruptedException {

		if (javaProcess != null) {
			while (true) {
				sendKillUDPPacket();

				try {
					int returnValue = javaProcess.exitValue();

					break;
				} catch (IllegalThreadStateException e) {
					// do nothing, try again
				}
				Thread.sleep(10);

			}

		}

	}

	public void sendKillUDPPacket() {

		try {

			DatagramSocket clientSocket = new DatagramSocket();
			InetAddress IPAddress = InetAddress.getByName("localhost");
			byte[] sendData = new byte[1];
			DatagramPacket sendPacket = new DatagramPacket(sendData,
					sendData.length, IPAddress, killPort);
			clientSocket.send(sendPacket);
			clientSocket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public String getQualifiedTestClassName() {
		return TestProcess.class.getName();
	}

	public void obtainKillPort() {
		killPort = PortUtil.getFreePort();

	}

	public byte[] getClassBytes(Class<?> c) throws IOException {

		return ClassUtil.getClassBytes(c);
	}

	public static int readIntegerFromFile(String absolutePath) throws Exception {
		File f = new File(absolutePath);
		FileReader fr = new FileReader(f);
		char[] chars = new char[500];
		int length;

		length = fr.read(chars);

		String s = new String(chars, 0, length);
		int i = Integer.parseInt(s);
		fr.close();
		return i;
	}

}
