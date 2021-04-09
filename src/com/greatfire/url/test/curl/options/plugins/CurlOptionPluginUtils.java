package com.greatfire.url.test.curl.options.plugins;

import net.sf.json.JSONObject;

import org.apache.commons.lang.ArrayUtils;

import com.greatfire.url.test.curl.GeneralProcess;

public class CurlOptionPluginUtils {

	public static void addResults(ICurlOptionPlugin[] iCurlOptionPlugins, JSONObject results, GeneralProcess curlProcess) {
		for(ICurlOptionPlugin iCurlOptionPlugin : iCurlOptionPlugins) {
			iCurlOptionPlugin.addResults(results, curlProcess);
		}
	}

	public static String[] getOptions(ICurlOptionPlugin[] iCurlOptionPlugins, JSONObject urlTestResult) {
		String[] options = new String[0];
		for(ICurlOptionPlugin iCurlOptionPlugin : iCurlOptionPlugins) {
			options = (String[]) ArrayUtils.addAll(options, iCurlOptionPlugin.getOptions(urlTestResult));
		}
		return options;
	}
}
