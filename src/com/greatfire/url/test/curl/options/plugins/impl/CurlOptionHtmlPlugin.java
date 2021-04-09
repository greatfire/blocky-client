package com.greatfire.url.test.curl.options.plugins.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sf.json.JSONObject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.greatfire.url.test.curl.Curl;
import com.greatfire.url.test.curl.GeneralProcess;
import com.greatfire.url.test.curl.options.plugins.ICurlOptionPlugin;

public class CurlOptionHtmlPlugin implements ICurlOptionPlugin {

	private File tmpFile;

	private static final int HTML_SAVE_MAX_LEN = 400000;
	private static final String[] HTML_SAVE_FOR_HOSTS = new String[]{"baidu.com", "google.com.hk", "weibo.com"};

	private static Log log = LogFactory.getLog(Curl.class);

	public CurlOptionHtmlPlugin() {
		try {
			tmpFile = File.createTempFile("curl",".html");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void addResults(JSONObject results, GeneralProcess cp) {

		if(tmpFile != null) {

			String html_charset = detectCharset(results, tmpFile);
			results.put("html_charset", html_charset);

			try {
				BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(tmpFile), html_charset));	
				
				try {
					// Read HTML without altering line breaks			
					StringBuilder builder = new StringBuilder();
					char[] buffer = new char[8192];
					int read;
					while ((read = in.read(buffer, 0, buffer.length)) > 0) {
						builder.append(buffer, 0, read);
					}
					String html = builder.toString();

					// Save HTML for certain URLs
					if(saveHtmlForThisUrl(results.getString("url"))) {

						// Truncate to max length
						if(html.length() > HTML_SAVE_MAX_LEN) {
							html = html.substring(0, HTML_SAVE_MAX_LEN);
						}

						log.info("Adding html with length: " + html.length());
						results.put("html", html);
					}

					// Look for html title
					Pattern patternHtmlTitle = Pattern.compile("<title>([^<]+)");
					Matcher matcherHtmlTitle = patternHtmlTitle.matcher(html);
					if(matcherHtmlTitle.find()) {
						String htmlTitle = matcherHtmlTitle.group(1).trim();
						results.put("html_title", htmlTitle);
					} else {
						//					log.error(html);
					}
				} finally {
					in.close();
				}

			} catch (FileNotFoundException e) {
				// Happens eg if curl request failed, then no file was created
				log.error("Could not read html file");
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 

			tmpFile.delete();	
		}
	}

	@Override
	public String[] getOptions(JSONObject urlTestResult) {
		if(tmpFile != null) {
			return new String[]{"-o", tmpFile.getAbsolutePath()};
		}
		return null;
	}

	private String detectCharset(JSONObject results, File tmpFile) {
		String charset = null;

		// 1. Get charset from header
		if(results.containsKey("content_type")) {
			log.debug("Looking for charset in header");
			Pattern pattern = Pattern.compile("charset=(.+)", Pattern.CASE_INSENSITIVE);
			Matcher matcher = pattern.matcher(results.getString("content_type"));
			if(matcher.find()) {
				charset = matcher.group(1);
			}
		}

		// 2. Read html file and look for meta tag
		try {
			log.debug("Looking for charset in html");
			BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(tmpFile)));
			String s;
			Pattern patternMetaCharset = Pattern.compile("<meta.*?charset=[\"']?([^\"']+)", Pattern.CASE_INSENSITIVE);
			Pattern patternEndHead = Pattern.compile("</head>", Pattern.CASE_INSENSITIVE);
			while((s = in.readLine()) != null) {
				log.debug("detectCharset reading html: " + s);
				Matcher matcherMetaCharset = patternMetaCharset.matcher(s);
				if(matcherMetaCharset.find()) {
					charset = matcherMetaCharset.group(1);
				}

				// Stop reading at the end of the <head> section
				Matcher matcherEndHead = patternEndHead.matcher(s);
				if(matcherEndHead.find()) {
					log.debug("detectCharset end of head");
					break;
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if(charset != null) {

			// Remove non-allowed characters
			charset = charset.replaceAll("[^A-Za-z0-9-_]", "");

			// Only return if a valid charset
			try {
				Charset ch = Charset.forName(charset);
				return ch.name();
			} catch(Exception e) {
				log.error(e.getMessage());
			}
		}

		// Default. Give up..
		return Charset.defaultCharset().displayName();
	}

	private boolean saveHtmlForThisUrl(String urlString) {
		try {
			URL url = new URL(urlString);

			for(String htmlSaveForHost : HTML_SAVE_FOR_HOSTS) {
				if(url.getHost().equals(htmlSaveForHost) || url.getHost().endsWith("." + htmlSaveForHost)) {
					return true;
				}
			}
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}
}
