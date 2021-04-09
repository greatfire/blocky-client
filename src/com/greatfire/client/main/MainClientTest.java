package com.greatfire.client.main;

import net.sf.json.JSONObject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.greatfire.url.test.tester.UrlTester;

public class MainClientTest {

	private static Log log = LogFactory.getLog(MainClientTest.class);
	
	public static void main(String[] args) throws Exception {        
		final UrlTester urlTester = new UrlTester(20000, 100000, 2, 50, null) {
			
			@Override
			protected void newResult(JSONObject testResultJSON) {
				log.info(testResultJSON.toString(2));
				System.exit(-1);
			}

			@Override
			protected void urlQueueIsEmpty() {
			}
		};
		urlTester.testUrl("http://www.google.com");
	}
}
