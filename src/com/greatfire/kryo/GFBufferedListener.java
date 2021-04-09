package com.greatfire.kryo;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import net.sf.json.JSONObject;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.greatfire.url.test.curl.Curl;

public abstract class GFBufferedListener extends Listener {

	public class GFReceivedObject {
		
		private InetSocketAddress addr;
		private Object obj;

		public GFReceivedObject(InetSocketAddress inetSocketAddress, Object obj) {
			setAddr(inetSocketAddress);
			setObj(obj);
		}

		public InetSocketAddress getAddr() {
			return addr;
		}

		private void setAddr(InetSocketAddress inetSocketAddress) {
			this.addr = inetSocketAddress;
		}
		
		public int getInt() {
			return (Integer)obj;
		}
		
		public JSONObject getJSON() {
			return (JSONObject)obj;
		}

		public Object getObj() {
			return obj;
		}

		private void setObj(Object obj) {
			this.obj = obj;
		}

		public boolean isInt() {
			return (obj != null && obj instanceof Integer);
		}

		public boolean isJSON() {
			return (obj != null && obj instanceof JSONObject);
		}
	}
	
	List<GFReceivedObject> objects = new ArrayList<GFReceivedObject>();
	
	private boolean running = false;
	private Runnable runnable;
	
	private static Log log = LogFactory.getLog(GFBufferedListener.class);
	
	public GFBufferedListener() {
		started();
		
		runnable = new Runnable() {

			@Override
			public void run() {
				running = true;
				
				// Make a copy of our objects
				List<GFReceivedObject> objects2 = new ArrayList<GFReceivedObject>(objects);
				
				// Clear original list now that we have a copy
				objects.clear();
				
				// Process the objects
				process(objects2);
				
				running = false;
			}
		};
	}

	@Override
	public void received(Connection conn, Object obj) {
		log.info("Received object");
		objects.add(new GFReceivedObject(conn.getRemoteAddressTCP(), obj));
		startThreadIfNotRunningAlready();
	}

	private synchronized void startThreadIfNotRunningAlready() {
		if(!running) {
			new Thread(runnable).start();
		}
	}

	protected abstract void process(List<GFReceivedObject> objects);

	protected abstract void started();
}