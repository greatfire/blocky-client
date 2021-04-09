package com.greatfire.url.test.curl.options.plugins.impl;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONObject;

import com.greatfire.url.test.curl.Dig;
import com.greatfire.url.test.curl.GeneralProcess;
import com.greatfire.url.test.curl.options.plugins.ICurlOptionPlugin;

public class CurlOptionOptionsPlugin implements ICurlOptionPlugin {

	static Map<String, String> options = new HashMap<String, String>();

	static {
		options.put("curl_option_max_redir", "10");
		options.put("curl_option_max_time", "15");
		options.put("curl_option_retry", "2");
		options.put("curl_option_user_agent", "Mozilla/5.0");
	}

	@Override
	public void addResults(JSONObject results, GeneralProcess cp) {
		results.putAll(options);
	}

	//	 *  -k -L --max-redirs -m --retry
	@Override
	public String[] getOptions(JSONObject urlTestResult) {
		int port = 0;

		// Try to get port from URL (eg "http://host:port")
		try {
			port = new URL(urlTestResult.getString("url")).getPort();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}

		// If that failed, check if https, if so default to 443
		if(port < 1) {
			try {
				if(new URL(urlTestResult.getString("url")).getProtocol().toLowerCase().equals("https")) {
					port = 443;
				}
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		// All other cases, default to 80
		if(port < 1) {
			port = 80;			
		}

		List<String> out = new ArrayList<String>();

		/* 
				(SSL) This option explicitly allows curl to perform "insecure" SSL connections and transfers. All SSL connections are attempted to be made secure by using the CA certificate bundle installed by default. This makes all connections
              	considered "insecure" fail unless -k/--insecure is used.
		 */
		out.add("--insecure");

		/*
				(HTTP/HTTPS) If the server reports that the requested page has moved to a different location (indicated with a Location: header and a 3XX response code), this option will make curl redo the request  on  the  new  place.  If  used
				together  with -i/--include or -I/--head, headers from all requested pages will be shown. When authentication is used, curl only sends its credentials to the initial host. If a redirect takes curl to a different host, it won't be
				able to intercept the user+password. See also --location-trusted on how to change this. You can limit the amount of redirects to follow by using the --max-redirs option.

				When curl follows a redirect and the request is not a plain GET (for example POST or PUT), it will do the following request with a GET if the HTTP response was 301, 302, or 303. If the response code was any other 3xx  code,  curl
				will re-send the following request using the same unmodified method.
		 */
		out.add("--location");

		/* 
				Set maximum numberofredirection-followingsallowed. If
				-L/--location is used, this option can be used topreventcurl
				from following redirections "in absurdum". By default, the limit
				is set to 50 redirections. Set this option to -1 to make it lim-
				itless.
		 */
		out.add("--max-redirs");
		out.add(options.get("curl_option_max_redir"));

		/* 
				Maximum time in seconds that you allow the whole operation to take.  This is useful for preventing your batch jobs from hanging for hours due to slow networks or links going down.  See also the --connect-timeout option.
		 */
		out.add("--max-time");
		out.add(options.get("curl_option_max_time"));

		/*
				If a transient error is returned when curl tries to perform a transfer, it will retry this number of times before giving up. Setting the number to 0 makes curl do no retries (which is the default). Transient error means either: a
				timeout, an FTP 5xx response code or an HTTP 5xx response code.

				When curl is about to retry a transfer, it will first wait one second and then for all forthcoming retries it will double the waiting time until it reaches 10 minutes which then will be the delay between the rest of the  retries.
				By using --retry-delay you disable this exponential backoff algorithm. See also --retry-max-time to limit the total time allowed for retries. (Added in 7.12.3)
		 */
		out.add("--retry");
		out.add(options.get("curl_option_retry"));

		/*
				(HTTP) Specify the User-Agent string to send to the HTTP server. Some badly done CGIs fail if this field isn't set to "Mozilla/4.0". To encode blanks in the string, surround the string with single quote marks. This  can  also  be
              	set with the -H/--header option of course.
		 */
		out.add("--user-agent");
		out.add(options.get("curl_option_user_agent"));

		/*
				The active network interface
		 */
		out.add("--interface");
		out.add(urlTestResult.getString("netint"));
		
		/*
				Specifying the interface, some systems also require specifying the IP version.
		 */
		out.add("--ipv4");

		/*
				Use the IP resolved before the cURL command (unless the host is already an IP address)
		 */
		if(!Dig.isIpAddress(urlTestResult.getString("host"))) {
			out.add("--resolve");
			out.add(urlTestResult.getString("host") 
					+ ":" 
					+ port
					+ ":" 
					+ urlTestResult.getString("dig_host_ip")
					);
		}

		String[] simpleArray = new String[out.size()];
		return out.toArray(simpleArray);
	}
}