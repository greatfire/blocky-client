package com.greatfire.url.test.curl.options.plugins.impl;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import net.sf.json.JSONObject;

import com.greatfire.url.test.curl.GeneralProcess;
import com.greatfire.url.test.curl.options.plugins.ICurlOptionPlugin;

public class CurlOptionVerbosePlugin implements ICurlOptionPlugin {

	private static Log log = LogFactory.getLog(CurlOptionVerbosePlugin.class);

	@Override
	public void addResults(JSONObject results, GeneralProcess cp) {
		// Has been replaced by looking up DNS before cURL command
		results.put("host_ip", getHostIp(cp));
	}

	@Override
	public String[] getOptions(JSONObject urlTestResult) {
		return new String[]{
				// Enables verbose output. Will be directed to standard error.
				"-v"
		};
	}

	private String getHostIp(GeneralProcess cp) {
		for(String out : cp.getStdError()) {
			Pattern pattern = Pattern.compile("Trying (\\d{1,3}(\\.\\d{1,3}){3})");
			Matcher matcher = pattern.matcher(out);
			if(matcher.find()) {
				return matcher.group(1);
			}
		}
		log.error("Could not find host IP in std err: ");
		for(String out : cp.getStdError()) {
			log.error(out);
		}
		return null;
	}
}
