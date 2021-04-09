package com.greatfire.url.test.tester;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class UrlQueue {

	private static final long MAX_KEEP_IN_HISTORY = 60000 * 2;

	private SortedSet<UrlItem> queue = Collections.synchronizedSortedSet(new TreeSet<UrlItem>());
	private Set<UrlItem> history = Collections.synchronizedSet(new HashSet<UrlItem>());

	private int minTimeBetweenLowPriorityTests;
	private int minTimeBetweenTestsSameHost;
	private int maxUrlsInQueue;

	private long tested = 0;

	private static Log log = LogFactory.getLog(UrlQueue.class);

	public UrlQueue(int minTimeBetweenLowPriorityTests, int minTimeBetweenTestsSameHost, int maxUrlsInQueue) {
		this.minTimeBetweenLowPriorityTests = minTimeBetweenLowPriorityTests;
		this.minTimeBetweenTestsSameHost = minTimeBetweenTestsSameHost;
		this.maxUrlsInQueue = maxUrlsInQueue;
	}

	public void add(String url, boolean highPriority) {
		cleanHistory();

		synchronized (history) {
			for(UrlItem ui : history) {
				if(ui.getUrl().equals(url)) {
					log.info("URL recently tested, ignoring");
					return;
				}
			}
		}

		UrlItem ui_new = new UrlItem(url, highPriority ? 2 : 1);
		synchronized (queue) {
			for(UrlItem ui_old : queue) {
				if(ui_old.equals(ui_new)) {
					if(highPriority) {
						log.info("URL " + url + " with priority " + ui_new.getPriority() + " already in queue. Incrementing its priority. Queue size now: " + queue.size());
						queue.remove(ui_old);
						ui_new = ui_old;
						ui_new.incrementPriority();
						break;
					} 

					else {
						log.info("URL " + url + " with priority " + ui_new.getPriority() + " already in queue. Ignoring. Queue size now: " + queue.size());
						return;
					}
				}
			}

			while(queue.size() > (maxUrlsInQueue + 1)) {
				log.info("Queue too big (" + queue.size() + "), removing last item");
				if(!queue.remove(queue.last())) {
					log.error("Failed removing last item in queue, resetting queue");
					queue.clear();
				}
			}

			if(queue.add(ui_new)) {
				log.info("Added URL " + url + " with priority " + ui_new.getPriority() + ". Queue size now: " + queue.size());
			} else {
				log.error("Could not add URL to queue " + url);
			}
		}
	}

	public String getNext() {
		synchronized(queue) {
			for(UrlItem ui : queue) {
				if(recentlyTested() && ui.isHighPriority() == false) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					return getNext();
				}

				if(hostTestedRecently(ui.getHost())) {
					log.debug("Recently tested this host, skipping for now: " + ui.getHost());
					continue;
				}

				int sizeBefore = queue.size();
				queue.remove(ui);
				int sizeAfter = queue.size();
				history.add(ui);
				ui.setTested();
				setTested();
				log.info("Returning URL for testing - " + ui.getUrl() + " with priority " + ui.getPriority() + ". Queue size before/after: " + sizeBefore + "/" + sizeAfter);
				if(sizeBefore == sizeAfter) {
					log.error("Queue size didn't change after remove - faulty comparator? - resetting queue");
					queue.clear();
				}
				return ui.getUrl();
			}
		}

		return null;
	}

	public int getSize() {
		return queue.size();
	}

	private void cleanHistory() {
		synchronized (history) {
			for(UrlItem ui : history) {
				if(ui.getSinceTested() > MAX_KEEP_IN_HISTORY) {
					history.remove(ui);
					cleanHistory();
					return;
				}
			}	
		}
	}

	private boolean recentlyTested() {
		if(tested > 0) {
			return (System.currentTimeMillis() - tested) < minTimeBetweenLowPriorityTests;
		}
		return false;
	}

	private boolean hostTestedRecently(String host) {
		if(hostTestedRecentlyExcluded(host)) {
			return false;
		}

		synchronized (history) {
			for(UrlItem ui : history) {
				if(ui.getHost().equals(host)) {
					if(ui.getSinceTested() < minTimeBetweenTestsSameHost) {
						return true;
					}
				}
			}
		}

		return false;
	}

	private boolean hostTestedRecentlyExcluded(String host) {
		// Together about 1/3 of our URLs, and both hosted in China so GFW reset problem doesn't apply
		if(host.equals("s.weibo.com") || host.equals("www.baidu.com")) { 
			return true;
		}
		return false;
	}

	private void setTested() {
		this.tested = System.currentTimeMillis();
	}
}