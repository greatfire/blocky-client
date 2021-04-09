package com.greatfire.url.test.curl.options.plugins.impl;

import net.sf.json.JSONObject;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.greatfire.url.test.curl.GeneralProcess;
import com.greatfire.url.test.curl.options.plugins.ICurlOptionPlugin;

public class CurlOptionFormatPlugin implements ICurlOptionPlugin {

	private static final String SEPARATOR = "\\n";
	
	private static String[] variables = new String[] {
		"url_effective",
		"http_code",
		"time_total",
		"time_namelookup",
		"time_connect",
		"time_appconnect",
		"time_pretransfer",
		"time_redirect",
		"time_starttransfer",
		"size_download",
		"size_upload",
		"size_header",
		"size_request",
		"speed_download",
		"speed_upload",
		"content_type",
		"num_connects",
		"num_redirects",
		"redirect_url"
	};

	private static String formattedVariables = SEPARATOR + StringUtils.join(getFormattedVariables(), SEPARATOR) + SEPARATOR;
	
	private Log log = LogFactory.getLog(CurlOptionFormatPlugin.class);

	@Override
	public void addResults(JSONObject results, GeneralProcess cp) {
		for(String s : cp.getStdOutput()) {
			String[] keyAndValue = s.split(":", 2);
			if(keyAndValue.length == 2) {
				results.put(keyAndValue[0], keyAndValue[1]);
				log.debug(keyAndValue[0] + ": " + keyAndValue[1]);
			}
		}
	}
	
	@Override
	public String[] getOptions(JSONObject urlTestResult) {
		return new String[]{"--write-out", formattedVariables};
	}
	
	private static String[] getFormattedVariables() {
		String[] formattedVariables = new String[variables.length];
		int i = 0;
		for(String variable : variables) {
			formattedVariables[i] = variable + ":%{" + variable + "}";
			i++;
		}
		return formattedVariables;
	}
}
