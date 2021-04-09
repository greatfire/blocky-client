package com.greatfire.kryo;

import java.io.IOException;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Server;

public class GFServer extends Server {

	public GFServer(int tcpPort, int udpPort) throws IOException {
		super(KryoUtils.WRITE_BUFFER_SIZE, KryoUtils.OBJECT_BUFFER_SIZE);
//		com.esotericsoftware.minlog.Log.TRACE();
		start();
		KryoUtils.registerClasses(getKryo());
		bind(tcpPort, udpPort);
	}

	public int getNumActiveConnections() {
		int n = 0;
		for(Connection conn : getConnections()) {
			if(conn.isConnected()) {
				n++;
			}
		}
		return n;
	}
}
