package com.greatfire.url.test.curl;

import java.util.Collections;
import java.util.List;

import org.apache.commons.validator.routines.InetAddressValidator;

public class Dig extends GeneralProcess {

	public Dig(String host, String server, int time) {
		super(new String[]{
				"dig", 
				"+short", 
				"+time=" + time,
				"@" + server,
				host
		});
	}
	
	// Using default timeout of 10 seconds
	public Dig(String host, String server) {
		this(host, server, 10);
	}

	public String getRandomARecord() {
		if(getStdOutput().size() > 0) {
			List<String> output = getStdOutput();
			Collections.shuffle(output);
			for(String record : output) {
				if(isIpAddress(record)) {
					return record;
				}
			}
		}
		return null;
	}

	public static boolean isIpAddress(String str) {
		return InetAddressValidator.getInstance().isValid(str);
	}
}
