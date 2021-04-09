package com.greatfire.kryo;

import java.io.IOException;
import java.net.SocketException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Listener;

public class GFClient implements Runnable {

	private static final int TIMEOUT = 15000;
	private static Log log = LogFactory.getLog(GFClient.class);
	private int tcpPort;
	private int udpPort;
	private String[] hosts;
	private int host_index = 0;

	private Client client;
	private Listener listener;

	public GFClient(String hosts, int tcpPort, int udpPort, Listener listener) throws IOException {
		//		com.esotericsoftware.minlog.Log.TRACE();
		this.tcpPort = tcpPort;
		this.udpPort = udpPort;
		this.hosts = hosts.split(",");
		this.listener = listener;

		start();
	}

	@Override
	public void run() {
		while(true) {
			String host = getNextHost();
			try {
				connect(host);
			} catch (Exception e) {
				e.printStackTrace();
				try {
					Thread.sleep(TIMEOUT);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
					return;
				}
			}
		}
	}

	private void connect(String host) throws Exception {
		log.info("Starting new client to host: " + host);
		Client client2 = new Client(KryoUtils.WRITE_BUFFER_SIZE, KryoUtils.OBJECT_BUFFER_SIZE);
		client2.start();
		client2.connect(TIMEOUT, host, tcpPort, udpPort);
		
		KryoUtils.registerClasses(client2.getKryo());
		client2.addListener(listener);

		this.client = client2;
		
		while(true) {
			Thread.sleep(TIMEOUT);

			if(client2.isConnected()) {
				log.info("Connection to " + host + " is alive");

			} else {
				log.error("Connection to " + host + " is dead");
				client2.close();
				client2.stop();
				return;
			}
		}
	}

	private String getNextHost() {
		String host = hosts[host_index];
		host_index++;
		if(host_index == hosts.length) {
			host_index = 0;
		}
		return host;
	}

	private void start() {
		new Thread(this).start();
	}

	public void sendTCP(final Object object) {
		if(sendTCP_(object)) {
			return;
		}

		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					for(int i = 0; i < 10; i++) {
						log.error("Could not send object with length " + object.toString().length() + " over TCP, waiting and trying again");
						Thread.sleep(5000);
						if(sendTCP_(object)) {
							return;
						}
					}
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}).start();
	}

	private boolean sendTCP_(Object object) {
		if(client != null && client.isConnected()) {
			try {
				int bytesSent = client.sendTCP(object);
				if(bytesSent > 0) {
					return true;
				}
			} catch(Exception e) {
				log.error(e.getMessage());
			}
		}

		return false;
	}
}