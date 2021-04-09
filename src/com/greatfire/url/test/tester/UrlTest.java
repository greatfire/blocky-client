package com.greatfire.url.test.tester;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import net.sf.json.JSONObject;

import com.greatfire.url.test.curl.Curl;

public abstract class UrlTest implements Runnable {

	private static final long TIME_TO_LIVE = 60 * 1000;

	private long created;
	
	private String url;
	private String netint;
	private String invalidDnsServer;
	
	private Thread thread;
	
	private static Log log = LogFactory.getLog(UrlTest.class);

	public UrlTest(String url, String netint, String invalidDnsServer) {
		created = System.currentTimeMillis();
		
		this.url = url;
		this.netint = netint;
		this.invalidDnsServer = invalidDnsServer;
		
		thread = new Thread(this);
		thread.start();
	}
	
	@Override
	public void run() {
		Curl curl = new Curl(url, netint, invalidDnsServer);
		JSONObject testResultJSON = curl.getUrlTestResult();		
		newResult(testResultJSON);
	}

	public abstract void newResult(JSONObject testResultJSON);

	public boolean isDead() {
		if(thread == null) {
			log.info("UrlTest of " + url + " not yet started");
			return false;
		}
		
		if(!thread.isAlive()) {
			log.info("UrlTest of " + url + " finished");
			return true;
		}
		
		if((System.currentTimeMillis() - created) > TIME_TO_LIVE) {
			log.info("UrlTest of " + url + " expired");
			thread.interrupt();
			return true;
		}

		return false;
	}
}
