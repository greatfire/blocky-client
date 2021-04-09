package com.greatfire.url.test.tester;

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class UrlItem implements Comparable<UrlItem> {

	private long added;
	private String host = "";
	private int priority;
	private long tested = 0;
	private String url;

	private static Log log = LogFactory.getLog(UrlItem.class);

	public UrlItem(String url, int priority) {
		this.url = url;
		this.priority = priority;
		this.added = System.currentTimeMillis();

		try {
			URL u = new URL(url);
			this.host = u.getHost();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public int compareTo(UrlItem arg1) {
		if(this == arg1) {
//			log.info("Comparator: equal because same object: " + this.getUrl() + " / " + arg1.getUrl());
			return 0;
		}

		if(this.getUrl().equals(arg1.getUrl())) {
//			log.info("Comparator: equal because same URL: " + this.getUrl() + " / " + arg1.getUrl());
			return 0;
		}

		if(this.getPriority() != arg1.getPriority()) {
//			log.info("Comparator: different because different priority: " + this.getUrl() + " / " + arg1.getUrl());
			return this.getPriority() > arg1.getPriority() ? -1 : 1;
		}

		if(this.getSinceAdded() != arg1.getSinceAdded()) {
//			log.info("Comparator: different because added at different times: " + this.getUrl() + " / " + arg1.getUrl());
			return this.getSinceAdded() > arg1.getSinceAdded() ? -1 : 1;
		}

//		log.info("Comparator: different because different URL: " + this.getUrl() + " / " + arg1.getUrl());
		return this.getUrl().compareTo(arg1.getUrl());
	}

	@Override 
	public boolean equals(Object arg1) {
		if(this == arg1) {
			return true;
		}
		if(!(arg1 instanceof UrlItem)) {
			return false;
		}
		if(compareTo((UrlItem) arg1) == 0) {
			return true;
		}
		return false;
	}

	/**
	 * A class that overrides equals must also override hashCode.
	 */
	@Override 
	public int hashCode() {
		return new HashCodeBuilder(31, 17).append(url).toHashCode();
	}

	public String getHost() {
		return host;
	}

	public int getPriority() {
		return priority;
	}

	public long getSinceAdded() {
		return System.currentTimeMillis() - added;
	}

	public long getSinceTested() {
		if(tested != 0) {
			return System.currentTimeMillis() - tested;
		}
		return 0;
	}

	public long getTested() {
		return tested;
	}

	public String getUrl() {
		return url;
	}

	public void incrementPriority() {
		priority++;
	}

	public boolean isHighPriority() {
		return priority > 1;
	}

	public void setTested() {
		this.tested = System.currentTimeMillis();
	}
}
