package com.greatfire.url.test.tester;

import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.sf.json.JSONObject;

import org.apache.commons.lang.SystemUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public abstract class UrlTester implements Runnable {

	protected static Log log = LogFactory.getLog(UrlTester.class);
	protected UrlQueue queue;

	private int maxConcurrentTests;
	private List<UrlTest> activeTests = new ArrayList<UrlTest>();
	private String invalidDnsServer;

	public UrlTester(int minTimeBetweenLowPriorityTests, int minTimeBetweenTestsSameHost, int maxConcurrentTests, int maxUrlsInQueue, String invalidDnsServer) {
		queue = new UrlQueue(minTimeBetweenLowPriorityTests, minTimeBetweenTestsSameHost, maxUrlsInQueue);
		this.maxConcurrentTests = maxConcurrentTests;
		this.invalidDnsServer = invalidDnsServer;

		new Thread(this).start();
	}

	@Override
	public void run() {
		while(true) {
			try {				
				ArrayList<NetworkInterface> interfaces = getNetworkInterfaces();
				if(interfaces.size() == 0) {
					log.info("No network interfaces found, waiting");
					try {
						Thread.sleep(5000);
						continue;
					} catch (InterruptedException e) {
						e.printStackTrace();
						return;
					}
				}

				// If we reached max concurrent tests, wait. Multiply maxConcurrentTests with number of interfaces since each has it's own IP and own potential restrictions.
				while(activeTests.size() > (maxConcurrentTests * interfaces.size())) {
					log.info("Too many current active tests, sleeping and waiting: " + activeTests.size());

					// Clean up existing threads
					boolean removedActiveTest = false;
					for(UrlTest ut : activeTests) {
						if(ut.isDead()) {
							activeTests.remove(ut);
							removedActiveTest = true;
							break;
						}
					}
					if(removedActiveTest) {
						continue;
					}

					try {
						Thread.sleep(1000);
						continue;
					} catch (InterruptedException e) {
						e.printStackTrace();
						return;
					}
				}

				// Wait for a URL to test
				final String url = queue.getNext();
				if(url == null) {
					if(queue.getSize() == 0) {
						urlQueueIsEmpty();
					}

					try {
						Thread.sleep(1000);
						continue;
					} catch (InterruptedException e) {
						e.printStackTrace();
						return;
					}					
				}

				// Test URL on all network interfaces
				for(final NetworkInterface interface_ : interfaces) {
					String netint;
					
					// The interface name doesn't work in curl on windows. For windows, use IP address instead.
					if(SystemUtils.IS_OS_WINDOWS) {
						netint = interface_.getInterfaceAddresses().get(0).getAddress().getHostAddress();
					} 
					
					// Non-windows, use interface name.
					else {
						netint = interface_.getName();
					}
					
					activeTests.add(new UrlTest(url, netint, invalidDnsServer) {

						@Override
						public void newResult(JSONObject testResultJSON) {
							UrlTester.this.newResult(testResultJSON);
						}						
					});
				}
			} catch (SocketException e1) {
				e1.printStackTrace();
			}
		}
	}

	public static ArrayList<NetworkInterface> getNetworkInterfaces() throws SocketException {
		ArrayList<NetworkInterface> netints = new ArrayList<NetworkInterface>();
		try {
			for(NetworkInterface netint : Collections.list(NetworkInterface.getNetworkInterfaces())) {
				if(networkInterfaceUsable(netint)) {
					netints.add(netint);					
				}
				for(NetworkInterface netint_ : Collections.list(netint.getSubInterfaces())) {
					if(networkInterfaceUsable(netint_)) {
						netints.add(netint_);					
					}
				}
			}
		} catch (SocketException e) {
			e.printStackTrace();
		}

		return netints;
	}

	private static boolean networkInterfaceUsable(NetworkInterface netint) throws SocketException {
		if(netint.isLoopback()) {
			return false;
		}

		if(!netint.isUp()) {
			return false;
		}

		// Not ideal but not sure of other way to exclude virtual box interfaces
		if(netint.getName().contains("vbox")) {
			return false;
		}
		return true;
	}

	public int getQueueSize() {
		return queue.getSize();
	}

	public void testUrl(String url) {
		queue.add(url, true);
	}

	public void testUrls(String[] urls) {
		for(String url : urls) {
			queue.add(url, false);
		}
	}

	protected abstract void urlQueueIsEmpty();

	protected abstract void newResult(JSONObject testResultJSON);
}