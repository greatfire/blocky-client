package com.greatfire.client.main;

import java.io.InputStream;
import java.util.Iterator;
import java.util.Properties;

import net.sf.json.JSONObject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xbill.DNS.ResolverConfig;

import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.greatfire.kryo.GFClient;
import com.greatfire.kryo.KryoCommands;
import com.greatfire.url.test.tester.UrlTester;

public class MainClient {

	private static Properties prop;
	
	private static Log log = LogFactory.getLog(MainClient.class);

	private static GFClient client;
	
	public static void main(String[] args) throws Exception {
		log.info("Starting main client");

		prop = new Properties();
		InputStream in = MainClient.class.getClassLoader().getResourceAsStream("client.properties");
		prop.load(in);
		
		for(String arg : args) {
			String[] chunks = arg.split("=");
			if(chunks.length == 2) {
				log.info("Overriding property: " + arg);
				prop.setProperty(chunks[0], chunks[1]);
			}
		}
		in.close();
		
		/*
		 *  Make sure there are usable network interfaces on machine
		 */
		UrlTester.getNetworkInterfaces();
		
		/*
		 *  Make sure there are DNS servers
		 */
		String[] servers = ResolverConfig.getCurrentConfig().servers();
		if(servers.length == 0) {
			throw new Exception("No DNS resolver servers could be found");
		}
		
		/*
		 *  Runs actual url tests
		 */
		String invalidDnsServer = getProp("invalidDnsServer");
		final UrlTester urlTester = new UrlTester(20000, 100000, 5, 50, invalidDnsServer) {
			private long lastRequestForMoreUrls = 0;

			@Override
			protected void newResult(JSONObject testResultJSON) {
				log.info("New url test result");
				log.info(testResultJSON.toString(2));
				client.sendTCP(testResultJSON);
			}

			@Override
			protected void urlQueueIsEmpty() {
				if(client == null) {
					return;
				}
				
				if(System.currentTimeMillis() - lastRequestForMoreUrls > 30000) {
					log.info("Requesting more URLs");
					client.sendTCP(KryoCommands.GET_URLS);
					lastRequestForMoreUrls = System.currentTimeMillis();
				}
			}
		};
		
		/*
		 *  Receive new urls from backend
		 */
		Listener backendListener = new Listener() {
			public void received (Connection connection, Object object) {
				if (object instanceof String) {
					String url = (String)object;
					log.info("Received url: " + url);
					urlTester.testUrl(url);
				}

				if (object instanceof String[]) {
					String[] urls = (String[])object;
					log.info("Received urls: " + urls.length);
					urlTester.testUrls(urls);
				}
			}
		};
		
		/*
		 *  Connect to backend
		 */
		client = new GFClient(
				getProp("backendHosts"), 
				getPropInt("backendTcpPort"), 
				getPropInt("backendUdpPort"),
				backendListener);
		
		log.info("Exiting main thread");
	}

	private static String getProp(String key) throws Exception {
		String value = prop.getProperty(key);
		if(value == null) {
			throw new Exception("Please specify arg: " + key);
		}
		log.info("Using prop " + key + ": " + value);
		return value;
	}

	private static int getPropInt(String key) throws NumberFormatException, Exception {
		int value = Integer.valueOf(getProp(key));
		if(value > 0) {
			return value;
		}
		throw new Exception("0 value for key: " + key);
	}
}