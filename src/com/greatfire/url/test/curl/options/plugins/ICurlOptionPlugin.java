package com.greatfire.url.test.curl.options.plugins;

import net.sf.json.JSONObject;

import com.greatfire.url.test.curl.GeneralProcess;

public interface ICurlOptionPlugin {

	public void addResults(JSONObject results, GeneralProcess curlProcess);
	
	public String[] getOptions(JSONObject urlTestResult);
}
