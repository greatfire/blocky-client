package com.greatfire.kryo;

import java.math.BigInteger;

import net.sf.json.JSONObject;

import com.esotericsoftware.kryo.Kryo;

public class KryoUtils {

	// Very large buffers since occasionally full html is returned from curl tests.. too big?
	public static int OBJECT_BUFFER_SIZE = 500000 * 4;
	public static int WRITE_BUFFER_SIZE = OBJECT_BUFFER_SIZE * 4;

	public static void registerClasses(Kryo kryo) {
		byte[] default_key = BigInteger.valueOf(574959673).toByteArray();
		registerClasses(kryo, default_key);
	}
	
	public static void registerClasses(Kryo kryo, byte[] key) {
		
		// Buffer larger here since encryption actually increases the size somewhat
//		kryo.register(JSONObject.class, new BlowfishCompressor(kryo.newSerializer(JSONObject.class), key, WRITE_BUFFER_SIZE));
//		kryo.register(String[].class, new BlowfishCompressor(kryo.newSerializer(String[].class), key, WRITE_BUFFER_SIZE));

		kryo.register(JSONObject.class);
		kryo.register(String[].class);
	}
}
