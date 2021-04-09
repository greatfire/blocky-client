package com.greatfire.url.test.curl;

import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.validator.routines.InetAddressValidator;
import org.xbill.DNS.ResolverConfig;

import com.greatfire.url.test.curl.options.plugins.CurlOptionPluginUtils;
import com.greatfire.url.test.curl.options.plugins.ICurlOptionPlugin;
import com.greatfire.url.test.curl.options.plugins.impl.CurlOptionFormatPlugin;
import com.greatfire.url.test.curl.options.plugins.impl.CurlOptionHtmlPlugin;
import com.greatfire.url.test.curl.options.plugins.impl.CurlOptionOptionsPlugin;
import com.greatfire.url.test.curl.options.plugins.impl.CurlOptionVerbosePlugin;

import net.sf.json.JSONObject;

public class Curl {

	private ICurlOptionPlugin[] iCurlOptionPlugins = new ICurlOptionPlugin[] {
			new CurlOptionFormatPlugin(),
			new CurlOptionHtmlPlugin(),
			new CurlOptionOptionsPlugin(),
			new CurlOptionVerbosePlugin()
	};

	private GeneralProcess curlProcess;
	private boolean protocols_include_https;
	private JSONObject urlTestResult = new JSONObject();

	private static Log log = LogFactory.getLog(Curl.class);

	public Curl(String url, String netint, String invalidDnsServer) {
		// Add url to result
		urlTestResult.put("url", url);
		urlTestResult.put("netint", netint);

		try {
			setOsVersion();
			setCurlVersion();

			String host = new URL(url).getHost();
			urlTestResult.put("host", host);
			if(!Dig.isIpAddress(host)) {
				setInvalidDnsServerResult(host, invalidDnsServer);
				setDnsServer(host);
			}

			if(new URL(url).getProtocol().toLowerCase().equals("https") && !protocols_include_https) {
				throw new Exception("This is an HTTPS URL but this system can't test HTTPS");
			}

			String[] options = CurlOptionPluginUtils.getOptions(iCurlOptionPlugins, urlTestResult);
			String[] command = new String[options.length + 2];
			command[0] = "curl";
			for(int i = 0; i < options.length; i++) {
				command[i + 1] = options[i];
			}
			if(url != null) {
				command[command.length - 1] = url;
			}
			curlProcess = new GeneralProcess(command);

			CurlOptionPluginUtils.addResults(iCurlOptionPlugins, urlTestResult, curlProcess);
			urlTestResult.put("curl_exit_value", curlProcess.getExitValue());

		} catch (Exception e) {
			e.printStackTrace();
			urlTestResult.put("exception", e.getMessage());
		} 
	}

	public JSONObject getUrlTestResult() {
		return urlTestResult;
	}

	private void setDnsServer(String host) throws Exception {
		ResolverConfig.refresh();
		String[] servers = ResolverConfig.getCurrentConfig().servers();
		if(servers.length == 0) {
			throw new Exception("No DNS resolver servers could be found");
		}

		for(String server : servers) {
			urlTestResult.put("dns_server", server);
			Dig dig = new Dig(host, server);
			String record = dig.getRandomARecord();
			if(record != null) {
				urlTestResult.put("dig_host_ip", record);
				return;
			}
		}

		throw new Exception("The host " + host + " could not be resolved");
	}

	private void setInvalidDnsServerResult(String host, String invalidDnsServer) {
		urlTestResult.put("invalid_dns_server", invalidDnsServer);
		if(invalidDnsServer != null) {
			Dig dig = new Dig(host, invalidDnsServer, 3);
			String record = dig.getRandomARecord();
			urlTestResult.put("invalid_dns_server_dig_host_ip", record);
			urlTestResult.put("invalid_dns_server_dig_host_exit_value", dig.getExitValue());
		}
	}

	private void setCurlVersion() throws Exception {
		GeneralProcess cp = new GeneralProcess(new String[]{"curl", "--version"});
		List<String> cpStdOutput = cp.getStdOutput();
		if(cpStdOutput.size() != 3) {
			throw new Exception("Invalid curl version");
		}
		urlTestResult.put("curl_version", cpStdOutput.get(0));
		urlTestResult.put("curl_protocols", cpStdOutput.get(1));
		urlTestResult.put("curl_features", cpStdOutput.get(2));

		protocols_include_https = urlTestResult.getString("curl_protocols").contains("https");
	}

	private void setOsVersion() {
		urlTestResult.put("os_name", System.getProperty("os.name"));
	}
}